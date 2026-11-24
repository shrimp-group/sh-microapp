package com.wkclz.micro.dag.utils;

import com.wkclz.core.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;

/**
 * 节点参数数值解析工具：Number 直取 / 字符串 trim 后解析，解析失败统一抛业务校验异常。
 * 上下限封顶策略随业务各异，由调用方自行处理
 */
@Slf4j
public final class ParamUtils {

    private ParamUtils() {
    }

    /** long 数值参数解析：Number 取 longValue，字符串 trim 后 Long.parseLong；非法记 warn 并抛 ValidationException */
    public static long parseLongParam(Object v, String label) {
        try {
            return v instanceof Number n ? n.longValue() : Long.parseLong(v.toString().trim());
        } catch (NumberFormatException e) {
            // 参数校验类可容忍失败，降级 warn
            log.warn("{} 参数非法 value={}", label, v, e);
            throw ValidationException.of(label + " 参数非法: " + v);
        }
    }

    /** int 数值参数解析：Number 取 intValue，字符串 trim 后 Integer.parseInt；非法记 warn 并抛 ValidationException */
    public static int parseIntParam(Object v, String label) {
        try {
            return v instanceof Number n ? n.intValue() : Integer.parseInt(v.toString().trim());
        } catch (NumberFormatException e) {
            // 参数校验类可容忍失败，降级 warn
            log.warn("{} 参数非法 value={}", label, v, e);
            throw ValidationException.of(label + " 参数非法: " + v);
        }
    }
}
