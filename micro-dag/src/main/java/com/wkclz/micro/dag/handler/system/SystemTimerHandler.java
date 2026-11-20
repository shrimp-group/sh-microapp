package com.wkclz.micro.dag.handler.system;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.dag.api.NodeContext;
import com.wkclz.micro.dag.api.NodeHandler;
import com.wkclz.micro.dag.api.NodeResult;
import com.wkclz.micro.dag.bean.dto.NodeParamDef;
import com.wkclz.micro.dag.bean.enums.NodeCategory;
import com.wkclz.micro.dag.bean.enums.NodeParamType;
import com.wkclz.micro.dag.bean.enums.SystemHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/**
 * system-timer 定时器节点：按 delayMs / delayUntil 等待到点后成功
 * 分片 sleep（200ms 步长）+ 协作式中断检查；等待时长以 nanoTime 单调时钟计量，不受系统时钟抖动影响
 */
@Slf4j
@Component
public class SystemTimerHandler implements NodeHandler {

    /** 上限 24h */
    private static final long MAX_DELAY_MS = 86400000L;
    private static final long STEP_MS = 200;

    @Override
    public String nodeCode() {
        return SystemHandler.TIMER.getCode();
    }

    @Override
    public String nodeName() {
        return "系统-定时器";
    }

    @Override
    public NodeCategory category() {
        return NodeCategory.SYSTEM;
    }

    @Override
    public NodeResult execute(NodeContext ctx) throws InterruptedException {
        Map<String, Object> params = ctx.inputParams();
        log.info("system-timer: instance={} node={} attempt={} params={}",
            ctx.getInstanceCode(), ctx.getNodeCode(), ctx.getAttempt(), params);
        long delayMs = resolveDelayMs(params.get("delayMs"));
        long waitMs = delayMs;
        LocalDateTime target = LocalDateTime.now().plus(delayMs, ChronoUnit.MILLIS);
        if (params.get("delayUntil") != null) {
            LocalDateTime until = resolveDelayUntil(params.get("delayUntil").toString());
            if (until.isAfter(target)) {
                target = until;
                waitMs = Math.max(waitMs, Duration.between(LocalDateTime.now(), until).toMillis());
            }
        }
        long startNanos = System.nanoTime();
        long deadlineNanos = startNanos + Math.max(waitMs, 0) * 1_000_000L;
        while (true) {
            long remainNanos = deadlineNanos - System.nanoTime();
            if (remainNanos <= 0) {
                break;
            }
            long stepNanos = Math.min(STEP_MS * 1_000_000L, remainNanos);
            Thread.sleep(stepNanos / 1_000_000L, (int) (stepNanos % 1_000_000L));
            ctx.checkInterrupted();
        }
        long waitedMs = waitMs <= 0 ? 0 : Math.min((System.nanoTime() - startNanos) / 1_000_000L, waitMs);
        log.info("system-timer: waited={}ms target={}", waitedMs, target);
        return NodeResult.ok(Map.of("waitedMs", waitedMs, "targetTime", target.toString()));
    }

    @Override
    public List<NodeParamDef> paramSchema() {
        return List.of(
            NodeParamDef.of("delayMs", "延时(ms)", NodeParamType.NUMBER)
                .range(0L, MAX_DELAY_MS).help("与到点时间二选一，取较晚者"),
            NodeParamDef.of("delayUntil", "到点时间", NodeParamType.DATETIME)
                .placeholder("ISO-8601，如 2026-11-19T12:00:00")
        );
    }

    /** 解析 delayMs：缺省 0，上限 86400000，非法值抛校验异常 */
    long resolveDelayMs(Object v) {
        if (v == null) {
            return 0;
        }
        long ms;
        try {
            ms = v instanceof Number n ? n.longValue() : Long.parseLong(v.toString().trim());
        } catch (NumberFormatException e) {
            log.error("system-timer: delayMs 参数非法 value={}", v, e);
            throw ValidationException.of("system-timer delayMs 参数非法: " + v);
        }
        return Math.min(Math.max(ms, 0), MAX_DELAY_MS);
    }

    /** 解析 delayUntil：先 LocalDateTime.parse，失败再 Instant.parse（转系统时区），仍失败抛校验异常 */
    LocalDateTime resolveDelayUntil(String v) {
        try {
            return LocalDateTime.parse(v.trim());
        } catch (Exception ignored) {
            // 尝试 Instant 解析
        }
        try {
            return LocalDateTime.ofInstant(Instant.parse(v.trim()), ZoneId.systemDefault());
        } catch (Exception e) {
            log.error("system-timer: delayUntil 参数非法 value={}", v, e);
            throw ValidationException.of("system-timer delayUntil 参数非法（需 ISO-8601 时间）: " + v);
        }
    }
}
