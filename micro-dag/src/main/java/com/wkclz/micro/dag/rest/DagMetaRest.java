package com.wkclz.micro.dag.rest;

import com.wkclz.core.base.R;
import com.wkclz.micro.dag.api.NodeHandlerRegistry;
import com.wkclz.micro.dag.bean.req.ExpressionPreviewReq;
import com.wkclz.micro.dag.bean.resp.ExpressionPreviewResp;
import com.wkclz.micro.dag.bean.resp.HandlerInfoResp;
import com.wkclz.micro.dag.core.DagExpressionEvaluator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "DAG-元能力", description = "DAG 节点 handler 与表达式试算")
@Slf4j
@RestController
@RequestMapping(Route.PREFIX)
public class DagMetaRest {

    @Resource
    private NodeHandlerRegistry registry;
    @Resource
    private DagExpressionEvaluator evaluator;

    @Operation(summary = "查询已注册节点 handler 列表")
    @GetMapping(Route.HANDLER_LIST)
    public R<List<HandlerInfoResp>> handlerList() {
        log.info("micro-dag: 查询已注册 handler 列表");
        List<HandlerInfoResp> list = registry.all().stream()
            .map(h -> {
                HandlerInfoResp resp = new HandlerInfoResp();
                resp.setCode(h.nodeCode());
                resp.setName(h.nodeName());
                resp.setCategory(h.category().name());
                resp.setParamSchema(h.paramSchema());
                return resp;
            })
            .toList();
        return R.ok(list);
    }

    @Operation(summary = "表达式试算")
    @PostMapping(Route.EXPRESSION_PREVIEW)
    public R<ExpressionPreviewResp> expressionPreview(@RequestBody ExpressionPreviewReq req) {
        log.info("micro-dag: 表达式试算 expr={}", req.getExpression());
        Map<String, Object> ctx = req.getCtx() == null ? Map.of() : req.getCtx();
        Map<String, Map<String, Object>> nodeOutputs = req.getNodeOutputs() == null ? Map.of() : req.getNodeOutputs();
        ExpressionPreviewResp resp = new ExpressionPreviewResp();
        try {
            boolean result = evaluator.evaluate(req.getExpression(), ctx, nodeOutputs);
            resp.setParseOk(true);
            resp.setResult(result);
        } catch (Exception e) {
            log.warn("micro-dag: 表达式解析失败 expr={}", req.getExpression(), e);
            resp.setParseOk(false);
            resp.setErrorMsg(e.getMessage());
        }
        return R.ok(resp);
    }
}
