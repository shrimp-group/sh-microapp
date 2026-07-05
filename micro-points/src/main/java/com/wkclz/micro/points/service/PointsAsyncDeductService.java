package com.wkclz.micro.points.service;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.points.bean.entity.PointsConsumeRecord;
import com.wkclz.micro.points.bean.entity.PointsDeductionRecord;
import com.wkclz.micro.points.bean.entity.PointsEarnRecord;
import com.wkclz.micro.points.bean.enums.PointsConsumeStatus;
import com.wkclz.micro.points.bean.enums.PointsDeductionStatus;
import com.wkclz.micro.points.helper.PointsLockHelper;
import com.wkclz.micro.points.mapper.PointsConsumeRecordMapper;
import com.wkclz.micro.points.mapper.PointsDeductionRecordMapper;
import com.wkclz.micro.points.mapper.PointsEarnRecordMapper;
import com.wkclz.redis.helper.RedisIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 积分异步扣减服务（两阶段消费之第二阶段：扣减）
 * <p>
 * 处理 PENDING 任务记录（earn_flow_no=NULL）：
 * <ol>
 *   <li>消费后通过 {@link #triggerAsyncDeduct} 异步触发单条 PENDING 处理</li>
 *   <li>定时任务通过 {@link #processAllPending} 批量扫描所有 PENDING（兜底/重试）</li>
 * </ol>
 * <p>
 * 单条 PENDING 处理为单一事务（all-or-nothing），批量拉取仅用于 SELECT 优化：
 * <ul>
 *   <li>批量大小按 2^(n-1) 指数退避增长（1→2→4→8→…→1024），达 1024 后保持 1024</li>
 *   <li>每批按 expire_time ASC 排序（最近到期优先扣减，FIFO）</li>
 *   <li>每条 earn 扣减：写 COMPLETED 动作记录 + 更新 earn（used_points/available_points/is_used_up）</li>
 *   <li>累计扣减满足 need 后：任务记录置 PROCESSED，钱包 releaseFrozen，消费流水置 DEDUCTED</li>
 *   <li>获取流水不足：任务记录置 PARTIAL，钱包仅释放实际扣减额，告警日志</li>
 * </ul>
 * <p>
 * 用户级串行：每个用户的 PENDING 处理通过 RedisLock 串行，不同用户可并行。
 * <p>
 * 注意：{@link #processOnePending(String)} 会获取用户锁；{@link #processAllPending()} 在外层
 * 已按用户加锁，遍历用户任务时调用 {@link #processOnePendingLocked(PointsDeductionRecord)}
 * 直接执行事务，避免重复获取锁导致死锁（RedisLock 非可重入）。
 *
 * @see PointsLockHelper
 * @see PointsConsumeService
 */
@Slf4j
@Service
public class PointsAsyncDeductService {

    /** 扣减动作记录流水号前缀（PD = Points Deduction） */
    private static final String DEDUCTION_FLOW_NO_PREFIX = "PD";
    /** 批量拉取上限（2^10 = 1024） */
    private static final int MAX_BATCH_SIZE = 1024;
    /** 批量扫描分页大小（processAllPending 用） */
    private static final int SCAN_PAGE_SIZE = 200;

    @Autowired
    private PointsDeductionRecordMapper deductionMapper;
    @Autowired
    private PointsEarnRecordMapper earnMapper;
    @Autowired
    private PointsConsumeRecordMapper consumeMapper;
    @Autowired
    private PointsWalletService walletService;
    @Autowired
    private PointsLockHelper lockHelper;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private RedisIdGenerator redisIdGenerator;

    /**
     * 消费后异步触发单条 PENDING 处理。
     * <p>
     * 由 {@link PointsConsumeService#consume} 在锁外调用（避免异步任务等待同一用户锁而死锁）。
     * 通过 Spring @Async 在独立线程池执行，立即返回不阻塞调用方。
     * <p>
     * 注意：本方法必须由外部 Bean 调用才能使 @Async 生效（Spring AOP 代理）。
     *
     * @param deductionFlowNo 扣减任务流水号（消费时创建的 PENDING 任务记录的 flow_no）
     */
    @Async
    public void triggerAsyncDeduct(String deductionFlowNo) {
        if (deductionFlowNo == null || deductionFlowNo.isBlank()) {
            log.warn("触发异步扣减失败，deductionFlowNo 为空");
            return;
        }
        log.info("异步扣减触发, deductionFlowNo={}", deductionFlowNo);
        try {
            processOnePending(deductionFlowNo);
        } catch (Exception e) {
            // 异步任务异常不向上传播，避免影响调用方；PENDING 保留供下次 processAllPending 重试
            log.error("异步扣减失败, deductionFlowNo={}", deductionFlowNo, e);
        }
    }

    /**
     * 批量处理所有 PENDING 任务记录（定时任务/手动触发）。
     * <p>
     * 分页扫描 PENDING 任务记录，按 user_code 分组，对每个用户用 RedisLock 串行处理其全部 PENDING。
     * 单条 PENDING 处理失败（事务回滚）不影响其他 PENDING。
     * <p>
     * 注意：本方法已在外层按用户加锁，遍历用户任务时直接调用
     * {@link #processOnePendingLocked(PointsDeductionRecord)} 执行事务，
     * 不再重复获取用户锁（RedisLock 非可重入，重复获取会死锁）。
     */
    public void processAllPending() {
        log.info("批量处理 PENDING 任务开始");
        int offset = 0;
        AtomicInteger totalProcessed = new AtomicInteger();
        AtomicInteger totalFailed = new AtomicInteger();
        while (true) {
            List<PointsDeductionRecord> page = deductionMapper.selectPendingTaskRecords(offset, SCAN_PAGE_SIZE);
            if (page == null || page.isEmpty()) {
                break;
            }
            // 按 user_code 分组（保持顺序，便于按用户串行）
            Map<String, List<PointsDeductionRecord>> grouped = new LinkedHashMap<>();
            for (PointsDeductionRecord task : page) {
                grouped.computeIfAbsent(task.getUserCode(), k -> new ArrayList<>()).add(task);
            }
            // 对每个用户：获取用户锁后串行处理其全部 PENDING
            for (Map.Entry<String, List<PointsDeductionRecord>> entry : grouped.entrySet()) {
                String userCode = entry.getKey();
                List<PointsDeductionRecord> userTasks = entry.getValue();
                try {
                    lockHelper.executeWithUserLock(userCode, () -> {
                        for (PointsDeductionRecord task : userTasks) {
                            try {
                                processOnePendingLocked(task);
                                totalProcessed.getAndIncrement();
                            } catch (Exception e) {
                                totalFailed.getAndIncrement();
                                log.error("处理 PENDING 失败, deductionFlowNo={}", task.getFlowNo(), e);
                            }
                        }
                    });
                } catch (Exception e) {
                    // 获取用户锁失败时，本页该用户跳过，由下次扫描重试
                    log.warn("获取用户锁失败，跳过本页该用户, userCode={}", userCode, e);
                }
            }
            if (page.size() < SCAN_PAGE_SIZE) {
                break;
            }
            offset += SCAN_PAGE_SIZE;
        }
        log.info("批量处理 PENDING 任务结束, totalProcessed={}, totalFailed={}", totalProcessed, totalFailed);
    }

    /**
     * 处理单条 PENDING 任务记录（核心）。
     * <p>
     * 查询任务记录 → 状态校验 → 获取用户锁 → 单一事务内 doDeduct（all-or-nothing）。
     * 若任务记录已非 PENDING（已处理），跳过。
     * <p>
     * 适用场景：异步触发（{@link #triggerAsyncDeduct}），调用方未持有用户锁。
     *
     * @param deductionFlowNo 扣减任务流水号
     */
    public void processOnePending(String deductionFlowNo) {
        if (deductionFlowNo == null || deductionFlowNo.isBlank()) {
            log.warn("处理 PENDING 失败，deductionFlowNo 为空");
            return;
        }
        // 查询任务记录（按 flow_no 唯一）
        PointsDeductionRecord query = new PointsDeductionRecord();
        query.setFlowNo(deductionFlowNo);
        PointsDeductionRecord task = deductionMapper.selectOneByEntity(query);
        if (task == null) {
            log.warn("PENDING 任务记录不存在, deductionFlowNo={}", deductionFlowNo);
            return;
        }
        // 幂等防护：非 PENDING 状态直接跳过
        if (!PointsDeductionStatus.PENDING.name().equals(task.getStatus())) {
            log.info("PENDING 任务已处理，跳过, deductionFlowNo={}, status={}", deductionFlowNo, task.getStatus());
            return;
        }
        String userCode = task.getUserCode();
        log.info("处理 PENDING 任务开始, deductionFlowNo={}, userCode={}, orderNo={}, need={}",
                deductionFlowNo, userCode, task.getOrderNo(), task.getDeductionPoints());

        // 获取用户锁 + 单一事务执行扣减（all-or-nothing）
        lockHelper.executeWithUserLock(userCode, () -> processOnePendingLocked(task));
    }

    /**
     * 处理单条 PENDING 任务记录（已持有用户锁版本）。
     * <p>
     * 假定调用方已获取用户锁，直接执行事务。
     * 适用场景：
     * <ul>
     *   <li>{@link #processOnePending(String)} 内部调用（已获取锁）</li>
     *   <li>{@link #processAllPending()} 用户锁内遍历调用（已获取锁）</li>
     * </ul>
     * <p>
     * 单条 PENDING 处理失败时抛出异常，事务自动回滚，调用方负责 try-catch 隔离。
     *
     * @param task PENDING 任务记录（status 必须为 PENDING）
     */
    private void processOnePendingLocked(PointsDeductionRecord task) {
        // 二次校验状态（防止 processAllPending 扫描期间状态已变）
        if (!PointsDeductionStatus.PENDING.name().equals(task.getStatus())) {
            log.info("PENDING 任务已处理（locked 路径），跳过, deductionFlowNo={}, status={}",
                    task.getFlowNo(), task.getStatus());
            return;
        }
        transactionTemplate.execute(status -> doDeduct(task));
    }

    /**
     * 执行扣减核心逻辑（单一事务，all-or-nothing）。
     * <p>
     * 批量拉取策略：批次大小按 2^(n-1) 增长（1→2→4→8→…→1024），达 1024 后保持 1024。
     * 每批按 expire_time ASC 排序，offset 跳过已处理的 earn。
     * 所有 UPDATE 在同一事务内，避免部分提交导致重复扣减。
     *
     * @param task PENDING 任务记录
     */
    private Void doDeduct(PointsDeductionRecord task) {
        String tenantCode = task.getTenantCode();
        String userCode = task.getUserCode();
        String orderNo = task.getOrderNo();
        Integer need = task.getDeductionPoints() == null ? 0 : task.getDeductionPoints();
        Integer accumulatedPoints = 0;

        int batchN = 1;
        int offset = 0;
        while (true) {
            int batchSize = computeBatchSize(batchN);
            // 批量拉取可用获取流水（available_points > 0, 按 expire_time ASC）
            List<PointsEarnRecord> batch = earnMapper.selectAvailableBatchByExpireTime(
                    tenantCode, userCode, batchSize, offset);
            log.info("拉取批次, deductionFlowNo={}, batchN={}, batchSize={}, offset={}, fetched={}",
                    task.getFlowNo(), batchN, batchSize, offset, batch == null ? 0 : batch.size());
            if (batch == null || batch.isEmpty()) {
                // 无更多可用流水
                break;
            }
            // 遍历批次中每条 earn，逐条扣减
            for (PointsEarnRecord earn : batch) {
                if (accumulatedPoints >= need) {
                    break;
                }
                Integer earnAvailable = earn.getAvailablePoints() == null ? 0 : earn.getAvailablePoints();
                if (earnAvailable <= 0) {
                    continue;
                }
                // 本次扣减 = min(earn.available, need - accumulated)
                Integer deduct = Math.min(earnAvailable, need - accumulatedPoints);

                // 写 COMPLETED 动作记录（earn_flow_no 非空）
                String actionFlowNo = redisIdGenerator.generateIdWithPrefix(DEDUCTION_FLOW_NO_PREFIX);
                PointsDeductionRecord action = new PointsDeductionRecord();
                action.setTenantCode(tenantCode);
                action.setUserCode(userCode);
                action.setFlowNo(actionFlowNo);
                action.setOrderNo(orderNo);
                action.setEarnFlowNo(earn.getFlowNo());
                action.setDeductionPoints(deduct);
                action.setStatus(PointsDeductionStatus.COMPLETED.name());
                deductionMapper.insert(action);

                // 更新 earn：usedPoints += deduct, availablePoints -= deduct, isUsedUp 视情况置 1
                Integer newUsed = (earn.getUsedPoints() == null ? 0 : earn.getUsedPoints()) + deduct;
                Integer newAvailable = earnAvailable - deduct;
                earn.setUsedPoints(newUsed);
                earn.setAvailablePoints(newAvailable);
                earn.setIsUsedUp(newAvailable == 0 ? 1 : 0);
                int rows = earnMapper.updateByIdSelective(earn);
                if (rows < 1) {
                    log.warn("earn 更新乐观锁失败, earnFlowNo={}, version={}", earn.getFlowNo(), earn.getVersion());
                    throw ValidationException.of("获取流水更新冲突，请重试");
                }

                accumulatedPoints += deduct;
                log.info("扣减明细, deductionFlowNo={}, earnFlowNo={}, actionFlowNo={}, deduct={}, accumulated={}, need={}",
                        task.getFlowNo(), earn.getFlowNo(), actionFlowNo, deduct, accumulatedPoints, need);
            }
            offset += batch.size();
            if (accumulatedPoints >= need) {
                break;
            }
            batchN++;
        }

        // 处理结果：满足或不足
        if (accumulatedPoints >= need) {
            // ===== DONE：任务记录置 PROCESSED，钱包 releaseFrozen(need)，消费流水置 DEDUCTED =====
            int taskRows = deductionMapper.updateStatusByVersion(task.getId(), PointsDeductionStatus.PROCESSED.name(), task.getVersion());
            if (taskRows < 1) {
                log.warn("任务记录状态更新乐观锁失败, id={}, version={}", task.getId(), task.getVersion());
                throw ValidationException.of("任务记录状态更新冲突，请重试");
            }
            walletService.releaseFrozen(tenantCode, userCode, need);

            // 消费流水 FROZEN → DEDUCTED
            PointsConsumeRecord consume = consumeMapper.selectByOrderNo(tenantCode, orderNo);
            if (consume != null) {
                consume.setStatus(PointsConsumeStatus.DEDUCTED.name());
                int consumeRows = consumeMapper.updateByIdSelective(consume);
                if (consumeRows < 1) {
                    log.warn("消费流水状态更新乐观锁失败, id={}, version={}", consume.getId(), consume.getVersion());
                    throw ValidationException.of("消费流水状态更新冲突，请重试");
                }
            } else {
                log.warn("消费流水不存在, orderNo={}", orderNo);
            }
            log.info("PENDING 处理完成（PROCESSED）, deductionFlowNo={}, userCode={}, orderNo={}, need={}",
                    task.getFlowNo(), userCode, orderNo, need);
        } else {
            // ===== PARTIAL：任务记录置 PARTIAL，钱包 releaseFrozen(实际扣减额)，告警 =====
            int taskRows = deductionMapper.updateStatusByVersion(task.getId(), PointsDeductionStatus.PARTIAL.name(), task.getVersion());
            if (taskRows < 1) {
                log.warn("任务记录状态更新乐观锁失败（PARTIAL）, id={}, version={}", task.getId(), task.getVersion());
                throw ValidationException.of("任务记录状态更新冲突，请重试");
            }
            if (accumulatedPoints > 0) {
                walletService.releaseFrozen(tenantCode, userCode, accumulatedPoints);
            }
            Integer gap = need - accumulatedPoints;
            log.warn("积分不足（PARTIAL）, deductionFlowNo={}, userCode={}, orderNo={}, need={}, deducted={}, gap={}",
                    task.getFlowNo(), userCode, orderNo, need, accumulatedPoints, gap);
        }
        return null;
    }

    /**
     * 计算第 n 批的批量大小：2^(n-1)，封顶 1024 后保持 1024。
     * <ul>
     *   <li>n=1: 2^0 = 1</li>
     *   <li>n=2: 2^1 = 2</li>
     *   <li>n=3: 2^2 = 4</li>
     *   <li>...</li>
     *   <li>n=11: 2^10 = 1024</li>
     *   <li>n≥12: 1024（封顶）</li>
     * </ul>
     *
     * @param batchN 批次序号（从 1 开始）
     * @return 批量大小
     */
    private int computeBatchSize(int batchN) {
        if (batchN <= 1) {
            return 1;
        }
        // 2^(n-1)，n>=12 时封顶 1024
        int shift = batchN - 1;
        if (shift >= 10) {
            return MAX_BATCH_SIZE;
        }
        return 1 << shift;
    }

}
