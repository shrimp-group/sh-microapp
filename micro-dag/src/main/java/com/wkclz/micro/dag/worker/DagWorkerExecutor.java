package com.wkclz.micro.dag.worker;

import com.wkclz.micro.dag.api.NodeContext;
import com.wkclz.micro.dag.api.NodeHandler;
import com.wkclz.micro.dag.api.NodeHandlerRegistry;
import com.wkclz.micro.dag.api.NodeResult;
import com.wkclz.micro.dag.bean.entity.DagInstance;
import com.wkclz.micro.dag.bean.entity.DagNodeInstance;
import com.wkclz.micro.dag.bean.enums.ExecutionEvent;
import com.wkclz.micro.dag.cache.DagRedisPublisher;
import com.wkclz.micro.dag.config.DagProperties;
import com.wkclz.micro.dag.mapper.DagNodeInstanceMapper;
import com.wkclz.micro.dag.service.DagEngineService;
import com.wkclz.micro.dag.service.DagExecutionLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 节点执行器：工作线程池执行 handler，监督线程负责超时/租约续期/结果回调。
 * 双线程池设计避免"等待者占满工作线程"饿死：pool 跑 handler，supervisorPool 只做等待与派发。
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "dag", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DagWorkerExecutor {

    private static final long LEASE_MARGIN_MS = 30_000;
    /** 过程日志截断标记（终态兜底落库时追加） */
    private static final String TRUNCATED_MARK = "...[truncated]";

    private final ThreadPoolExecutor pool;
    private final ThreadPoolExecutor supervisorPool;
    private final ScheduledThreadPoolExecutor leasePool;
    private final ScheduledThreadPoolExecutor flushPool;
    /** 运行任务登记表，键为 instanceCode:nodeCode（跨机广播按此键定位本机任务） */
    private final ConcurrentHashMap<String, Future<?>> futures = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Boolean> interruptedFlags = new ConcurrentHashMap<>();
    /** 运行任务的过程日志上下文登记表，键同 futures（flusher 定时 drain 落库） */
    private final ConcurrentHashMap<String, NodeContext> reportCtxs = new ConcurrentHashMap<>();
    private final String workerId = ManagementFactory.getRuntimeMXBean().getName();

    private final NodeHandlerRegistry registry;
    private final DagNodeInstanceMapper nodeMapper;
    private final DagEngineService engine;
    private final DagExecutionLogService logService;
    private final DagRedisPublisher publisher;
    private final DagProperties properties;

    public DagWorkerExecutor(NodeHandlerRegistry registry, DagNodeInstanceMapper nodeMapper,
                             DagEngineService engine, DagExecutionLogService logService,
                             DagRedisPublisher publisher, DagProperties properties) {
        log.info("micro-dag: 初始化节点执行器 worker={}", workerId);
        this.registry = registry;
        this.nodeMapper = nodeMapper;
        this.engine = engine;
        this.logService = logService;
        this.publisher = publisher;
        this.properties = properties;
        int threads = properties.getWorker().getThreads();
        this.pool = new ThreadPoolExecutor(threads, threads, 60, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(), namedFactory("dag-worker"));
        this.supervisorPool = new ThreadPoolExecutor(threads, threads, 60, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(), namedFactory("dag-supervisor"));
        this.leasePool = new ScheduledThreadPoolExecutor(1, namedFactory("dag-lease"));
        this.leasePool.setMaximumPoolSize(1);
        // 独立单线程 flusher：与租约续期隔离，DB 慢不拖累续期
        long flushMs = properties.getLog().getFlushIntervalMs();
        this.flushPool = new ScheduledThreadPoolExecutor(1, namedFactory("dag-log-flusher"));
        this.flushPool.scheduleAtFixedRate(this::flushAll, flushMs, flushMs, TimeUnit.MILLISECONDS);
        log.info("micro-dag: 过程日志 flusher 已启动 intervalMs={}", flushMs);
    }

    /** 调度器 CAS 领取成功后调用（在调度线程返回前仅做提交，不阻塞） */
    public void submit(DagNodeInstance node, DagInstance instance) {
        log.info("micro-dag: 提交执行 instanceCode={} node={} attempt={} worker={}",
            node.getInstanceCode(), node.getNodeCode(), node.getAttempt(), workerId);
        // ctx 前置构建并登记，供 flusher 运行中上报过程日志
        NodeContext ctx = buildContext(node, instance);
        CompletableFuture<NodeResult> future = CompletableFuture.supplyAsync(() -> execute(node, instance, ctx), pool);
        futures.put(runKey(node), future);
        reportCtxs.put(runKey(node), ctx);
        supervisorPool.submit(() -> supervise(node, future));
    }

    /** 工作线程内执行：清理钩子（如置位）→ handler.execute（ctx 由 submit 预构建并登记上报） */
    private NodeResult execute(DagNodeInstance node, DagInstance instance, NodeContext ctx) {
        NodeHandler handler = registry.get(node.getHandler());
        if (node.getCleanupFlag() != null && node.getCleanupFlag() == 1) {
            log.info("micro-dag: 执行清理钩子 instanceCode={} node={}", node.getInstanceCode(), node.getNodeCode());
            handler.cleanup(ctx);
            logService.log(node.getInstanceCode(), node.getNodeCode(), node.getAttempt(), ExecutionEvent.CLEANUP, null, workerId);
            node.setCleanupFlag(0);
            nodeMapper.updateById(node);
        }
        try {
            return handler.execute(ctx);
        } catch (Exception e) {
            // 受检异常包装后由 CompletableFuture 转为 ExecutionException，监督线程统一派发
            throw new IllegalStateException("handler 执行异常: " + node.getNodeCode(), e);
        }
    }

    /** 监督线程：租约续期 + 超时控制 + 结果派发（成功/显式失败/超时/中断/失败五路） */
    private void supervise(DagNodeInstance node, CompletableFuture<NodeResult> future) {
        long leaseMs = leaseDuration(node);
        ScheduledFuture<?> renewal = leasePool.scheduleAtFixedRate(() -> {
            try {
                nodeMapper.renewLease(node.getId(), workerId, LocalDateTime.now().plusNanos(leaseMs * 1_000_000));
            } catch (Exception e) {
                log.error("micro-dag: 租约续期失败 node={}", node.getNodeCode(), e);
            }
        }, leaseMs / 3, leaseMs / 3, TimeUnit.MILLISECONDS);
        try {
            Long timeoutMs = node.getTimeoutMs();
            // timeout_ms=0 与 null 同义：不限时（0ms 立即超时会导致节点永远无法成功）
            NodeResult result = (timeoutMs == null || timeoutMs <= 0) ? future.get() : future.get(timeoutMs, TimeUnit.MILLISECONDS);
            if (interruptedFlags.remove(runKey(node)) != null) {
                engine.onInterrupted(node.getInstanceCode(), node.getNodeCode());
                return;
            }
            if (!result.isSuccess()) {
                // handler 显式失败（NodeResult.fail）：output 有则随事件写入 output_json
                engine.onFailure(node.getInstanceCode(), node.getNodeCode(),
                    ExecutionEvent.FAIL, failMsg(result), result.getOutput());
                return;
            }
            if (StringUtils.isNotBlank(result.getTraceCode())
                && nodeMapper.backfillTraceCode(node.getInstanceCode(), result.getTraceCode()) == 1) {
                log.info("micro-dag: traceCode 回填 instanceCode={} traceCode={}", node.getInstanceCode(), result.getTraceCode());
            }
            engine.onSuccess(node.getInstanceCode(), node.getNodeCode(), result.getOutput());
        } catch (TimeoutException e) {
            future.cancel(true);
            engine.onFailure(node.getInstanceCode(), node.getNodeCode(),
                ExecutionEvent.TIMEOUT, "timeout after " + node.getTimeoutMs() + "ms", null);
        } catch (CancellationException e) {
            // interrupt() 会 cancel CompletableFuture，get 抛 CancellationException（工作线程本身无法被强杀，靠标记协作退出）
            if (interruptedFlags.remove(runKey(node)) != null) {
                engine.onInterrupted(node.getInstanceCode(), node.getNodeCode());
            } else {
                log.error("micro-dag: 任务被取消但无中断标记 instanceCode={} node={}", node.getInstanceCode(), node.getNodeCode());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            engine.onInterrupted(node.getInstanceCode(), node.getNodeCode());
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            log.error("micro-dag: 节点执行异常 instanceCode={} node={}", node.getInstanceCode(), node.getNodeCode(), cause);
            if (cause instanceof InterruptedException || interruptedFlags.remove(runKey(node)) != null) {
                engine.onInterrupted(node.getInstanceCode(), node.getNodeCode());
            } else {
                engine.onFailure(node.getInstanceCode(), node.getNodeCode(), ExecutionEvent.FAIL, stackSummary(cause), null);
            }
        } catch (Exception e) {
            // 兜底：回调链路（含事务提交）抛出的未分类异常不能静默吞掉，否则节点卡 RUNNING 等租约回收形成死循环
            log.error("micro-dag: 节点回调派发异常 instanceCode={} node={}", node.getInstanceCode(), node.getNodeCode(), e);
        } finally {
            futures.remove(runKey(node));
            // 终态兜底：drain 剩余过程日志（含截断标记），再移除登记
            flushCtx(node);
            reportCtxs.remove(runKey(node));
            renewal.cancel(false);
            publisher.wakeUp();
        }
    }

    /** 中断入口（DagInterruptListener 调用）：置本地标记 + cancel 工作任务 */
    public void interrupt(String instanceCode, String nodeCode) {
        String key = instanceCode + ":" + nodeCode;
        log.info("micro-dag: 本机中断 instanceCode={} nodeCode={}", instanceCode, nodeCode);
        interruptedFlags.put(key, true);
        Future<?> future = futures.get(key);
        if (future != null) {
            future.cancel(true);
        }
    }

    /** 是否存在指定节点的本机执行任务（DagInterruptListener 判断是否本机处理） */
    public boolean isRunningHere(String instanceCode, String nodeCode) {
        return futures.containsKey(instanceCode + ":" + nodeCode);
    }

    public String getWorkerId() {
        return workerId;
    }

    /**
     * 租约时长：无超时节点用 fixedMs；有超时节点取 max(fixedMs, timeout + 30s)，
     * 保证租约恒覆盖超时窗口（超时由本执行器 Future.get 强制），避免超时瞬间与故障转移重派撞车
     */
    private long leaseDuration(DagNodeInstance node) {
        long fixedMs = properties.getLease().getFixedMs();
        // timeout_ms<=0 与 null 同义：不限时，租约用兜底时长
        if (node.getTimeoutMs() == null || node.getTimeoutMs() <= 0) {
            return fixedMs;
        }
        return Math.max(fixedMs, node.getTimeoutMs() + LEASE_MARGIN_MS);
    }

    private NodeContext buildContext(DagNodeInstance node, DagInstance instance) {
        return new NodeContext(node.getInstanceCode(), node.getNodeCode(), instance.getTraceCode(),
            node.getAttempt() == null ? 1 : node.getAttempt(),
            node.getInputParamsJson(), () -> interruptedFlags.containsKey(runKey(node)),
            properties.getLog().getMaxChars());
    }

    /** 定时落库：扫描本机运行中任务的过程日志缓冲，非空即写 NODE_LOG 记录 */
    private void flushAll() {
        for (NodeContext ctx : reportCtxs.values()) {
            flush(ctx, false);
        }
    }

    /** 终态兜底落库：drain 剩余缓冲；曾截断时补截断标记，保证截断可感知 */
    private void flushCtx(DagNodeInstance node) {
        NodeContext ctx = reportCtxs.get(runKey(node));
        if (ctx != null) {
            flush(ctx, true);
        }
    }

    /** drain 单个上下文日志缓冲并落库（drainLog 原子取走，周期与兜底并发不重不漏）；withMark 仅兜底为 true */
    private void flush(NodeContext ctx, boolean withMark) {
        String content = ctx.drainLog();
        if (withMark && ctx.isTruncated()) {
            content = StringUtils.isBlank(content) ? TRUNCATED_MARK : content + "\n" + TRUNCATED_MARK;
        }
        if (StringUtils.isNotBlank(content)) {
            logService.log(ctx.getInstanceCode(), ctx.getNodeCode(), ctx.getAttempt(),
                ExecutionEvent.NODE_LOG, null, content, workerId);
        }
    }

    /** 运行任务登记键：instanceCode:nodeCode */
    private String runKey(DagNodeInstance node) {
        return node.getInstanceCode() + ":" + node.getNodeCode();
    }

    /** 显式失败的失败消息：output 含 String 类型 msg 键则取该值，否则缺省文案 */
    private String failMsg(NodeResult result) {
        if (result.getOutput() != null && result.getOutput().get("msg") instanceof String msg) {
            return msg;
        }
        return "handler 返回失败";
    }

    private String stackSummary(Throwable cause) {
        StringWriter sw = new StringWriter();
        cause.printStackTrace(new PrintWriter(sw));
        String stack = sw.toString();
        return stack.length() > 1000 ? stack.substring(0, 1000) : stack;
    }

    private static ThreadFactory namedFactory(String name) {
        return r -> {
            Thread t = new Thread(r, name);
            t.setDaemon(true);
            return t;
        };
    }
}
