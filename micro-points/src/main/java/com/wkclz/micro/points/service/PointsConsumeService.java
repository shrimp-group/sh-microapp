package com.wkclz.micro.points.service;

import com.alibaba.fastjson2.JSON;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.iam.contract.context.PrincipalContext;
import com.wkclz.micro.points.bean.entity.PointsConsumeRecord;
import com.wkclz.micro.points.bean.entity.PointsDeductionRecord;
import com.wkclz.micro.points.bean.entity.PointsWallet;
import com.wkclz.micro.points.bean.enums.PointsConsumeStatus;
import com.wkclz.micro.points.bean.enums.PointsDeductionStatus;
import com.wkclz.micro.points.bean.req.PointsConsumeReq;
import com.wkclz.micro.points.bean.req.PointsRefundReq;
import com.wkclz.micro.points.bean.resp.PointsConsumeResp;
import com.wkclz.micro.points.bean.resp.PointsRefundResp;
import com.wkclz.micro.points.helper.PointsIdempotentHelper;
import com.wkclz.micro.points.helper.PointsIdempotentHelper.IdempotentBizType;
import com.wkclz.micro.points.helper.PointsIdempotentHelper.IdempotentResult;
import com.wkclz.micro.points.helper.PointsLockHelper;
import com.wkclz.micro.points.mapper.PointsConsumeRecordMapper;
import com.wkclz.micro.points.mapper.PointsDeductionRecordMapper;
import com.wkclz.micro.points.mapper.PointsEarnRecordMapper;
import com.wkclz.redis.helper.RedisIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 积分消费服务（两阶段消费之第一阶段：冻结）
 * <p>
 * 流程：参数校验 → 幂等检测（CONSUME:orderNo）→ 用户锁 → 事务（写消费流水 FROZEN + 钱包冻结 + 写 PENDING 任务记录）→
 * 事务提交后缓存幂等结果 → 锁外触发异步扣减。
 * <p>
 * 事务与锁的顺序：幂等(外) → 锁(中) → 事务(内)。
 * 由于 @Transactional 在同类自调用时不生效，采用 {@link TransactionTemplate} 编程式事务包裹 doConsume。
 * <p>
 * 异步扣减触发：在用户锁释放后触发，避免异步任务因等待同一用户锁而死锁。
 * 使用 {@link AtomicReference} 在锁内捕获 deductionFlowNo，锁外用于触发异步。
 *
 * @see PointsIdempotentHelper
 * @see PointsLockHelper
 * @see PointsAsyncDeductService
 */
@Slf4j
@Service
public class PointsConsumeService {

    /** 消费流水号前缀（PC = Points Consume） */
    private static final String CONSUME_FLOW_NO_PREFIX = "PC";
    /** 扣减任务流水号前缀（PD = Points Deduction） */
    private static final String DEDUCTION_FLOW_NO_PREFIX = "PD";

    @Autowired
    private PointsLockHelper lockHelper;
    @Autowired
    private RedisIdGenerator redisIdGenerator;
    @Autowired
    private PointsEarnRecordMapper earnMapper;
    @Autowired
    private PointsWalletService walletService;
    @Autowired
    private PointsRefundService pointsRefundService;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private PointsConsumeRecordMapper consumeMapper;
    @Autowired
    private PointsIdempotentHelper idempotentHelper;
    @Autowired
    private PointsDeductionRecordMapper deductionMapper;
    @Autowired
    private PointsAsyncDeductService asyncDeductService;

