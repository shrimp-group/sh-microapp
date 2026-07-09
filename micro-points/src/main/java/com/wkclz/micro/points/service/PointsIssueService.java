package com.wkclz.micro.points.service;

import com.alibaba.fastjson2.JSON;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.iam.contract.context.PrincipalContext;
import com.wkclz.micro.points.PointsConstants;
import com.wkclz.micro.points.bean.entity.PointsEarnRecord;
import com.wkclz.micro.points.bean.entity.PointsWallet;
import com.wkclz.micro.points.bean.enums.PointsSourceType;
import com.wkclz.micro.points.bean.req.PointsIssueReq;
import com.wkclz.micro.points.bean.resp.PointsIssueResp;
import com.wkclz.micro.points.helper.PointsIdempotentHelper;
import com.wkclz.micro.points.helper.PointsLockHelper;
import com.wkclz.micro.points.mapper.PointsEarnRecordMapper;
import com.wkclz.redis.helper.RedisIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;

/**
 * 积分发放服务
 * <p>
 * 业务方发放（pointSourceType=ISSUANCE）与管理员手动发放（pointSourceType=ADMIN_ISSUE）共用本服务。
 * 流程：幂等检测 → 用户锁 → 事务（写获取流水 + 钱包累加）。
 * <p>
 * 事务与锁的顺序：幂等(外) → 锁(中) → 事务(内)。
 * 由于 @Transactional 在同类自调用时不生效，采用 {@link TransactionTemplate} 编程式事务包裹 doIssue。
 *
 * @see PointsIdempotentHelper
 * @see PointsLockHelper
 */
@Slf4j
@Service
public class PointsIssueService {

    /** 流水号前缀 */
    private static final String FLOW_NO_PREFIX = "PI";

    @Autowired
    private PointsWalletService walletService;
    @Autowired
    private PointsEarnRecordMapper earnRecordMapper;
    @Autowired
    private PointsIdempotentHelper idempotentHelper;
    @Autowired
    private PointsLockHelper lockHelper;
    @Autowired
    private RedisIdGenerator redisIdGenerator;
    @Autowired
    private TransactionTemplate transactionTemplate;

    /**
     * 发放积分。
     * <p>
     * 同一 sourceNo 重复调用只生效一次（幂等键 {ISSUE|ADMIN_ISSUE}:sourceNo）。
     *
     * @param req 发放入参
     * @return 发放结果（flowNo/points/availablePoints/totalEarnedPoints）
     */
    public PointsIssueResp issuePoints(PointsIssueReq req) {
        log.info("积分发放开始, userCode={}, points={}, sourceNo={}, pointSourceType={}",
                req.getUserCode(), req.getPoints(), req.getSourceNo(), req.getPointSourceType());

        // 1. 参数校验
        validate(req);

        // 2. 解析来源类型与幂等业务类型
        PointsSourceType sourceType = parseSourceType(req.getPointSourceType());
        PointsIdempotentHelper.IdempotentBizType bizType =
                (sourceType == PointsSourceType.ADMIN_ISSUE)
                        ? PointsIdempotentHelper.IdempotentBizType.ADMIN_ISSUE
                        : PointsIdempotentHelper.IdempotentBizType.ISSUE;

        // 3. 幂等检测
        PointsIdempotentHelper.IdempotentResult idem = idempotentHelper.tryIdempotent(bizType, req.getSourceNo());
        if (idem.isHit()) {
            log.info("积分发放幂等命中, bizType={}, sourceNo={}", bizType, req.getSourceNo());
            return JSON.parseObject(idem.getCachedResult(), PointsIssueResp.class);
        }
        if (!idempotentHelper.markProcessing(bizType, req.getSourceNo())) {
            log.info("积分发放处理中, bizType={}, sourceNo={}", bizType, req.getSourceNo());
            throw ValidationException.of("积分发放处理中，请稍后重试");
        }

        // 4. 解析租户编码
        String tenantCode = resolveTenantCode(req.getTenantCode());

        // 5. 用户锁 + 事务
        try {
            PointsIssueResp resp = lockHelper.executeWithUserLock(req.getUserCode(), () -> {
                // 编程式事务：避免 @Transactional 同类自调用失效
                PointsIssueResp r = transactionTemplate.execute(status -> doIssue(req, tenantCode, sourceType));
                // 事务提交成功后缓存幂等结果，保证仅缓存成功结果
                idempotentHelper.cacheResult(bizType, req.getSourceNo(), JSON.toJSONString(r));
                return r;
            });
            log.info("积分发放成功, flowNo={}, userCode={}, points={}",
                    resp.getFlowNo(), req.getUserCode(), resp.getPoints());
            return resp;
        } catch (Exception e) {
            log.error("积分发放失败, userCode={}, sourceNo={}", req.getUserCode(), req.getSourceNo(), e);
            throw e;
        }
    }

    /**
     * 发放核心逻辑（事务内执行）。
     * <p>
     * 写 points_earn_record（available_points=points, used_points=0, is_used_up=0）+ 钱包累加。
     *
     * @param req        发放入参
     * @param tenantCode 租户编码
     * @param sourceType 来源类型
     * @return 发放结果
     */
    @Transactional(rollbackFor = Exception.class)
    public PointsIssueResp doIssue(PointsIssueReq req, String tenantCode, PointsSourceType sourceType) {
        // 获取或创建钱包
        PointsWallet wallet = walletService.getOrCreateWallet(tenantCode, req.getUserCode());

        // 生成流水号
        String flowNo = redisIdGenerator.generateIdWithPrefix(FLOW_NO_PREFIX);

        // 写获取流水
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
        record.setPointSourceType(sourceType.name());
        record.setSourceNo(req.getSourceNo());
        earnRecordMapper.insert(record);
        log.info("获取流水写入成功, flowNo={}, userCode={}, points={}", flowNo, req.getUserCode(), req.getPoints());

        // 钱包累加 available + total_earned
        walletService.addAvailable(tenantCode, req.getUserCode(), req.getPoints());

        // 构造响应（钱包新值 = 原值 + points）
        PointsIssueResp resp = new PointsIssueResp();
        resp.setFlowNo(flowNo);
        resp.setPoints(req.getPoints());
        resp.setAvailablePoints(wallet.getAvailablePoints() + req.getPoints());
        resp.setTotalEarnedPoints(wallet.getTotalEarnedPoints() + req.getPoints());
        return resp;
    }

    /**
     * 参数校验
     */
    private void validate(PointsIssueReq req) {
        if (req.getUserCode() == null || req.getUserCode().isBlank()) {
            throw ValidationException.of("userCode 不能为空");
        }
        if (req.getPoints() == null || req.getPoints() <= 0) {
            throw ValidationException.of("points 必须大于 0");
        }
        if (req.getSourceNo() == null || req.getSourceNo().isBlank()) {
            throw ValidationException.of("sourceNo 不能为空");
        }
    }

    /**
     * 解析积分来源类型
     */
    private PointsSourceType parseSourceType(String pointSourceType) {
        if (pointSourceType == null || pointSourceType.isBlank()) {
            return PointsSourceType.ISSUANCE;
        }
        try {
            return PointsSourceType.valueOf(pointSourceType);
        } catch (IllegalArgumentException e) {
            throw ValidationException.of("无效的积分来源类型: " + pointSourceType);
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
