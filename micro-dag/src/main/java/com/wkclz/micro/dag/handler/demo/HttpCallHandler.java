package com.wkclz.micro.dag.handler.demo;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.dag.api.NodeContext;
import com.wkclz.micro.dag.api.NodeHandler;
import com.wkclz.micro.dag.api.NodeResult;
import com.wkclz.micro.dag.bean.enums.NodeCategory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * demo-HTTP 调用：按入参发起 HTTP 请求并返回状态码与响应摘要，演示外部调用节点
 * 入参：url（必填）、method（默认 GET）、headers（可选 Map）、body（POST/PUT/PATCH 可选）
 */
@Slf4j
public class HttpCallHandler implements NodeHandler {

    private static final int MAX_BODY_LEN = 2000;
    private static final Set<String> BODY_METHODS = Set.of("POST", "PUT", "PATCH");
    private static final Set<String> ALLOWED_METHODS = Set.of("GET", "POST", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS");

    @Override
    public String nodeCode() {
        return "demo-http-call";
    }

    @Override
    public String nodeName() {
        return "演示-HTTP调用";
    }

    @Override
    public NodeCategory category() {
        return NodeCategory.DEMO;
    }

    @Override
    public NodeResult execute(NodeContext ctx) {
        Map<String, Object> params = ctx.inputParams();
        log.info("demo-http-call: instance={} node={} params={}", ctx.getInstanceCode(), ctx.getNodeCode(), params);
        String url = params.get("url") == null ? null : params.get("url").toString().trim();
        if (url == null || url.isBlank()) {
            throw ValidationException.of("demo-http-call 缺少 url 参数");
        }
        HttpMethod httpMethod = resolveMethod(params.getOrDefault("method", "GET").toString());

        RestClient.RequestBodySpec spec = RestClient.create()
            .method(httpMethod)
            .uri(URI.create(url));
        if (params.get("headers") instanceof Map<?, ?> headers) {
            headers.forEach((k, v) -> spec.header(String.valueOf(k), String.valueOf(v)));
        }
        if (BODY_METHODS.contains(httpMethod.name()) && params.get("body") != null) {
            spec.body(params.get("body"));
        }
        ResponseEntity<String> resp = spec.retrieve()
            .onStatus(code -> true, (req, res) -> { })
            .toEntity(String.class);

        Map<String, Object> output = new HashMap<>();
        output.put("status", resp.getStatusCode().value());
        output.put("body", truncate(resp.getBody()));
        log.info("demo-http-call: url={} status={}", url, resp.getStatusCode().value());
        return NodeResult.ok(output);
    }

    private HttpMethod resolveMethod(String method) {
        String m = method == null ? "GET" : method.trim().toUpperCase();
        if (!ALLOWED_METHODS.contains(m)) {
            log.error("demo-http-call: method 参数非法 method={}", m);
            throw ValidationException.of("demo-http-call method 参数非法: " + m);
        }
        return HttpMethod.valueOf(m);
    }

    private String truncate(String body) {
        if (body == null) {
            return "";
        }
        return body.length() > MAX_BODY_LEN ? body.substring(0, MAX_BODY_LEN) : body;
    }
}
