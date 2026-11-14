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

    private final ThreadPoolExecutor pool;
    private final ThreadPoolExecutor supervisorPool;
    private final ScheduledThreadPoolExecutor leasePool;
    /** 运行任务登记表，键为 instanceCode:nodeCode（跨机广播按此键定位本机任务） */
    private final ConcurrentHashMap<String, Future<?>> futures = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Boolean> interruptedFlags = new ConcurrentHashMap<>();
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
    }

    /** 调度器 CAS 领取成功后调用（在调度线程返回前仅做提交，不阻塞） */
    public void submit(DagNodeInstance node, DagInstance instance) {
        log.info("micro-dag: 提交执行 instanceCode={} node={} attempt={} worker={}",
            node.getInstanceCode(), node.getNodeCode(), node.getAttempt(), workerId);
        CompletableFuture<NodeResult> future = CompletableFuture.supplyAsync(() -> execute(node, instance), pool);
        futures.put(runKey(node), future);
        supervisorPool.submit(() -> supervise(node, future));
    }

    /** 工作线程内执行：清理钩子（如置位）→ handler.execute */
    private NodeResult execute(DagNodeInstance node, DagInstance instance) {
        NodeHandler handler = registry.get(node.getHandler());
        NodeContext ctx = buildContext(node, instance);
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

    /** 监督线程：租约续期 + 超时控制 + 结果派发（成功/超时/中断/失败四路） */
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
            NodeResult result = timeoutMs == null ? future.get() : future.get(timeoutMs, TimeUnit.MILLISECONDS);
            if (interruptedFlags.remove(runKey(node)) != null) {
                engine.onInterrupted(node.getInstanceCode(), node.getNodeCode());
                return;
            }
            if (StringUtils.isNotBlank(result.getTraceCode())
                && nodeMapper.backfillTraceCode(node.getInstanceCode(), result.getTraceCode()) == 1) {
                log.info("micro-dag: traceCode 回填 instanceCode={} traceCode={}", node.getInstanceCode(), result.getTraceCode());
            }
            engine.onSuccess(node.getInstanceCode(), node.getNodeCode(), result.getOutput());
        } catch (TimeoutException e) {
            future.cancel(true);
            engine.onFailure(node.getInstanceCode(), node.getNodeCode(), ExecutionEvent.TIMEOUT, "timeout after " + node.getTimeoutMs() + "ms");
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
                engine.onFailure(node.getInstanceCode(), node.getNodeCode(), ExecutionEvent.FAIL, stackSummary(cause));
            }
        } finally {
            futures.remove(runKey(node));
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
        if (node.getTimeoutMs() == null) {
            return fixedMs;
        }
        return Math.max(fixedMs, node.getTimeoutMs() + LEASE_MARGIN_MS);
    }

    private NodeContext buildContext(DagNodeInstance node, DagInstance instance) {
        return new NodeContext(node.getInstanceCode(), node.getNodeCode(), instance.getTraceCode(),
            node.getAttempt() == null ? 1 : node.getAttempt(),
            node.getInputParamsJson(), () -> interruptedFlags.containsKey(runKey(node)));
    }

    /** 运行任务登记键：instanceCode:nodeCode */
    private String runKey(DagNodeInstance node) {
        return node.getInstanceCode() + ":" + node.getNodeCode();
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
