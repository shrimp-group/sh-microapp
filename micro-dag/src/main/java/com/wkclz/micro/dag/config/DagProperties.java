package com.wkclz.micro.dag.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * dag 引擎配置项（默认值即生产推荐值）
 */
@Data
@ConfigurationProperties(prefix = "dag")
public class DagProperties {

    /** 引擎开关，false 时调度器与监听器均不启动（REST 仍可用） */
    private boolean enabled = true;

    /** demo handler 注册开关（log-print/delay/transform/http-call/script-exec），默认关闭 */
    private boolean demoEnabled = false;

    private Scheduler scheduler = new Scheduler();
    private Worker worker = new Worker();
    private Lease lease = new Lease();
    private Trigger trigger = new Trigger();
    private Log log = new Log();

    /** 调度触发通道：auto=事件唤醒+轮询兜底 / db=仅轮询 / pubsub=仅事件唤醒(不推荐) */
    private String channel = "auto";

    @Data
    public static class Scheduler {
        /** 轮询间隔 ms */
        private long intervalMs = 5000;
        /** 单轮最多领取任务数 */
        private int batchSize = 20;
    }

    @Data
    public static class Worker {
        /** 执行线程池大小 */
        private int threads = Runtime.getRuntime().availableProcessors() * 2;
    }

    @Data
    public static class Lease {
        /** 租约兜底时长 ms（与节点 timeoutMs 取小） */
        private long fixedMs = 120000;
    }

    @Data
    public static class Trigger {
        /** 时间驱动触发器开关 */
        private boolean enabled = true;
        /** 触发轮询间隔 ms */
        private long intervalMs = 30000;
    }

    /** 过程日志上报配置 */
    @Data
    public static class Log {
        /** 单节点过程日志缓冲上限（字符数），超出丢弃并标记截断 */
        private int maxChars = 8000;
        /** 过程日志定时落库间隔 ms */
        private long flushIntervalMs = 3000;
    }

}
