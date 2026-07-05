package com.wkclz.auto.executor;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.wkclz.auto.bean.ApiInfo;
import com.wkclz.auto.bean.ApiParamInfo;
import com.wkclz.auto.bean.TestCaseResult;
import com.wkclz.auto.bean.TestReport;
import com.wkclz.auto.mock.MockHelper;
import com.wkclz.auto.mock.TestDataGenerator;
import com.wkclz.auto.scanner.ApiScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class TestExecutor {

    private static final Logger logger = LoggerFactory.getLogger(TestExecutor.class);

    @Autowired
    private ApiScanner apiScanner;

    @Autowired
    private MockHelper mockHelper;

    @Autowired
    private WebApplicationContext webApplicationContext;

    public TestReport execute() {
        return execute(null);
    }

    public TestReport execute(String packagePath) {
        List<ApiInfo> apiInfos = packagePath != null ? apiScanner.scan(packagePath) : apiScanner.scan();
        return executeApis(apiInfos);
    }

    public TestReport executeApis(List<ApiInfo> apiInfos) {
        TestReport report = new TestReport();
        report.setStartTime(LocalDateTime.now());

        List<TestCaseResult> results = new ArrayList<>();
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        for (ApiInfo apiInfo : apiInfos) {
            TestCaseResult result = executeApi(mockMvc, apiInfo);
            results.add(result);
        }

        report.setEndTime(LocalDateTime.now());
        report.setResults(results);
        report.setTotalApiCount(results.size());
        report.setSuccessCount((int) results.stream().filter(TestCaseResult::isSuccess).count());
        report.setFailCount((int) results.stream().filter(r -> !r.isSuccess() && r.getHttpStatus() >= 400 && r.getHttpStatus() < 500).count());
        report.setErrorCount((int) results.stream().filter(r -> !r.isSuccess() && r.getHttpStatus() >= 500).count());

        long totalTime = 0;
        for (TestCaseResult r : results) {
            totalTime += r.getCostTimeMs();
        }
        report.setTotalCostTimeMs(totalTime);

        return report;
    }

    private TestCaseResult executeApi(MockMvc mockMvc, ApiInfo apiInfo) {
        TestCaseResult result = new TestCaseResult();
        result.setUri(apiInfo.getUri());
        result.setMethod(apiInfo.getMethod());
        result.setDesc(apiInfo.getDesc());
        result.setModule(apiInfo.getModule());

        try {
            mockHelper.mockControllerDependencies(apiInfo.getControllerClass());

            MockHttpServletRequestBuilder requestBuilder = buildRequest(apiInfo, result);
            long startTime = System.currentTimeMillis();

            MockHttpServletResponse response = mockMvc.perform(requestBuilder).andReturn().getResponse();
            long costTime = System.currentTimeMillis() - startTime;

            result.setHttpStatus(response.getStatus());
            result.setCostTimeMs(costTime);
            result.setResponseBody(response.getContentAsString());
            result.setSuccess(response.getStatus() >= 200 && response.getStatus() < 300);

        } catch (Exception e) {
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
            result.setCostTimeMs(0);
            logger.error("test failed: {} {}", apiInfo.getMethod(), apiInfo.getUri(), e);
        } finally {
            mockHelper.resetAll();
        }

        return result;
    }

    private MockHttpServletRequestBuilder buildRequest(ApiInfo apiInfo, TestCaseResult result) {
        String method = apiInfo.getMethod();
        String uri = apiInfo.getUri();

        MockHttpServletRequestBuilder builder;
        switch (method) {
            case "POST":
                builder = org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(uri);
                break;
            case "PUT":
                builder = org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put(uri);
                break;
            case "DELETE":
                builder = org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete(uri);
                break;
            default:
                builder = org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(uri);
                break;
        }

        builder.contentType("application/json;charset=UTF-8");

        if (apiInfo.getParams() != null) {
            for (ApiParamInfo paramInfo : apiInfo.getParams()) {
                Object paramValue = mockHelper.generateParamValue(paramInfo);

                if (paramInfo.isRequestBody()) {
                    String jsonBody = JSON.toJSONString(paramValue, JSONWriter.Feature.WriteNulls);
                    builder.content(jsonBody);
                    result.setRequestBody(jsonBody);
                } else if (paramInfo.isPathVariable()) {
                } else if (paramInfo.isRequestParam()) {
                    if (paramValue != null) {
                        builder.param(paramInfo.getName(), paramValue.toString());
                    }
                } else {
                    if (method.equals("GET")) {
                        populateGetParams(builder, paramInfo.getType(), paramInfo.getName());
                    } else {
                        String jsonBody = JSON.toJSONString(paramValue, JSONWriter.Feature.WriteNulls);
                        builder.content(jsonBody);
                        result.setRequestBody(jsonBody);
                    }
                }
            }
        }

        return builder;
    }

    private void populateGetParams(MockHttpServletRequestBuilder builder, Class<?> type, String prefix) {
        populateGetParams(builder, type, prefix, 0);
    }

    private void populateGetParams(MockHttpServletRequestBuilder builder, Class<?> type, String prefix, int depth) {
        if (type == null || depth > 3) {
            return;
        }

        if (type.isPrimitive() || type == String.class || type == Integer.class || type == Long.class
            || type == Boolean.class || type == Double.class || type == Float.class
            || type == java.time.LocalDateTime.class || type == java.time.LocalDate.class
            || type == java.time.LocalTime.class || type == java.util.Date.class
            || type == java.math.BigDecimal.class || type == java.math.BigInteger.class
            || type.isEnum()) {
            Object value = TestDataGenerator.generate(type);
            if (value != null) {
                builder.param(prefix, value.toString());
            }
            return;
        }

        if (java.util.Collection.class.isAssignableFrom(type) || type.isArray()) {
            return;
        }

        java.lang.reflect.Field[] fields = type.getDeclaredFields();
        for (java.lang.reflect.Field field : fields) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) || java.lang.reflect.Modifier.isFinal(field.getModifiers())) {
                continue;
            }
            String paramName = prefix.isEmpty() ? field.getName() : prefix + "." + field.getName();
            populateGetParams(builder, field.getType(), paramName, depth + 1);
        }
    }
}
