package com.wkclz.micro.dag.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * dag 引擎配置项（默认值即生产推荐值）
 */
@ConfigurationProperties(prefix = "dag")
public class DagProperties {

    /** 引擎开关，false 时调度器与监听器均不启动（REST 仍可用） */
    private boolean enabled = true;

    private Scheduler scheduler = new Scheduler();
    private Worker worker = new Worker();
    private Lease lease = new Lease();

    /** 调度触发通道：auto=事件唤醒+轮询兜底 / db=仅轮询 / pubsub=仅事件唤醒(不推荐) */
    private String channel = "auto";

    public static class Scheduler {
        /** 轮询间隔 ms */
        private long intervalMs = 5000;
        /** 单轮最多领取任务数 */
        private int batchSize = 20;

        public long getIntervalMs() { return intervalMs; }
        public void setIntervalMs(long intervalMs) { this.intervalMs = intervalMs; }
        public int getBatchSize() { return batchSize; }
        public void setBatchSize(int batchSize) { this.batchSize = batchSize; }
    }

    public static class Worker {
        /** 执行线程池大小 */
        private int threads = Runtime.getRuntime().availableProcessors() * 2;

        public int getThreads() { return threads; }
        public void setThreads(int threads) { this.threads = threads; }
    }

    public static class Lease {
        /** 租约兜底时长 ms（与节点 timeoutMs 取小） */
        private long fixedMs = 120000;

        public long getFixedMs() { return fixedMs; }
        public void setFixedMs(long fixedMs) { this.fixedMs = fixedMs; }
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public Scheduler getScheduler() { return scheduler; }
    public void setScheduler(Scheduler scheduler) { this.scheduler = scheduler; }
    public Worker getWorker() { return worker; }
    public void setWorker(Worker worker) { this.worker = worker; }
    public Lease getLease() { return lease; }
    public void setLease(Lease lease) { this.lease = lease; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
}
