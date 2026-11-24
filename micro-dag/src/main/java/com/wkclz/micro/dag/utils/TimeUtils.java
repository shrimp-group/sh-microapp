package com.wkclz.micro.dag.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 时间解析工具：模块内共用的宽松时间解析规则
 */
public final class TimeUtils {

    private TimeUtils() {
    }

    /** 宽松时间解析：先按 LocalDateTime.parse（无时区），失败再按 Instant.parse 转系统时区；两者皆失败抛原解析异常 */
    public static LocalDateTime parseFlexibleDateTime(String v) {
        try {
            return LocalDateTime.parse(v);
        } catch (Exception ignored) {
            // 尝试 Instant 解析
        }
        return LocalDateTime.ofInstant(Instant.parse(v), ZoneId.systemDefault());
    }
}