    /**
     * 积分消费（两阶段之第一阶段）。
     * <p>
     * 同一 orderNo 重复调用只生效一次（幂等键 CONSUME:orderNo）。
     * 校验余额 → 冻结积分 → 写 PENDING 任务记录 → 触发异步扣减。
     *
     * @param req 消费入参
     * @return 消费结果（flowNo/status=FROZEN/points）
     */
    public PointsConsumeResp consume(PointsConsumeReq req) {
        log.info("积分消费开始, userCode={}, points={}, orderNo={}",
                req.getUserCode(), req.getPoints(), req.getOrderNo());

        // 1. 参数校验
        validate(req);

        // 2. 幂等检测
        IdempotentResult idem = idempotentHelper.tryIdempotent(IdempotentBizType.CONSUME, req.getOrderNo());
        if (idem.isHit()) {
            log.info("积分消费幂等命中, orderNo={}", req.getOrderNo());
            return JSON.parseObject(idem.getCachedResult(), PointsConsumeResp.class);
        }
        if (!idempotentHelper.markProcessing(IdempotentBizType.CONSUME, req.getOrderNo())) {
            log.info("积分消费处理中, orderNo={}", req.getOrderNo());
            throw ValidationException.of("积分消费处理中，请稍后重试");
        }

        // 3. 解析租户编码
        String tenantCode = resolveTenantCode(req.getTenantCode());

        // 4. 用户锁 + 事务（在锁内捕获 deductionFlowNo，锁外触发异步扣减）
        AtomicReference<String> deductionFlowNoRef = new AtomicReference<>();
        PointsConsumeResp resp;
        try {
            resp = lockHelper.executeWithUserLock(req.getUserCode(), () -> {
                // 编程式事务：避免 @Transactional 同类自调用失效
                PointsConsumeResp r = transactionTemplate.execute(status -> doConsume(req, tenantCode, deductionFlowNoRef));
                // 事务提交成功后缓存幂等结果，保证仅缓存成功结果
                idempotentHelper.cacheResult(IdempotentBizType.CONSUME, req.getOrderNo(), JSON.toJSONString(r));
                return r;
            });
            log.info("积分消费成功, flowNo={}, userCode={}, orderNo={}, points={}",
                    resp.getFlowNo(), req.getUserCode(), req.getOrderNo(), resp.getPoints());
        } catch (Exception e) {
            log.error("积分消费失败, userCode={}, orderNo={}", req.getUserCode(), req.getOrderNo(), e);
            throw e;
        }

        // 5. 锁外触发异步扣减（避免异步任务等待同一用户锁而死锁）
        String deductionFlowNo = deductionFlowNoRef.get();
        if (deductionFlowNo != null) {
            log.info("触发异步扣减, deductionFlowNo={}, consumeFlowNo={}", deductionFlowNo, resp.getFlowNo());
            asyncDeductService.triggerAsyncDeduct(deductionFlowNo);
        }
        return resp;
    }

    /**
     * 消费核心逻辑（事务内执行）。
     * <p>
     * 校验余额 → 写 points_consume_record（status=FROZEN）→ 钱包冻结 → 写 points_deduction_record 任务记录（status=PENDING）。
     * 通过 deductionFlowNoRef 回传扣减任务流水号给外层，用于锁外触发异步扣减。
     *
     * @param req                消费入参
     * @param tenantCode         租户编码
     * @param deductionFlowNoRef 用于回传扣减任务流水号（不能为 null）
     * @return 消费结果
     */
    private PointsConsumeResp doConsume(PointsConsumeReq req, String tenantCode,
                                        AtomicReference<String> deductionFlowNoRef) {
        // ===== Q：查询钱包 =====
        PointsWallet wallet = walletService.getOrCreateWallet(tenantCode, req.getUserCode());

        // ===== CHK：校验可用积分余额 =====
        if (wallet.getAvailablePoints() < req.getPoints()) {
            log.warn("可用积分不足, userCode={}, available={}, need={}",
                    req.getUserCode(), wallet.getAvailablePoints(), req.getPoints());
            throw ValidationException.of("可用积分不足");
        }

        // ===== C1：写 points_consume_record（status=FROZEN） =====
        String consumeFlowNo = redisIdGenerator.generateIdWithPrefix(CONSUME_FLOW_NO_PREFIX);
        PointsConsumeRecord consumeRecord = new PointsConsumeRecord();
        consumeRecord.setTenantCode(tenantCode);
        consumeRecord.setUserCode(req.getUserCode());
        consumeRecord.setFlowNo(consumeFlowNo);
        consumeRecord.setConsumeTime(LocalDateTime.now());
        consumeRecord.setPoints(req.getPoints());
        consumeRecord.setReason(req.getReason());
        consumeRecord.setOrderNo(req.getOrderNo());
        consumeRecord.setStatus(PointsConsumeStatus.FROZEN.name());
        consumeMapper.insert(consumeRecord);
        log.info("消费流水写入成功, flowNo={}, userCode={}, orderNo={}, points={}",
                consumeFlowNo, req.getUserCode(), req.getOrderNo(), req.getPoints());

        // ===== F1：钱包冻结 available -= points, frozen += points =====
        walletService.freeze(tenantCode, req.getUserCode(), req.getPoints());

        // ===== D1：写 points_deduction_record 任务记录（earn_flow_no=NULL, status=PENDING） =====
        String deductionFlowNo = redisIdGenerator.generateIdWithPrefix(DEDUCTION_FLOW_NO_PREFIX);
        PointsDeductionRecord deductionTask = new PointsDeductionRecord();
        deductionTask.setTenantCode(tenantCode);
        deductionTask.setUserCode(req.getUserCode());
        deductionTask.setFlowNo(deductionFlowNo);
        deductionTask.setOrderNo(req.getOrderNo());
        deductionTask.setEarnFlowNo(null);
        deductionTask.setDeductionPoints(req.getPoints());
        deductionTask.setStatus(PointsDeductionStatus.PENDING.name());
        deductionMapper.insert(deductionTask);
        log.info("扣减任务记录写入成功, deductionFlowNo={}, userCode={}, orderNo={}, deductionPoints={}",
                deductionFlowNo, req.getUserCode(), req.getOrderNo(), req.getPoints());

        // 回传 deductionFlowNo 给外层（锁外触发异步扣减用）
        deductionFlowNoRef.set(deductionFlowNo);

        // 构造响应
        PointsConsumeResp resp = new PointsConsumeResp();
        resp.setFlowNo(consumeFlowNo);
        resp.setStatus(PointsConsumeStatus.FROZEN.name());
        resp.setPoints(req.getPoints());
        return resp;
    }

