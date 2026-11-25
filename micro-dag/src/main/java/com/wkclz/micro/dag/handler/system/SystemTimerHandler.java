package com.wkclz.micro.dag.handler.system;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.dag.api.NodeContext;
import com.wkclz.micro.dag.api.NodeHandler;
import com.wkclz.micro.dag.api.NodeResult;
import com.wkclz.micro.dag.bean.constant.DagConstants;
import com.wkclz.micro.dag.bean.dto.NodeParamDef;
import com.wkclz.micro.dag.bean.entity.DagNodeInstance;
import com.wkclz.micro.dag.bean.enums.NodeCategory;
import com.wkclz.micro.dag.bean.enums.NodeParamType;
import com.wkclz.micro.dag.bean.enums.NodeStatus;
import com.wkclz.micro.dag.bean.enums.SystemHandler;
import com.wkclz.micro.dag.mapper.DagNodeInstanceMapper;
import com.wkclz.micro.dag.utils.ParamUtils;
import com.wkclz.micro.dag.utils.TimeUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * system-timer 定时器节点：触发前校验直接上游完成状态（SUCCESS/SKIPPED 视为完成），
 * 按 delayMs / delayUntil 等待到点后成功；到点时间具 24h 有效期——24h 内错过立即执行，超期置失败等人工干预
 * 分片 sleep（200ms 步长）+ 协作式中断检查；等待时长以 nanoTime 单调时钟计量，不受系统时钟抖动影响
 */
@Slf4j
@Component
public class SystemTimerHandler implements NodeHandler {

    /** 上限 24h（兼作到点有效期窗口） */
    private static final long MAX_DELAY_MS = 86400000L;
    private static final long STEP_MS = 200;

    @Resource
    private DagNodeInstanceMapper nodeInstanceMapper;

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
        // 触发前上游校验：SUCCESS/SKIPPED 视为完成，存在未完成上游置失败等人工干预（分支级联 SKIPPED 的定时器不会被领取，无需在此处理）
        NodeResult upstreamFail = checkUpstreams(ctx, params);
        if (upstreamFail != null) {
            return upstreamFail;
        }
        long delayMs = resolveDelayMs(params.get("delayMs"));
        long waitMs = delayMs;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime target = now.plus(delayMs, ChronoUnit.MILLIS);
        if (params.get("delayUntil") != null) {
            LocalDateTime until = resolveDelayUntil(params.get("delayUntil").toString());
            // 到点有效期：绝对到点错过超 24h 置失败等人工干预；24h 内错过走现有等待循环自然立即成功（waitedMs=0）
            if (until.isBefore(now.minus(Duration.ofMillis(MAX_DELAY_MS)))) {
                log.warn("system-timer: 定时任务已过期超过 1 天 instance={} node={} target={}",
                    ctx.getInstanceCode(), ctx.getNodeCode(), until);
                return NodeResult.fail(Map.of("msg", "定时任务已过期超过 1 天: target=" + until));
            }
            if (until.isAfter(target)) {
                target = until;
                waitMs = Math.max(waitMs, Duration.between(now, until).toMillis());
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

    /** 上游校验：SUCCESS/SKIPPED 视为完成（手动跳过算成功）；存在未完成状态上游返回 fail 结果（不进入等待），全部完成返回 null */
    private NodeResult checkUpstreams(NodeContext ctx, Map<String, Object> params) {
        Object raw = params.get(DagConstants.SNAPSHOT_KEY_UPSTREAMS);
        if (!(raw instanceof List<?> upstreams) || upstreams.isEmpty()) {
            return null;
        }
        Map<String, String> statusMap = nodeInstanceMapper.listByInstanceCode(ctx.getInstanceCode()).stream()
            .collect(Collectors.toMap(DagNodeInstance::getNodeCode, DagNodeInstance::getStatus, (a, b) -> a));
        String unfinished = upstreams.stream()
            .map(String::valueOf)
            .filter(code -> !NodeStatus.SUCCESS.name().equals(statusMap.get(code))
                && !NodeStatus.SKIPPED.name().equals(statusMap.get(code)))
            .map(code -> code + "=" + statusMap.get(code))
            .collect(Collectors.joining(", "));
        if (unfinished.isEmpty()) {
            return null;
        }
        log.warn("system-timer: 上游节点未完成 instance={} node={} unfinished={}",
            ctx.getInstanceCode(), ctx.getNodeCode(), unfinished);
        return NodeResult.fail(Map.of("msg", "上游节点未完成: " + unfinished));
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
        long ms = ParamUtils.parseLongParam(v, "system-timer delayMs");
        return Math.min(Math.max(ms, 0), MAX_DELAY_MS);
    }

    /** 解析 delayUntil：先 LocalDateTime.parse，失败再 Instant.parse（转系统时区），仍失败抛校验异常 */
    LocalDateTime resolveDelayUntil(String v) {
        try {
            return TimeUtils.parseFlexibleDateTime(v.trim());
        } catch (Exception e) {
            // 参数校验类可容忍失败，降级 warn
            log.warn("system-timer: delayUntil 参数非法 value={}", v, e);
            throw ValidationException.of("system-timer delayUntil 参数非法（需 ISO-8601 时间）: " + v);
        }
    }
}
