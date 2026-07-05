package com.wkclz.micro.points.helper;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.points.PointsConstants;
import com.wkclz.redis.helper.RedisHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 积分幂等检测助手
 * <p>
 * 基于 Redis 实现，用于积分写操作入口（发放、消费、回退、管理员手动发放）的幂等控制。
 * 幂等结果键格式：{@code points:idempotent:{bizType}:{bizNo}}（TTL 24 小时）
 * 处理中标记键格式：{@code points:idempotent:proc:{bizType}:{bizNo}}（TTL 30 秒，SETNX 防并发）
 * <p>
 * 幂等检测使用业务单据号（source_no / order_no），不依赖 flow_no。
 *
 * @see PointsConstants#IDEMPOTENT_KEY_PREFIX
 */
@Slf4j
@Component
public class PointsIdempotentHelper {

    /** 结果缓存 TTL（24 小时），覆盖业务重试周期 */
    private static final int RESULT_TTL_HOURS = 24;
    /** 处理中标记 TTL（30 秒），防并发抢占，超时自动释放 */
    private static final int PROCESSING_TTL_SECONDS = 30;
    /** 处理中标记 key 段，拼接在幂等前缀之后 */
    private static final String PROCESSING_KEY_SEGMENT = "proc:";
    /** 处理中标记的哨兵值（仅作占位，不会被读取为业务结果） */
    private static final String PROCESSING_VALUE = "1";

    @Autowired
    private RedisHelper redisHelper;

    /**
     * 幂等业务类型
     */
    public enum IdempotentBizType {
        /** 积分发放 */
        ISSUE,
        /** 积分消费 */
        CONSUME,
        /** 积分回退 */
        REFUND,
        /** 管理员手动发放 */
        ADMIN_ISSUE,
        /** 积分消费取消（支付失败补偿） */
        CANCEL
    }

    /**
     * 幂等检测结果
     */
    public static class IdempotentResult {

        private final boolean hit;
        private final String cachedResult;

        private IdempotentResult(boolean hit, String cachedResult) {
            this.hit = hit;
            this.cachedResult = cachedResult;
        }

        /** 是否命中幂等（已处理过） */
        public boolean isHit() {
            return hit;
        }

        /** 首次处理的缓存结果 JSON（仅 hit=true 时有值） */
        public String getCachedResult() {
            return cachedResult;
        }

        static IdempotentResult miss() {
            return new IdempotentResult(false, null);
        }

        static IdempotentResult hit(String cachedResult) {
            return new IdempotentResult(true, cachedResult);
        }
    }

    /**
     * 幂等检测：检查指定业务单据是否已处理过。
     * <p>
     * 命中（已处理）：返回 hit=true 及首次处理的缓存结果 JSON，调用方可直接返回，不重复变更数据。
     * 未命中（首次处理或处理中标记仍在）：返回 hit=false。
     *
     * @param bizType 幂等业务类型
     * @param bizNo   业务单据号（source_no / order_no）
     * @return 幂等检测结果
     */
    public IdempotentResult tryIdempotent(IdempotentBizType bizType, String bizNo) {
        validate(bizType, bizNo);
        String key = buildResultKey(bizType, bizNo);
        String cached = redisHelper.getString(key);
        if (cached != null) {
            log.info("幂等命中, bizType={}, bizNo={}", bizType, bizNo);
            return IdempotentResult.hit(cached);
        }
        log.debug("幂等未命中, bizType={}, bizNo={}", bizType, bizNo);
        return IdempotentResult.miss();
    }

    /**
     * 标记业务单据为处理中（防并发抢占）。
     * <p>
     * 基于 Redis SETNX + 30 秒 TTL，仅当无其他处理中标记时返回 true。
     * 调用方应在首次处理前调用本方法，处理完成后调用 {@link #cacheResult} 会清理本标记；
     * 若处理异常，标记将在 TTL 到期后自动释放，允许重试。
     *
     * @param bizType 幂等业务类型
     * @param bizNo   业务单据号
     * @return true=抢占成功，false=已被其他请求占用
     */
    public boolean markProcessing(IdempotentBizType bizType, String bizNo) {
        validate(bizType, bizNo);
        String procKey = buildProcessingKey(bizType, bizNo);
        boolean acquired = redisHelper.setIfAbsent(procKey, PROCESSING_VALUE, PROCESSING_TTL_SECONDS, TimeUnit.SECONDS);
        if (acquired) {
            log.debug("标记处理中成功, bizType={}, bizNo={}", bizType, bizNo);
        } else {
            log.info("标记处理中失败（已被占用）, bizType={}, bizNo={}", bizType, bizNo);
        }
        return acquired;
    }

    /**
     * 缓存首次处理结果（TTL 24 小时），并清理处理中标记。
     *
     * @param bizType    幂等业务类型
     * @param bizNo      业务单据号
     * @param resultJson 首次处理结果 JSON
     */
    public void cacheResult(IdempotentBizType bizType, String bizNo, String resultJson) {
        validate(bizType, bizNo);
        String key = buildResultKey(bizType, bizNo);
        redisHelper.setString(key, resultJson, RESULT_TTL_HOURS, TimeUnit.HOURS);
        // 清理处理中标记
        redisHelper.delete(buildProcessingKey(bizType, bizNo));
        log.info("缓存幂等结果成功, bizType={}, bizNo={}", bizType, bizNo);
    }

    private void validate(IdempotentBizType bizType, String bizNo) {
        if (bizType == null) {
            throw ValidationException.of("幂等业务类型不能为空");
        }
        if (bizNo == null || bizNo.isBlank()) {
            throw ValidationException.of("幂等业务单据号不能为空");
        }
    }

    private String buildResultKey(IdempotentBizType bizType, String bizNo) {
        return PointsConstants.IDEMPOTENT_KEY_PREFIX + bizType.name() + ":" + bizNo;
    }

    private String buildProcessingKey(IdempotentBizType bizType, String bizNo) {
        return PointsConstants.IDEMPOTENT_KEY_PREFIX + PROCESSING_KEY_SEGMENT + bizType.name() + ":" + bizNo;
    }
}
