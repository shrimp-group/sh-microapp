package com.wkclz.micro.dag.handler.demo;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.dag.api.NodeContext;
import com.wkclz.micro.dag.api.NodeHandler;
import com.wkclz.micro.dag.api.NodeResult;
import com.wkclz.micro.dag.bean.enums.NodeCategory;
import com.wkclz.micro.dag.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

import java.util.Map;

/**
 * demo-JS 脚本：用 GraalJS 执行入参 script 中定义的 handle(input) 函数，演示动态逻辑节点
 * 入参：script（必填，需定义 function handle(input)）；input（可选，缺省为全部节点入参）
 * 返回对象时原样输出，返回标量时包装为 {"result": 标量}
 */
@Slf4j
public class ScriptExecHandler implements NodeHandler {

    @Override
    public String nodeCode() {
        return "demo-script-exec";
    }

    @Override
    public String nodeName() {
        return "演示-JS脚本";
    }

    @Override
    public NodeCategory category() {
        return NodeCategory.DEMO;
    }

    @Override
    public NodeResult execute(NodeContext ctx) {
        Map<String, Object> params = ctx.inputParams();
        log.info("demo-script-exec: instance={} node={} attempt={}",
            ctx.getInstanceCode(), ctx.getNodeCode(), ctx.getAttempt());
        Object script = params.get("script");
        if (!(script instanceof String s) || s.isBlank()) {
            throw ValidationException.of("demo-script-exec 缺少 script 参数");
        }
        try (Context context = Context.newBuilder("js").allowAllAccess(true).build()) {
            context.eval("js", s);
            Value handle = context.getBindings("js").getMember("handle");
            if (handle == null || !handle.canExecute()) {
                throw ValidationException.of("demo-script-exec script 未定义可执行函数 handle(input)");
            }
            context.getBindings("js").putMember("__input__", params.containsKey("input") ? params.get("input") : params);
            Value json = context.eval("js", "JSON.stringify(handle(__input__))");
            if (!json.isString()) {
                throw ValidationException.of("demo-script-exec handle 返回值不可序列化");
            }
            Object parsed = JsonUtils.readValue(json.asString(), Object.class);
            ctx.log("脚本执行完成");
            if (parsed instanceof Map<?, ?> m) {
                @SuppressWarnings("unchecked")
                Map<String, Object> output = (Map<String, Object>) m;
                return NodeResult.ok(output);
            }
            return NodeResult.ok(Map.of("result", parsed));
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("demo-script-exec: 脚本执行失败 node={}", ctx.getNodeCode(), e);
            throw ValidationException.of("demo-script-exec 脚本执行失败: " + e.getMessage());
        }
    }
}
