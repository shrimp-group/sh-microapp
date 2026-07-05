package com.wkclz.auto.mock;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TestDataGenerator {

    private static final int MAX_DEPTH = 3;
    private static final Set<String> SKIP_FIELDS = Set.of("id", "version", "deleted", "serialVersionUID");

    public static Object generate(Class<?> type) {
        return generate(type, 0);
    }

    public static Object generate(Class<?> type, int depth) {
        if (depth > MAX_DEPTH) {
            return null;
        }

        if (type == null) {
            return null;
        }

        if (type == boolean.class || type == Boolean.class) return true;
        if (type == byte.class || type == Byte.class) return (byte) 1;
        if (type == short.class || type == Short.class) return (short) 1;
        if (type == int.class || type == Integer.class) return 1;
        if (type == long.class || type == Long.class) return 1L;
        if (type == float.class || type == Float.class) return 1.0f;
        if (type == double.class || type == Double.class) return 1.0;
        if (type == char.class || type == Character.class) return 'a';
        if (type == String.class) return "";
        if (type == LocalDateTime.class) return LocalDateTime.now();
        if (type == LocalDate.class) return LocalDate.now();
        if (type == LocalTime.class) return LocalTime.now();
        if (type == Date.class) return new Date();
        if (type == Object.class) return new HashMap<>();

        if (type.isEnum()) {
            Object[] enumConstants = type.getEnumConstants();
            return enumConstants.length > 0 ? enumConstants[0] : null;
        }

        if (type.isArray()) {
            Class<?> componentType = type.getComponentType();
            return java.lang.reflect.Array.newInstance(componentType, 0);
        }

        if (List.class.isAssignableFrom(type)) {
            return new ArrayList<>();
        }

        if (Set.class.isAssignableFrom(type)) {
            return new HashSet<>();
        }

        if (Map.class.isAssignableFrom(type)) {
            return new HashMap<>();
        }

        if (type.isInterface() || Modifier.isAbstract(type.getModifiers())) {
            return null;
        }

        try {
            Constructor<?> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            Object instance = constructor.newInstance();

            Field[] fields = type.getDeclaredFields();
            for (Field field : fields) {
                if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
                    continue;
                }
                if (SKIP_FIELDS.contains(field.getName())) {
                    continue;
                }

                field.setAccessible(true);
                Object value = generate(field.getType(), depth + 1);
                if (value != null) {
                    field.set(instance, value);
                }
            }

            return instance;
        } catch (Exception e) {
            return null;
        }
    }

    public static List<Object> generateForParams(List<Class<?>> paramTypes) {
        List<Object> params = new ArrayList<>();
        for (Class<?> type : paramTypes) {
            params.add(generate(type));
        }
        return params;
    }
}
