package com.wkclz.micro.points.helper;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.points.PointsConstants;
import com.wkclz.redis.helper.RedisLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 积分用户级串行锁助手
 * <p>
 * 基于 RedisLock 实现，保证同一用户的积分处理（发放、消费、回退、异步扣减）串行执行，不同用户可并行。
 * 锁键格式：{@code points:lock:{userCode}}。
 * <p>
 * 锁持有上限 30 秒（避免持锁线程异常导致的死锁），获取失败抛 {@link ValidationException}。
 * 采用 {@link RedisLock#tryLockWithRetry} 短暂重试，平滑瞬时并发；不使用 Watchdog 续期，
 * 以保留 30 秒硬上限作为死兜底。
 *
 * @see PointsConstants#LOCK_KEY_PREFIX
 * @see RedisLock
 */
@Slf4j
@Component
public class PointsLockHelper {

    /** 锁持有上限（秒）：避免持锁线程异常导致的死锁 */
    private static final int LOCK_TIME_SECONDS = 30;
    /** 获取锁重试次数（短暂等待，平滑并发） */
    private static final int RETRY_COUNT = 3;
    /** 获取锁重试间隔（毫秒） */
    private static final int RETRY_DELAY_MILLIS = 100;

    @Autowired
    private RedisLock redisLock;

    /**
     * 在用户级锁内执行有返回值的逻辑。
     * <p>
     * 获取锁 → 执行 supplier → 释放锁（finally）。supplier 抛出的异常原样向上传播，锁始终被释放。
     *
     * @param userCode 用户编码
     * @param supplier 业务逻辑
     * @param <T>      返回类型
     * @return 业务逻辑返回值
     * @throws ValidationException 获取锁失败（被占用且重试耗尽）
     */
    public <T> T executeWithUserLock(String userCode, Supplier<T> supplier) {
        validateUserCode(userCode);
        String lockKey = buildLockKey(userCode);
        log.info("获取用户积分锁开始, userCode={}", userCode);

        String requestId = redisLock.tryLockWithRetry(
                lockKey, LOCK_TIME_SECONDS, TimeUnit.SECONDS,
                RETRY_COUNT, RETRY_DELAY_MILLIS, TimeUnit.MILLISECONDS);
        if (requestId == null) {
            log.warn("获取用户积分锁失败（被占用）, userCode={}", userCode);
            throw ValidationException.of("用户积分操作处理中，请稍后重试");
        }
        log.info("获取用户积分锁成功, userCode={}", userCode);
        try {
            return supplier.get();
        } finally {
            redisLock.releaseLock(lockKey, requestId);
            log.debug("释放用户积分锁, userCode={}", userCode);
        }
    }

    /**
     * 在用户级锁内执行无返回值的逻辑。
     *
     * @param userCode 用户编码
     * @param runnable 业务逻辑
     * @throws ValidationException 获取锁失败（被占用且重试耗尽）
     */
    public void executeWithUserLock(String userCode, Runnable runnable) {
        executeWithUserLock(userCode, () -> {
            runnable.run();
            return null;
        });
    }

    private void validateUserCode(String userCode) {
        if (userCode == null || userCode.isBlank()) {
            throw ValidationException.of("用户编码不能为空");
        }
    }

    private String buildLockKey(String userCode) {
        return PointsConstants.LOCK_KEY_PREFIX + userCode;
    }
}
