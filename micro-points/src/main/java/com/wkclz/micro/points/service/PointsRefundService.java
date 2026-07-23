package com.wkclz.micro.points.service;

import com.alibaba.fastjson2.JSON;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.core.identity.IdentityContext;
import com.wkclz.micro.points.bean.entity.PointsConsumeRecord;
import com.wkclz.micro.points.bean.entity.PointsEarnRecord;
import com.wkclz.micro.points.bean.enums.PointsConsumeStatus;
import com.wkclz.micro.points.bean.enums.PointsSourceType;
import com.wkclz.micro.points.bean.req.PointsRefundReq;
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

/**
 * 积分回退服务
 * <p>
 * 以发放方式回退积分，point_source_type=REFUND，source_no=原消费单据号 orderNo。
 * 回退在发放逻辑基础上额外增加原单据校验：
 * <ol>
 *   <li>原消费记录必须存在且 status=DEDUCTED（未完成扣减不可回退）</li>
 *   <li>退回积分 refund_points ≤ refundable = total_deducted - already_refunded</li>
 * </ol>
 * 流程：幂等检测 → 用户锁 → 事务（原单据校验 + 写获取流水 + 钱包累加）。
 * <p>
 * 事务与锁的顺序：幂等(外) → 锁(中) → 事务(内)。
 * 由于 @Transactional 在同类自调用时不生效，采用 {@link TransactionTemplate} 编程式事务包裹 doRefund。
 * <p>
 * 回退不更新原消费记录状态（保持 DEDUCTED），回退关系仅通过获取流水的 source_no 关联。
 *
 * @see PointsIdempotentHelper
 * @see PointsLockHelper
 * @see PointsIssueService
 */
@Slf4j
@Service
public class PointsRefundService {

    /** 流水号前缀（与发放保持一致，均为获取流水） */
    private static final String FLOW_NO_PREFIX = "PI";

    @Autowired
    private PointsWalletService walletService;
    @Autowired
    private PointsConsumeRecordMapper consumeMapper;
    @Autowired
    private PointsDeductionRecordMapper deductionMapper;
    @Autowired
    private PointsEarnRecordMapper earnMapper;
    @Autowired
    private PointsIdempotentHelper idempotentHelper;
    @Autowired
    private PointsLockHelper lockHelper;
    @Autowired
    private RedisIdGenerator redisIdGenerator;
    @Autowired
    private TransactionTemplate transactionTemplate;

    /**
     * 积分回退。
     * <p>
     * 幂等键根据入参 refundNo 动态决定：
     * <ul>
     *   <li>refundNo 非空时，幂等键为 REFUND:refundNo（支持同一 orderNo 多次部分退款）</li>
     *   <li>refundNo 为空时，幂等键为 REFUND:orderNo（全额退款，向后兼容）</li>
     * </ul>
     * orderNo 始终用于查找原消费记录、超额防护计算、回退获取流水的 source_no。
     * 复用发放核心逻辑（写获取流水 + 钱包累加），额外增加原单据校验与超额防护。
     *
     * @param req 回退入参
     * @return 回退结果（flowNo/points）
     */
    public PointsRefundResp refund(PointsRefundReq req) {
        log.info("积分回退开始, userCode={}, points={}, orderNo={}", req.getUserCode(), req.getPoints(), req.getOrderNo());

        // 1. 参数校验
        validate(req);

        // 2. 幂等 bizNo：refundNo 非空时用 refundNo（部分退款幂等），为空时用 orderNo（全额退款幂等，向后兼容）
        String bizNo = (req.getRefundNo() != null && !req.getRefundNo().isBlank()) ? req.getRefundNo() : req.getOrderNo();

        // 3. 幂等检测
        IdempotentResult idem = idempotentHelper.tryIdempotent(IdempotentBizType.REFUND, bizNo);
        if (idem.isHit()) {
            log.info("积分回退幂等命中, bizNo={}", bizNo);
            return JSON.parseObject(idem.getCachedResult(), PointsRefundResp.class);
        }
        if (!idempotentHelper.markProcessing(IdempotentBizType.REFUND, bizNo)) {
            log.info("积分回退处理中, bizNo={}", bizNo);
            throw ValidationException.of("积分回退处理中，请稍后重试");
        }

        // 4. 解析租户编码
        String tenantCode = resolveTenantCode(req.getTenantCode());

        // 5. 用户锁 + 事务
        try {
            PointsRefundResp resp = lockHelper.executeWithUserLock(req.getUserCode(), () -> {
                // 编程式事务：避免 @Transactional 同类自调用失效
                PointsRefundResp r = transactionTemplate.execute(status -> doRefund(req, tenantCode));
                // 事务提交成功后缓存幂等结果，保证仅缓存成功结果
                idempotentHelper.cacheResult(IdempotentBizType.REFUND, bizNo, JSON.toJSONString(r));
                return r;
            });
            log.info("积分回退成功, flowNo={}, userCode={}, orderNo={}, refundNo={}, points={}",
                    resp.getFlowNo(), req.getUserCode(), req.getOrderNo(), req.getRefundNo(), resp.getPoints());
            return resp;
        } catch (Exception e) {
            log.error("积分回退失败, userCode={}, orderNo={}, refundNo={}", req.getUserCode(), req.getOrderNo(), req.getRefundNo(), e);
            throw e;
        }
    }

