package com.wkclz.micro.dag.api;

import java.util.Map;

/**
 * 节点执行结果：output 写入 output_json；traceCode 非空时回填 dag_instance.trace_code。
 * ok()/fail() 显式区分成功与失败语义，fail 结果由执行器派发 FAIL 事件；
 * notReady() 表示前置未满足，由执行器派发 NOT_READY 并按退避重试
 */
public class NodeResult {

    private final boolean success;
    /** 未就绪标记：节点已执行但前置条件未满足 */
    private final boolean notReady;
    private final Map<String, Object> output;
    private final String traceCode;

    private NodeResult(boolean success, boolean notReady, Map<String, Object> output, String traceCode) {
        this.success = success;
        this.notReady = notReady;
        this.output = output;
        this.traceCode = traceCode;
    }

    public static NodeResult ok() {
        return new NodeResult(true, false, null, null);
    }

    public static NodeResult ok(Map<String, Object> output) {
        return new NodeResult(true, false, output, null);
    }

    /** 显式失败（无 output） */
    public static NodeResult fail() {
        return new NodeResult(false, false, null, null);
    }

    /** 显式失败（output 仍写入 output_json；含 msg 键时作为失败消息） */
    public static NodeResult fail(Map<String, Object> output) {
        return new NodeResult(false, false, output, null);
    }

    /** 未就绪（无 output）：节点已执行但前置条件未满足，由执行器派发 NOT_READY 并按退避重试 */
    public static NodeResult notReady() {
        return new NodeResult(false, true, null, null);
    }

    /** 未就绪（output 仍写入 output_json；含 msg 键时作为未就绪原因） */
    public static NodeResult notReady(Map<String, Object> output) {
        return new NodeResult(false, true, output, null);
    }

    public NodeResult withTraceCode(String traceCode) {
        return new NodeResult(this.success, this.notReady, this.output, traceCode);
    }

    public boolean isSuccess() { return success; }
    public boolean isNotReady() { return notReady; }
    public Map<String, Object> getOutput() { return output; }
    public String getTraceCode() { return traceCode; }
}
