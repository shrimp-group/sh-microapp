package com.wkclz.micro.dag.api;

import java.util.Map;

/**
 * 节点执行结果：output 写入 output_json；traceCode 非空时回填 dag_instance.trace_code
 */
public class NodeResult {

    private final Map<String, Object> output;
    private final String traceCode;

    private NodeResult(Map<String, Object> output, String traceCode) {
        this.output = output;
        this.traceCode = traceCode;
    }

    public static NodeResult ok() {
        return new NodeResult(null, null);
    }

    public static NodeResult ok(Map<String, Object> output) {
        return new NodeResult(output, null);
    }

    public NodeResult withTraceCode(String traceCode) {
        return new NodeResult(this.output, traceCode);
    }

    public Map<String, Object> getOutput() { return output; }
    public String getTraceCode() { return traceCode; }
}