    /**
     * 支付失败补偿：取消积分消费。
     * <p>
     * 用于业务方支付失败后的积分补偿场景，根据原消费状态分支处理：
     * <ul>
     *   <li>FROZEN 状态：释放冻结积分，更新消费流水为 CANCELLED，更新 PENDING 扣减任务为 CANCELLED</li>
     *   <li>DEDUCTED 状态：计算可退积分，调用 {@link PointsRefundService#refundWithoutLock} 退款，
     *       返回退款流水号（消费流水状态保持 DEDUCTED）</li>
     * </ul>
     * 流程与 {@link #consume} 一致：幂等(外) → 锁(中) → 事务(内)。
     * 幂等键 CANCEL:orderNo，重复调用只生效一次。
     *
     * @param orderNo 原消费单据号
     * @param reason  取消原因
     * @return 取消结果（FROZEN 分支返回原 flowNo + status=CANCELLED + points；
     *         DEDUCTED 分支返回 refund 的 flowNo + status=DEDUCTED + 回退积分）
     */
    public PointsConsumeResp releaseConsume(String orderNo, String reason) {
        log.info("积分消费取消开始, orderNo={}, reason={}", orderNo, reason);

        // 1. 参数校验
        if (orderNo == null || orderNo.isBlank()) {
            throw ValidationException.of("orderNo 不能为空");
        }

        // 2. 幂等检测 CANCEL:orderNo
        IdempotentResult idem = idempotentHelper.tryIdempotent(IdempotentBizType.CANCEL, orderNo);
        if (idem.isHit()) {
            log.info("积分消费取消幂等命中, orderNo={}", orderNo);
            return JSON.parseObject(idem.getCachedResult(), PointsConsumeResp.class);
        }
        if (!idempotentHelper.markProcessing(IdempotentBizType.CANCEL, orderNo)) {
            log.info("积分消费取消处理中, orderNo={}", orderNo);
            throw ValidationException.of("积分取消处理中，请稍后重试");
        }

        // 3. 解析租户编码（releaseConsume 入参无 tenantCode，从登录态获取）
        String tenantCode = resolveTenantCode(null);

        // 4. 查询原消费流水获取 userCode、points
        PointsConsumeRecord consumeRecord = consumeMapper.selectByOrderNo(tenantCode, orderNo);
        if (consumeRecord == null) {
            log.warn("原消费记录不存在, orderNo={}", orderNo);
            throw ValidationException.of("原消费记录不存在");
        }
        String userCode = consumeRecord.getUserCode();
        Integer points = consumeRecord.getPoints() == null ? 0 : consumeRecord.getPoints();

        // 5. 用户锁 + 事务（在锁内执行取消逻辑，事务提交后缓存幂等结果）
        PointsConsumeResp resp;
        try {
            resp = lockHelper.executeWithUserLock(userCode, () -> {
                // 编程式事务：避免 @Transactional 同类自调用失效
                PointsConsumeResp r = transactionTemplate.execute(status ->
                        doReleaseConsume(orderNo, reason, consumeRecord, tenantCode, userCode, points));
                // 事务提交成功后缓存幂等结果，保证仅缓存成功结果
                idempotentHelper.cacheResult(IdempotentBizType.CANCEL, orderNo, JSON.toJSONString(r));
                return r;
            });
            log.info("积分消费取消成功, orderNo={}, userCode={}, status={}", orderNo, userCode, resp.getStatus());
        } catch (Exception e) {
            log.error("积分消费取消失败, orderNo={}, userCode={}", orderNo, userCode, e);
            throw e;
        }
        return resp;
    }

