package com.wkclz.micro.dag.utils;

import com.wkclz.core.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * JSON 工具：模块内共用单个 ObjectMapper（tools.jackson/Jackson 3，读写失败为 unchecked 异常）。
 * 按失败处理策略分变体，调用点按原语义选用：
 * parseMap（记 warn 后回退空 Map）/ parseMapQuiet（静默回退空 Map）/ readValue（异常外抛）/
 * toJson（记 error 后回退 "{}"）/ toJsonStrict（记 error 后抛 ValidationException）
 */
@Slf4j
public final class JsonUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonUtils() {
    }

    /** JSON → Map：null/空白返回空 Map；解析失败记 warn 后返回空 Map */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseMap(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return MAPPER.readValue(json, Map.class);
        } catch (Exception e) {
            log.warn("micro-dag: JSON 解析失败 json={}", json, e);
            return Map.of();
        }
    }

    /** JSON → Map：null/空白或解析失败均静默返回空 Map（节点参数等无需告警的容错场景） */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseMapQuiet(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return MAPPER.readValue(json, Map.class);
        } catch (Exception e) {
            return Map.of();
        }
    }

    /** JSON → 指定类型：解析失败异常向外传播，由调用方按上下文处理 */
    public static <T> T readValue(String json, Class<T> type) {
        return MAPPER.readValue(json, type);
    }

    /** 对象 → JSON：序列化失败记 warn 后返回 "{}" */
    public static String toJson(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("micro-dag: JSON 序列化失败", e);
            return "{}";
        }
    }

    /** 对象 → JSON：序列化失败记 error 后抛 ValidationException（参数落库等不允许静默截断的场景） */
    public static String toJsonStrict(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("micro-dag: 参数序列化失败", e);
            throw ValidationException.of("参数序列化失败: " + e.getMessage());
        }
    }
}
