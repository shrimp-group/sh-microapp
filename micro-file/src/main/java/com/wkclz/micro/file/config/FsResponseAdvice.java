//
//
//package com.wkclz.micro.file.config;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.node.ObjectNode;
//import com.fasterxml.jackson.databind.node.TextNode;
//import com.wkclz.micro.fs.api.FsApi;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.core.MethodParameter;
//import org.springframework.http.MediaType;
//import org.springframework.http.server.ServerHttpRequest;
//import org.springframework.http.server.ServerHttpResponse;
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
//
//import java.lang.reflect.Field;
//import java.util.Collection;
//import java.util.Date;
//import java.util.Iterator;
//
//@Slf4j
//@ControllerAdvice
//public class FsResponseAdvice implements ResponseBodyAdvice<Object> {
//
//    @Autowired
//    private FsApi fsApi;
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Override
//    public boolean supports(MethodParameter returnType, Class converterType) {
//        // 这里可以根据需要实现特定的条件判断
//        return true;
//    }
//
//    @Override
//    public Object beforeBodyWrite(
//        Object body,
//        MethodParameter returnType,
//        MediaType selectedContentType,
//        Class selectedConverterType,
//        ServerHttpRequest request,
//        ServerHttpResponse response) {
//
//        // return body; // 直接返回，交予默认的 ObjectMapper 进行序列化
//        try {
//            // 使用默认的 ObjectMapper 进行序列化
//            JsonNode rootNode = objectMapper.valueToTree(body);
//            // 对特定路径的字段进行脱敏处理
//            maskFields(rootNode);
//            return rootNode;
//        } catch (Exception e) {
//            log.error(e.getMessage(), e);
//            // 如果处理失败，返回原始响应
//            return body;
//        }
//    }
//
//
//    private static String pathExp = "imageUrl";
//    private void maskFields(JsonNode node) {
//        if (node.isArray()) {
//            node.forEach(this::maskFields);
//            return;
//        }
//        if (node.isObject()) {
//            ObjectNode objectNode = (ObjectNode) node;
//            Iterator<String> it = objectNode.fieldNames();
//            while (it.hasNext()) {
//                String fieldName = it.next();
//                JsonNode fieldNode = objectNode.get(fieldName);
//                if (fieldNode.isTextual() && fieldName.matches(pathExp)) {
//                    String text = fieldNode.asText();
//                    objectNode.put(fieldName, "aa:" + text);
//                    objectNode.put(fieldName + "Mask", "aa:" + text);
//                } else {
//                    maskFields(fieldNode);
//                }
//            }
//        }
//    }
//
//    private void changeBody(JsonNode jsonNode) {
//        if (jsonNode == null) {
//            return;
//        }
//        try {
//            JsonNode at = jsonNode.at("/data/rows/0/imageUrl");
//            if (at instanceof TextNode tn) {
//                String text = tn.asText();
//                // 转换
//                String preview = fsApi.sign(text);
//                System.out.println("找到了字段：" +preview);
//            }
//            System.out.println(at);
//        } catch (Exception e) {
//            log.error("changeBody error: {}", e.getMessage());
//        }
//    }
//    private void changeBody(Object body) {
//        // 基本类型，不处理。自身已经是字符串了，不处理
//        if (body == null || isPrimitive(body) || body instanceof String) {
//            return;
//        }
//
//        // 集合，处理每一条数据
//        if (body instanceof Collection<?> collection) {
//            for (Object o : collection) {
//                changeBody(o);
//            }
//            return;
//        }
//
//        Field[] fields = body.getClass().getDeclaredFields();
//        for (Field field : fields) {
//            Object fieldValue;
//            try {
//                field.setAccessible(true);
//                fieldValue = field.get(body);
//            } catch (IllegalAccessException e) {
//                continue;
//            }
//
//            // 基本类型，不处理
//            if (fieldValue == null || isPrimitive(fieldValue)) {
//                continue;
//            }
//
//            FsSign fsSign = field.getAnnotation(FsSign.class);
//            if (fsSign == null) {
//                changeBody(fieldValue);
//            }
//            if (fsSign != null && fieldValue instanceof String str) {
//                // 转换
//                String preview = fsApi.sign(str);
//                System.out.println("找到了字段：" +preview);
//            }
//        }
//    }
//    private static boolean isPrimitive(Object value) {
//        if (value instanceof Integer
//            || value instanceof Double
//            || value instanceof Float
//            || value instanceof Long
//            || value instanceof Short
//            || value instanceof Byte
//            || value instanceof Character
//            || value instanceof Boolean
//            || value instanceof Date) {
//            return true;
//        }
//        return false;
//    }
//
//}