    /**
     * 在已持有用户锁的情况下执行回退（供 {@link PointsConsumeService#releaseConsume} 调用）。
     * <p>
     * 与 {@link #refund} 的区别：
     * <ul>
     *   <li>跳过幂等检测（由调用方 releaseConsume 的 CANCEL:orderNo 幂等控制）</li>
     *   <li>跳过用户锁获取（调用方已持有用户锁，避免 RedisLock 不可重入导致的死锁）</li>
     * </ul>
     * 仅在事务内执行 {@link #doRefund} 逻辑。若外层已开启事务，则加入外层事务（PROPAGATION_REQUIRED）。
     *
     * @param req        回退入参
     * @param tenantCode 租户编码
     * @return 回退结果
     */
    PointsRefundResp refundWithoutLock(PointsRefundReq req, String tenantCode) {
        log.info("积分回退（无锁模式，调用方已持有用户锁）, userCode={}, points={}, orderNo={}",
                req.getUserCode(), req.getPoints(), req.getOrderNo());
        return transactionTemplate.execute(status -> doRefund(req, tenantCode));
    }

    /**
     * 回退核心逻辑（事务内执行）。
     * <p>
     * 原单据校验（FIND→CHK1→CALC→CHK2）→ 写 points_earn_record（REFUND）→ 钱包累加。
     * 不复用 {@link PointsIssueService#issuePoints} 的幂等/锁，因为外层已做，
     * 否则会导致同一用户锁重复获取（死锁）。
     *
     * @param req        回退入参
     * @param tenantCode 租户编码
     * @return 回退结果
     */
    private PointsRefundResp doRefund(PointsRefundReq req, String tenantCode) {
        // ===== FIND：按 orderNo 查原消费记录 =====
        PointsConsumeRecord consume = consumeMapper.selectByOrderNo(tenantCode, req.getOrderNo());
        log.info("回退原单据查询, tenantCode={}, orderNo={}, found={}", tenantCode, req.getOrderNo(), consume != null);

        // ===== CHK1：原消费存在且 status=DEDUCTED =====
        if (consume == null) {
            log.warn("原消费记录不存在, orderNo={}", req.getOrderNo());
            throw ValidationException.of("原消费记录不存在");
        }
        if (!PointsConsumeStatus.DEDUCTED.name().equals(consume.getStatus())) {
            log.warn("原消费未完成扣减，不可回退, orderNo={}, status={}", req.getOrderNo(), consume.getStatus());
            throw ValidationException.of("原消费未完成扣减，不可回退");
        }

        // ===== CALC：计算可退回额 =====
        // total_deducted = COMPLETED 动作记录 deduction_points 之和
        Integer totalDeductedVal = deductionMapper.sumCompletedDeductionPointsByOrderNo(tenantCode, req.getOrderNo());
        Integer totalDeducted = totalDeductedVal == null ? 0 : totalDeductedVal;
        // already_refunded = REFUND 获取流水 points 之和（source_no=orderNo）
        Integer alreadyRefundedVal = earnMapper.sumRefundPointsBySourceNo(tenantCode, req.getOrderNo());
        Integer alreadyRefunded = alreadyRefundedVal == null ? 0 : alreadyRefundedVal;
        Integer refundable = totalDeducted - alreadyRefunded;
        log.info("可退回额计算, orderNo={}, totalDeducted={}, alreadyRefunded={}, refundable={}",
                req.getOrderNo(), totalDeducted, alreadyRefunded, refundable);

        // ===== CHK2：超额防护 =====
        if (req.getPoints() > refundable) {
            log.warn("退回积分超过原单据扣减积分, orderNo={}, requestPoints={}, refundable={}",
                    req.getOrderNo(), req.getPoints(), refundable);
            throw ValidationException.of("退回积分超过原单据扣减积分，可退回：" + refundable);
        }

        // ===== W：写 points_earn_record（point_source_type=REFUND） =====
        String flowNo = redisIdGenerator.generateIdWithPrefix(FLOW_NO_PREFIX);
        PointsEarnRecord record = new PointsEarnRecord();
        record.setTenantCode(tenantCode);
        record.setUserCode(req.getUserCode());
        record.setFlowNo(flowNo);
        record.setEarnTime(LocalDateTime.now());
        record.setPoints(req.getPoints());
        record.setReason(req.getReason());
        // expire_time 为空时由 DB 默认值 2099-12-31 23:59:59 填充
        record.setExpireTime(req.getExpireTime());
        record.setUsedPoints(0);
        record.setAvailablePoints(req.getPoints());
        record.setIsUsedUp(0);
        record.setPointSourceType(PointsSourceType.REFUND.name());
        record.setSourceNo(req.getOrderNo());
        earnMapper.insert(record);
        log.info("回退获取流水写入成功, flowNo={}, userCode={}, orderNo={}, points={}",
                flowNo, req.getUserCode(), req.getOrderNo(), req.getPoints());

        // ===== W2：钱包累加 available + total_earned =====
        walletService.addAvailable(tenantCode, req.getUserCode(), req.getPoints());

        // 构造响应
        PointsRefundResp resp = new PointsRefundResp();
        resp.setFlowNo(flowNo);
        resp.setPoints(req.getPoints());
        return resp;
    }

    /**
     * 参数校验
     */
    private void validate(PointsRefundReq req) {
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
        return IdentityContext.getTenantCode();
    }

}
