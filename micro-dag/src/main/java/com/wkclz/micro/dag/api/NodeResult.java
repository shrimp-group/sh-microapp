package com.wkclz.micro.dag.api;

import java.util.Map;

/**
 * 节点执行结果：output 写入 output_json；traceCode 非空时回填 dag_instance.trace_code。
 * ok()/fail() 显式区分成功与失败语义，fail 结果由执行器派发 FAIL 事件
 */
public class NodeResult {

    private final boolean success;
    private final Map<String, Object> output;
    private final String traceCode;

    private NodeResult(boolean success, Map<String, Object> output, String traceCode) {
        this.success = success;
        this.output = output;
        this.traceCode = traceCode;
    }

    public static NodeResult ok() {
        return new NodeResult(true, null, null);
    }

    public static NodeResult ok(Map<String, Object> output) {
        return new NodeResult(true, output, null);
    }

    /** 显式失败（无 output） */
    public static NodeResult fail() {
        return new NodeResult(false, null, null);
    }

    /** 显式失败（output 仍写入 output_json；含 msg 键时作为失败消息） */
    public static NodeResult fail(Map<String, Object> output) {
        return new NodeResult(false, output, null);
    }

    public NodeResult withTraceCode(String traceCode) {
        return new NodeResult(this.success, this.output, traceCode);
    }

    public boolean isSuccess() { return success; }
    public Map<String, Object> getOutput() { return output; }
    public String getTraceCode() { return traceCode; }
}
