package com.wkclz.micro.mask.config;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.wkclz.iam.sdk.helper.SessionHelper;
import com.wkclz.micro.mask.cache.MaskCache;
import com.wkclz.micro.mask.pojo.entity.MdmMaskRule;
import com.wkclz.tool.tools.RegularTool;
import com.wkclz.tool.utils.JsUtil;
import com.wkclz.web.helper.RequestHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@ControllerAdvice
public class MaskResponseAdvice implements ResponseBodyAdvice<Object> {

    private static final Set<Class<?>> IGNORE_TYPES = Set.of(
        Boolean.class,
        Character.class,
        Byte.class,
        Short.class,
        Integer.class,
        Long.class,
        Float.class,
        Double.class,
        BigDecimal.class,
        Date.class,
        Void.class
    );

    private static final Cache<String, List<MdmMaskRule>> CACHE = CacheBuilder.newBuilder()
        .initialCapacity(100)
        .maximumSize(1024)
        .concurrencyLevel(Runtime.getRuntime().availableProcessors())
        .recordStats()
        .expireAfterAccess(12, TimeUnit.HOURS)
        .build();

    @Autowired
    private MaskCache maskCache;
    @Autowired
    private ObjectMapper objectMapper;


    // 缓存状态由 MaskCache 来驱动。若有变更，只会被拉取一次，否则一直缓存
    @Scheduled(fixedDelay = 12_000, initialDelay = 33_000)
    public static void clearCache() {
        if (MaskCache.getClearFlag()) {
            CACHE.invalidateAll();
            CACHE.cleanUp();
        }
    }

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        // 这里可以根据需要实现特定的条件判断
        return true;
    }

    @Override
    public Object beforeBodyWrite(
        Object body,
        MethodParameter returnType,
        MediaType selectedContentType,
        Class selectedConverterType,
        ServerHttpRequest request,
        ServerHttpResponse response) {

        // 基础类型，直接返回
        if (ignoreMask(body)) {
            return body;
        }

        // URL 判别是否需要继续脱敏，并缓存
        String userCode = SessionHelper.getUserCode();
        if (StringUtils.isBlank(userCode)) {
            userCode = "anonymous";
        }
        String method = request.getMethod().name();
        String uri = request.getURI().getPath();
        String key = userCode + ":" + method + ":" + uri;
        List<MdmMaskRule> curRules = CACHE.getIfPresent(key);

        if (curRules == null) {
            curRules = new ArrayList<>();
            List<MdmMaskRule> masks = maskCache.getMasks();
            for (MdmMaskRule mask : masks) {
                if (method.equals(mask.getRequestMethod()) && RequestHelper.match(mask.getRequestUri(), uri)) {
                    curRules.add(mask);
                }
            }
            CACHE.put(key, curRules);
        }

        // 没有命中任何规则
        if (curRules.isEmpty()) {
            // 直接返回，交予默认的 ObjectMapper 进行序列化
            return body;
        }

        try {
            // 使用默认的 ObjectMapper 进行序列化
            String valueStr = objectMapper.writeValueAsString(body);

            if (valueStr.startsWith("{")) {
                JSONObject jsonObject = JSONObject.parseObject(valueStr);
                maskFields(jsonObject, curRules);
                return jsonObject;
            }

            if (valueStr.startsWith("[")) {
                JSONArray jsonArray = JSONArray.parseArray(valueStr);
                maskFields(jsonArray, curRules);
                return jsonArray;
            }

            // 不是对象也不是数组，啥也不干！
            return body;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            // 如果处理失败，返回原始响应
            return body;
        }
    }


    private void maskFields(JSONObject jsonObject, List<MdmMaskRule> curRules) {
        if (jsonObject == null || CollectionUtils.isEmpty(curRules)) {
            return;
        }
        for (MdmMaskRule rule : curRules) {
            maskByRecursion(jsonObject, rule.getMaskJsonPath(), rule);
        }
    }
    private void maskFields(JSONArray jsonArray, List<MdmMaskRule> curRules) {
        if (CollectionUtils.isEmpty(jsonArray) || CollectionUtils.isEmpty(curRules)) {
            return;
        }
        for (MdmMaskRule rule : curRules) {
            String jsonPath = rule.getMaskJsonPath();
            // 参数是数组，但 JSONPath 不是以 $[*] 开头，必定啥也匹配不了，不需要考虑
            if (!jsonPath.startsWith("$[*]")) {
                continue;
            }
            maskByRecursion(jsonArray, jsonPath, rule);
        }
    }

    private static void maskByRecursion(JSONArray jsonArray, String jsonPath, MdmMaskRule rule) {
        if (CollectionUtils.isEmpty(jsonArray) || StringUtils.isBlank(jsonPath) || rule == null) {
            return;
        }
        if (!jsonPath.startsWith("$")) {
            jsonPath = "$" + jsonPath;
        }
        for (int i = 0; i < jsonArray.size(); i++) {
            Object data = jsonArray.get(i);
            switch (data) {
                // 空
                case null -> {}
                // 对象
                case JSONObject obj -> maskByRecursion(obj, jsonPath, rule);
                // 数组
                case JSONArray arr -> {
                    String path = jsonPath.substring(4);
                    maskByRecursion(arr, path, rule);
                }
                // 其他任意，均作为字符串处理
                default -> {
                    String result = maskByString(data.toString(), rule);
                    String tmp = jsonPath.replace("[*]", "[" + i + "]");
                    JSONPath.set(jsonArray, tmp, result);
                }
            }
        }
    }

    private static void maskByRecursion(JSONObject jsonObject, String jsonPath, MdmMaskRule rule) {
        if (jsonObject == null || StringUtils.isBlank(jsonPath) || rule == null) {
            return;
        }
        if (!jsonPath.startsWith("$")) {
            jsonPath = "$" + jsonPath;
        }
        if (jsonPath.contains("[*]")) {
            String tempJsonPath = jsonPath.substring(0, jsonPath.indexOf("[*]") + 3);
            Object data = JSONPath.eval(jsonObject, tempJsonPath);
            switch (data) {
                case null -> {}
                case JSONArray arr -> {
                    String tmp = jsonPath.substring(jsonPath.indexOf("[*]") + 3);
                    // 若 [*] 已是结束，此时应当保留 [*]，方便 maskByRecursion 直接 mask
                    if (StringUtils.isBlank(tmp)) {
                        tmp = "[*]";
                    }
                    maskByRecursion(arr, tmp, rule);
                }
                default -> log.warn("mask: 不支持的解析类型，解析结果不是数组");
            }
        } else {
            Object data = JSONPath.eval(jsonObject, jsonPath);
            switch (data) {
                // 无法提取
                case null -> {}
                // 提取之后仍然是对象
                case JSONObject ignored -> log.warn("mask: 不支持的解析类型: 解析结果仍然是对象");
                // 其他任意，均作为字符串处理
                default -> {
                    String result = maskByString(data.toString(), rule);
                    JSONPath.set(jsonObject, jsonPath, result);
                    // TODO 解密？
                }
            }
        }
    }


    public static String maskByString(String str, MdmMaskRule rule) {
        if (StringUtils.isNotBlank(rule.getMaskRuleRegular())) {
            return maskByRegular(str, rule.getMaskRuleRegular());
        }
        if (StringUtils.isNotBlank(rule.getMaskRuleScript())) {
            return maskByScript(str, rule.getMaskRuleScript());
        }
        return maskByDefault(str);
    }
    private static String maskByRegular(String str, String regular) {
        List<String> rts = RegularTool.find(str, regular);
        for (String rt : rts) {
            str = RegularTool.replaceAll(str, regular, "*".repeat(rt.length()));
        }
        return str;
    }
    private static String maskByScript(String str, String script) {
        return JsUtil.exec(script, str);
    }
    private static String maskByDefault(String str) {
        int length = str.length();
        if (length < 4) {
            return "***";
        }
        if (length < 8) {
            return str.charAt(0) + "****";
        }
        return str.substring(0, 3) + "****";
    }


    private static boolean ignoreMask(Object body) {
        if (body == null) {
            return true;
        }
        Class<?> clazz = body.getClass();
        return clazz.isPrimitive() || IGNORE_TYPES.contains(clazz);

    }
}