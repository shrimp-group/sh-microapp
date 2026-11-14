# micro-dag 分布式业务编排引擎

## 概述

嵌入式轻量分布式 DAG 编排引擎：流程定义/发布、实例发起、分布式对等调度（CAS 领取 + 租约故障转移）、
失败自动重试（FIXED/EXPONENTIAL 退避）、人工干预治理（中断/改参/跳过/重置/重跑/从节点重跑/终止）、
重跑清理钩子、唯一跟踪编码回填、自研分支表达式引擎。

设计文档：`docs/superpowers/specs/2026-11-14-micro-dag-design.md`

## 业务方接入

1. 引入依赖：

```xml
<dependency>
    <groupId>com.wkclz.microapp</groupId>
    <artifactId>micro-dag</artifactId>
</dependency>
```

2. 实现 NodeHandler 并注册为 Spring Bean：

```java
@Component
public class DataFetchHandler implements NodeHandler {
    public String nodeCode() { return "fetch"; }
    public NodeResult execute(NodeContext ctx) throws Exception {
        ctx.checkInterrupted(); // 长任务周期性检查协作式中断
        return NodeResult.ok(Map.of("rows", 100)).withTraceCode("TRC-xxx");
    }
    public void cleanup(NodeContext ctx) { /* 重跑前清理历史数据 */ }
}
```

3. 执行建表脚本 `sql/dag_init.sql`，然后调用 `POST /micro-dag/definition/save` + `/definition/publish`
   创建流程，`POST /micro-dag/instance/launch` 发起实例。

## 配置项（dag.*）

| 配置 | 默认值 | 说明 |
|---|---|---|
| dag.enabled | true | 引擎开关（调度器/Worker/监听器），false 时仅 REST 可用 |
| dag.scheduler.interval-ms | 5000 | 轮询兜底间隔 |
| dag.scheduler.batch-size | 20 | 单轮单实例最多领取任务数 |
| dag.worker.threads | CPU*2 | 执行线程池大小 |
| dag.lease.fixed-ms | 120000 | 租约兜底时长 |
| dag.channel | auto | auto=事件唤醒+轮询兜底 / db=仅轮询 / pubsub=仅事件 |

## 核心机制速记

- 状态推进双通道：完成事件即时回调 + 轮询兜底，汇合于 DB CAS，天然幂等
- 就绪领取唯一入口：DagScheduler.scan() 的 casClaim，防重复执行
- 故障转移：租约过期（lease_expire_time < now）→ 重置 PENDING 重派
- 重试：失败且 attempt ≤ maxRetry → RETRYING + next_retry_time，到期由调度提升
- 分支：上游完成后对出边求值（`${ctx.*}` / `${out.<node>.*}` + 比较 + and/or），不满足级联 SKIPPED
- 清理钩子：重跑目标 attempt > 0 → cleanup_flag=1 → 执行前先调 handler.cleanup

## API 一览（前缀 /micro-dag，详见 rest/Route.java）

定义 save/publish/page/detail/copy · handler/list · expression/preview ·
实例 launch/page/detail/terminate/rerun-from · 节点 detail/interrupt/params/skip/reset/rerun
