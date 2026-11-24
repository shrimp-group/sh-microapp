package com.wkclz.micro.dag.api;

import com.wkclz.micro.dag.utils.JsonUtils;
import lombok.Getter;

import java.util.Map;
import java.util.function.BooleanSupplier;

/**
 * 节点执行上下文（由 DagWorkerExecutor 构建传入）
 */
public class NodeContext {

    /** 过程日志缓冲默认上限（字符数） */
    private static final int DEFAULT_MAX_LOG_CHARS = 8000;

    @Getter
    private final String instanceCode;
    @Getter
    private final String nodeCode;
    @Getter
    private final String traceCode;
    @Getter
    private final int attempt;
    @Getter
    private final String inputJson;
    private final BooleanSupplier interruptedCheck;
    /** 过程日志缓冲上限（超出后丢弃后续内容并置 truncated） */
    private final int maxLogChars;

    private final StringBuilder logBuffer = new StringBuilder();
    /** 累计已接收的日志字符数（含已 drain 与超限丢弃的） */
    private int totalLogChars = 0;
    private boolean truncated = false;

    public NodeContext(String instanceCode, String nodeCode, String traceCode,
                       int attempt, String inputJson, BooleanSupplier interruptedCheck) {
        this(instanceCode, nodeCode, traceCode, attempt, inputJson, interruptedCheck, DEFAULT_MAX_LOG_CHARS);
    }

    public NodeContext(String instanceCode, String nodeCode, String traceCode,
                       int attempt, String inputJson, BooleanSupplier interruptedCheck, int maxLogChars) {
        this.instanceCode = instanceCode;
        this.nodeCode = nodeCode;
        this.traceCode = traceCode;
        this.attempt = attempt;
        this.inputJson = inputJson;
        this.interruptedCheck = interruptedCheck;
        this.maxLogChars = maxLogChars;
    }

    /** 节点参数解析为 Map（input_params_json），解析失败返回空 Map */
    public Map<String, Object> inputParams() {
        return JsonUtils.parseMapQuiet(inputJson);
    }

    /** 协作式中断检查点：命中抛 InterruptedException，由引擎转为 MANUAL_STOPPED */
    public void checkInterrupted() throws InterruptedException {
        if (interruptedCheck.getAsBoolean()) {
            throw new InterruptedException("node interrupted: " + nodeCode);
        }
    }

    /**
     * 过程日志缓冲（线程安全，纯内存，由 worker 定时落库）：
     * 累计字符超 maxLogChars 后直接丢弃不再缓冲并置 truncated 标志
     */
    public synchronized void log(String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        totalLogChars += message.length();
        if (truncated || totalLogChars > maxLogChars) {
            truncated = true;
            return;
        }
        if (logBuffer.length() > 0) {
            logBuffer.append('\n');
        }
        logBuffer.append(message);
    }

    /** 原子取走全部未落库内容（按行拼接，带换行）；返回 null 表示无内容 */
    public synchronized String drainLog() {
        if (logBuffer.length() == 0) {
            return null;
        }
        String content = logBuffer.toString();
        logBuffer.setLength(0);
        return content;
    }

    /** 累计已接收的日志字符数（含已 drain 与超限丢弃的） */
    public synchronized int logCharCount() {
        return totalLogChars;
    }

    /** 过程日志是否已截断（超限后置位，兜底落库时补截断标记） */
    public synchronized boolean isTruncated() {
        return truncated;
    }
}
