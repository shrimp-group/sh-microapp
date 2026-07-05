package com.wkclz.micro.audit.utils;

import com.alibaba.fastjson2.JSONObject;
import com.wkclz.core.base.BaseEntity;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.tool.bean.JavaField;
import com.wkclz.tool.utils.BeanUtil;
import com.wkclz.tool.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class AuditCompareUtil {

    public static <T extends BaseEntity> String getTableName(T data) {
        if (data == null) {
            throw ValidationException.of("getTableName data can not be null");
        }
        return getTableName(data.getClass());
    }
    public static <T extends BaseEntity> String getTableName(Class<T> clazz) {
        if (clazz == null) {
            throw ValidationException.of("getTableName clazz can not be null");
        }
        String simpleName = clazz.getSimpleName();
        return StringUtil.camelToUnderline(simpleName);
    }

    // 获取单个对象的值
    public static <T extends BaseEntity> String getDataValueJson(T data) {
        Map<String, Object> values = getValues(data);
        return JSONObject.toJSONString(values);
    }

    /**
     * 从对象中提取内容
     */
    private static <T extends BaseEntity> Map<String, Object> getValues(T data) {
        if (data == null) {
            throw ValidationException.of("compare data can not be null");
        }
        Map<String, JavaField> getters = BeanUtil.getJavaField(data.getClass());
        Map<String, Object> values = new HashMap<>();
        for (JavaField fieldInfo : getters.values()) {
            try {
                Method getter = fieldInfo.getGetter();
                Object value = getter.invoke(data);
                values.put(fieldInfo.getFieldName(), value);
            } catch (Exception e) {
                log.error("invoke getter error: {}", e.getMessage());
            }
        }
        return values;
    }

}
