package com.wkclz.micro.points;

/**
 * 积分模块常量
 */
public class PointsConstants {

    /**
     * 积分与现金比例：100 积分 = 1 元
     */
    public static final int POINTS_TO_CASH_RATE = 100;

    /**
     * 默认到期时间（数据库默认值，表示永不过期）
     */
    public static final String DEFAULT_EXPIRE_TIME = "2099-12-31 23:59:59";

    /**
     * 幂等检测 Redis key 前缀
     */
    public static final String IDEMPOTENT_KEY_PREFIX = "points:idempotent:";

    /**
     * 用户锁 Redis key 前缀
     */
    public static final String LOCK_KEY_PREFIX = "points:lock:";

    private PointsConstants() {
        // 私有构造器，防止实例化
    }
}