    /**
     * 取消消费核心逻辑（事务内执行）。
     * <p>
     * FROZEN 分支：释放冻结积分 + 更新消费流水为 CANCELLED + 更新 PENDING 任务为 CANCELLED。
     * DEDUCTED 分支：计算可退积分，调用 {@link PointsRefundService#refundWithoutLock} 退款
     * （refundWithoutLock 跳过幂等与用户锁，由本方法外层统一管理）。
     * CANCELLED 分支：幂等返回（外层幂等检测已命中，此处为防御性代码）。
     *
     * @param orderNo       原消费单据号
     * @param reason        取消原因
     * @param consumeRecord 原消费记录
     * @param tenantCode    租户编码
     * @param userCode      用户编码
     * @param points        原消费积分
     * @return 取消结果
     */
    private PointsConsumeResp doReleaseConsume(String orderNo, String reason, PointsConsumeRecord consumeRecord,
                                               String tenantCode, String userCode, Integer points) {
        String status = consumeRecord.getStatus();
        log.info("积分消费取消分支判断, orderNo={}, status={}", orderNo, status);

        // ===== FROZEN 分支：释放冻结积分 + 更新消费流水为 CANCELLED + 更新 PENDING 任务为 CANCELLED =====
        if (PointsConsumeStatus.FROZEN.name().equals(status)) {
            // 释放冻结积分
            walletService.releaseFrozen(tenantCode, userCode, points);
            log.info("释放冻结积分成功, orderNo={}, userCode={}, points={}", orderNo, userCode, points);

            // 更新消费流水状态为 CANCELLED
            consumeRecord.setStatus(PointsConsumeStatus.CANCELLED.name());
            int consumeRows = consumeMapper.updateByIdSelective(consumeRecord);
            if (consumeRows < 1) {
                log.warn("消费流水状态更新乐观锁失败, id={}, version={}", consumeRecord.getId(), consumeRecord.getVersion());
                throw ValidationException.of("消费流水状态更新冲突，请重试");
            }
            log.info("消费流水更新为 CANCELLED, orderNo={}, flowNo={}", orderNo, consumeRecord.getFlowNo());

            // 更新 PENDING 扣减任务记录为 CANCELLED
            PointsDeductionRecord deductionTask = deductionMapper.selectTaskRecordByOrderNo(tenantCode, orderNo);
            if (deductionTask != null && PointsDeductionStatus.PENDING.name().equals(deductionTask.getStatus())) {
                int taskRows = deductionMapper.updateStatusByVersion(
                        deductionTask.getId(), PointsDeductionStatus.CANCELLED.name(), deductionTask.getVersion());
                if (taskRows < 1) {
                    log.warn("扣减任务记录更新 CANCELLED 失败, deductionFlowNo={}, version={}",
                            deductionTask.getFlowNo(), deductionTask.getVersion());
                    throw ValidationException.of("扣减任务记录更新失败，请重试");
                }
                log.info("扣减任务记录更新为 CANCELLED, deductionFlowNo={}, orderNo={}",
                        deductionTask.getFlowNo(), orderNo);
            } else {
                log.info("无 PENDING 扣减任务记录或状态非 PENDING, orderNo={}", orderNo);
            }

            // 构造响应：原 flowNo + status=CANCELLED + points
            PointsConsumeResp resp = new PointsConsumeResp();
            resp.setFlowNo(consumeRecord.getFlowNo());
            resp.setStatus(PointsConsumeStatus.CANCELLED.name());
            resp.setPoints(points);
            return resp;
        }

        // ===== DEDUCTED 分支：计算可退积分，触发回退 =====
        if (PointsConsumeStatus.DEDUCTED.name().equals(status)) {
            // 计算已退积分：already_refunded = REFUND 获取流水 points 之和（source_no=orderNo）
            Integer alreadyRefundedVal = earnMapper.sumRefundPointsBySourceNo(tenantCode, orderNo);
            Integer alreadyRefunded = alreadyRefundedVal == null ? 0 : alreadyRefundedVal;
            Integer refundable = points - alreadyRefunded;
            log.info("DEDUCTED 分支可退积分计算, orderNo={}, totalPoints={}, alreadyRefunded={}, refundable={}",
                    orderNo, points, alreadyRefunded, refundable);

            // 无可退积分：跳过退款，仅返回幂等结果
            if (refundable <= 0) {
                log.info("无可退积分，跳过退款, orderNo={}, refundable={}", orderNo, refundable);
                PointsConsumeResp resp = new PointsConsumeResp();
                resp.setFlowNo(consumeRecord.getFlowNo());
                resp.setStatus(PointsConsumeStatus.DEDUCTED.name());
                resp.setPoints(0);
                return resp;
            }

            // 构造回退请求（refundNo=null，全额退款）
            PointsRefundReq refundReq = new PointsRefundReq();
            refundReq.setTenantCode(tenantCode);
            refundReq.setUserCode(userCode);
            refundReq.setPoints(refundable);
            refundReq.setReason(reason);
            refundReq.setOrderNo(orderNo);
            refundReq.setRefundNo(null);

            // 调用 PointsRefundService.refundWithoutLock（已持有用户锁，避免 RedisLock 不可重入导致的死锁）
            PointsRefundResp refundResp = pointsRefundService.refundWithoutLock(refundReq, tenantCode);
            log.info("回退成功, orderNo={}, refundFlowNo={}, refundPoints={}",
                    orderNo, refundResp.getFlowNo(), refundResp.getPoints());

            // 构造响应：refund 的 flowNo + status=DEDUCTED + 回退积分
            PointsConsumeResp resp = new PointsConsumeResp();
            resp.setFlowNo(refundResp.getFlowNo());
            resp.setStatus(PointsConsumeStatus.DEDUCTED.name());
            resp.setPoints(refundResp.getPoints());
            return resp;
        }

        // ===== CANCELLED 分支：幂等返回（外层幂等检测已命中，此处为防御性代码） =====
        if (PointsConsumeStatus.CANCELLED.name().equals(status)) {
            log.info("原消费已取消，幂等返回, orderNo={}", orderNo);
            PointsConsumeResp resp = new PointsConsumeResp();
            resp.setFlowNo(consumeRecord.getFlowNo());
            resp.setStatus(PointsConsumeStatus.CANCELLED.name());
            resp.setPoints(points);
            return resp;
        }

        // 其他未知状态
        log.warn("原消费状态不支持取消, orderNo={}, status={}", orderNo, status);
        throw ValidationException.of("原消费状态不支持取消：" + status);
    }

    /**
     * 参数校验
     */
    private void validate(PointsConsumeReq req) {
        if (req.getUserCode() == null || req.getUserCode().isBlank()) {
            throw ValidationException.of("userCode 不能为空");
        }
        if (req.getPoints() == null || req.getPoints() <= 0) {
            throw ValidationException.of("points 必须大于 0");
        }
        if (req.getOrderNo() == null || req.getOrderNo().isBlank()) {
            throw ValidationException.of("orderNo 不能为空");
        }
    }

    /**
     * 解析租户编码：入参优先，为空时从登录态获取
     */
    private String resolveTenantCode(String tenantCode) {
        if (tenantCode != null && !tenantCode.isBlank()) {
            return tenantCode;
        }
        return PrincipalContext.getTenantCode();
    }

}
