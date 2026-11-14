package com.wkclz.micro.dag.api;

import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.function.BooleanSupplier;

/**
 * 节点执行上下文（由 DagWorkerExecutor 构建传入）
 */
public class NodeContext {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final String instanceCode;
    private final String nodeCode;
    private final String traceCode;
    private final int attempt;
    private final String inputJson;
    private final BooleanSupplier interruptedCheck;

    public NodeContext(String instanceCode, String nodeCode, String traceCode,
                       int attempt, String inputJson, BooleanSupplier interruptedCheck) {
        this.instanceCode = instanceCode;
        this.nodeCode = nodeCode;
        this.traceCode = traceCode;
        this.attempt = attempt;
        this.inputJson = inputJson;
        this.interruptedCheck = interruptedCheck;
    }

    public String getInstanceCode() { return instanceCode; }
    public String getNodeCode() { return nodeCode; }
    public String getTraceCode() { return traceCode; }
    public int getAttempt() { return attempt; }
    public String getInputJson() { return inputJson; }

    /** 节点参数解析为 Map（input_params_json），解析失败返回空 Map */
    @SuppressWarnings("unchecked")
    public Map<String, Object> inputParams() {
        if (inputJson == null || inputJson.isBlank()) {
            return Map.of();
        }
        try {
            return OBJECT_MAPPER.readValue(inputJson, Map.class);
        } catch (Exception e) {
            return Map.of();
        }
    }

    /** 协作式中断检查点：命中抛 InterruptedException，由引擎转为 MANUAL_STOPPED */
    public void checkInterrupted() throws InterruptedException {
        if (interruptedCheck.getAsBoolean()) {
            throw new InterruptedException("node interrupted: " + nodeCode);
        }
    }
}
