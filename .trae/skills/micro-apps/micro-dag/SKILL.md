---
name: micro-dag
description: 当需要操作 micro-dag 模块时使用此技能 —— 嵌入式轻量分布式 DAG 业务编排引擎。当用户需要实现 NodeHandler 业务节点、创建/发布流程定义、发起与监控实例、人工干预（中断/跳过/重置/重跑）、配置系统节点（开始/结束/分支/定时器）、开始节点 cron 时间驱动，或修改 com.wkclz.micro.dag 包下任何代码时触发。
---

# Micro-Dag 模块

嵌入式轻量分布式 DAG 编排引擎：流程定义/发布、实例发起、分布式对等调度（CAS 领取 + 租约故障转移）、失败自动重试（FIXED/EXPONENTIAL 退避）、人工干预治理、自研分支表达式引擎、开始节点 cron 时间驱动。

## 适用场景

- 修改 `micro-dag` 包（com.wkclz.micro.dag）下任何 Java 文件
- 业务方接入：实现 NodeHandler、创建/发布流程、发起实例
- 调试调度、租约故障转移、重试、分支跳过、定时器问题
- 理解过程日志（NODE_LOG）、trace_code 回填、节点参数 schema 机制

## 架构概览

```
Rest             DagProcessRest / DagProcessVersionRest / DagMetaRest
                 DagInstanceRest / DagNodeInstanceRest
    ↓
Service          流程定义/版本/实例/节点实例 Service
    ↓
调度             DagScheduler（CAS 领取）+ Worker 线程池 + DagTriggerScheduler（cron 触发）
    ↓
Mapper           DagProcessMapper / DagProcessVersionMapper / DagNodeMapper
                 DagEdgeMapper / DagInstanceMapper / DagNodeInstanceMapper / DagExecutionLogMapper
```

**核心设计：**
- 状态推进双通道：完成事件即时回调 + 轮询兜底，汇合于 DB CAS，天然幂等
- 分布式对等：无中心节点，多实例部署靠 CAS 领取 + 租约（lease_expire_time）故障转移
- 节点分类：SYSTEM（内置永久生效）/ DEMO（dag.demo-enabled 控制）/ BUSINESS（业务接入默认）

## 数据实体（7 个，表定义见 sql/dag_init.sql）

| 实体 | 说明 |
|------|------|
| DagProcess | 流程主表（聚合状态 EDITING/PUBLISHED） |
| DagProcessVersion | 流程版本（DRAFT/PUBLISHED，version 递增） |
| DagNode / DagEdge | 节点/边定义（版本内 JSON 存储） |
| DagInstance | 实例（RUNNING/FAILED/SUCCESS/TERMINATED 等） |
| DagNodeInstance | 节点实例（七态 + attempt/lease/next_retry_time） |
| DagExecutionLog | 执行日志（事件 detail_json + NODE_LOG 过程日志 log_text） |

## 业务方接入

```java
@Component
public class DataFetchHandler implements NodeHandler {
    public String nodeCode() { return "fetch"; }
    public NodeResult execute(NodeContext ctx) throws Exception {
        ctx.checkInterrupted(); // 长任务周期性检查协作式中断
        // 前置未满足：return NodeResult.notReady(Map.of("msg", "上游数据未落库"));
        return NodeResult.ok(Map.of("rows", 100)).withTraceCode("TRC-xxx");
    }
    public void cleanup(NodeContext ctx) { /* 重跑前清理历史数据 */ }
}
```

- `NodeResult.ok/fail/notReady` 三种语义：fail 显式失败（output 含 String msg 键时作为失败消息）；notReady 复用失败重试链路（RETRYING + 退避），attempt 耗尽转 FAILED
- 可选 `paramSchema()` 声明参数定义（NodeParamDef：key/label/type/必填/requiredWhen/范围/选项），设计器按定义渲染参数表单，DagValidator 保存与发布时按定义校验
- 建表脚本 `sql/dag_init.sql`；演示流程 `sql/dag_demo.sql`（14 条，dag_code 前缀 dag_demo_，配合 `dag.demo-enabled=true`）

## REST API 端点（前缀 /micro-dag，共 20 个）

### 流程与版本（DagProcessRest / DagProcessVersionRest）

| 端点 | 方法 | 说明 |
|------|------|------|
| `/process/page` | GET | 流程分页查询 |
| `/process/create` | POST | 创建流程（后端生成 dagCode） |
| `/process/version/create` | POST | 创建新版本草稿 |
| `/process/version/update` | POST | 编辑 DRAFT 草稿 |
| `/process/version/publish` | POST | 发布版本 |
| `/process/version/detail` | GET | 版本详情（节点/边） |
| `/process/version/list` | GET | 版本列表 |

### 元能力（DagMetaRest）

| 端点 | 方法 | 说明 |
|------|------|------|
| `/handler/list` | GET | handler 列表（含 paramSchema，供设计器渲染） |
| `/expression/preview` | POST | 分支表达式预览求值 |

### 实例与节点（DagInstanceRest / DagNodeInstanceRest）

| 端点 | 方法 | 说明 |
|------|------|------|
| `/instance/launch` | POST | 发起实例（快照 branches/upstreams 进 inputParams） |
| `/instance/page` | GET | 实例分页查询 |
| `/instance/detail` | GET | 实例详情（含节点耗时 lastCostMs） |
| `/instance/terminate` | POST | 终止实例 |
| `/instance/rerun-from` | POST | 从指定节点重跑（body 可选 instanceParamsJson 非空时同步 bizParamsJson 与 contextJson） |
| `/node/detail` | GET | 节点详情 |
| `/node/interrupt` | POST | 中断运行中节点（协作式） |
| `/node/params` | POST | 修改节点参数 |
| `/node/skip` | POST | 跳过节点（连锁 SKIPPED 下游自动补救重置 PENDING） |
| `/node/reset` | POST | 重置节点 |
| `/node/rerun` | POST | 重跑节点（attempt > 0 触发 cleanup 钩子，body 可选 inputParamsJson 重设参数） |

## 配置项（dag.*）

| 配置 | 默认值 | 说明 |
|---|---|---|
| dag.enabled | true | 引擎开关（调度器/Worker/监听器），false 时仅 REST 可用 |
| dag.scheduler.interval-ms | 5000 | 轮询兜底间隔 |
| dag.scheduler.batch-size | 20 | 单轮单实例最多领取任务数 |
| dag.worker.threads | CPU*2 | 执行线程池大小 |
| dag.lease.fixed-ms | 120000 | 租约兜底时长 |
| dag.channel | auto | auto=事件唤醒+轮询兜底 / db=仅轮询 / pubsub=仅事件 |
| dag.demo-enabled | false | demo handler 注册开关 |
| dag.trigger.enabled / interval-ms | true / 30000 | START 节点 cron 触发器开关/轮询间隔 |
| dag.log.max-chars / flush-interval-ms | 8000 / 3000 | 过程日志缓冲上限/落库间隔 |

## 核心机制速记

- 入口/汇点约束：定义必须恰有 1 个 START 与 1 个 END，START 唯一入口（入度 0）、END 唯一汇点（出度 0），保存与发布共用校验
- 就绪领取唯一入口：DagScheduler.scan() 的 casClaim，防重复执行
- 故障转移：租约过期 → attempt ≤ maxRetry 重置 PENDING 重派，耗尽置 FAILED
- 分支：上游完成后对出边求值（`${ctx.*}` / `${out.<node>.*}` + 比较 + and/or），不满足级联 SKIPPED；级联跳过仅在全部入边确定性不满足时触发，未定状态（FAILED/PENDING/RUNNING/RETRYING）下游保持 PENDING 等待
- 系统 handler（handler/system 包，category=SYSTEM）：system-start（透传入参，支持 TIMER 触发）/ system-end / system-branch（出边求值明细）/ system-timer（到点成功，24h 有效期，上游完成校验）
- 开始节点时间驱动：START params `{"triggerType":"TIMER","cron":"0 0 2 * * *"}`，DagTriggerScheduler 到点自动 launch，幂等 traceCode=`TIMER-{dagCode}-v{version}-{触发时间}`，Redis 锁防集群竞争
- 过程日志：ctx.log(message) 内存缓冲（上限 dag.log.max-chars），flusher 定时落库 NODE_LOG（log_text 独立字段），节点终态 finally 兜底 flush
- 重跑清理钩子：重跑目标 attempt > 0 → cleanup_flag=1 → 执行前先调 handler.cleanup
- 存量兼容：仅 START/END/BRANCH 且 handler 为空时领取后直达成功；有 handler 一律走 worker

## 重要约束与常见问题

| 问题 | 原因/解决 |
|------|-----------|
| 节点一直 PENDING | 上游未定状态（FAILED/RETRYING）下游等待；实例无在途节点时置 FAILED，需人工 rerun/rerun-from 恢复 |
| 定时器节点超期失败 | system-timer 到点时间 24h 有效期，超期置失败等人工干预 |
| demo-script-exec 未注册 | GraalJS 依赖为 optional，版本须与运行 JDK 匹配（23.x 不兼容 JDK 25） |
| cron 触发不生效 | dag.enabled=false 或 dag.trigger.enabled=false 时触发器不运行；triggerType=TIMER 时 cron 必填且可解析 |
| 手动 skip 后下游未执行 | SKIPPED 上游 satisfiesDownstream=true；实例 RUNNING 时手动 skip PENDING 节点本就不级联，跳过补救仅针对被连锁 SKIPPED 的下游 |
| 存量库升级 | 执行 DDL 迁移：`ALTER TABLE dag_execution_log MODIFY detail_json JSON;` + 新增 log_text TEXT（迁移前校验存量 detail_json 均为合法 JSON） |

前端独立工程 sh-dag-ui（appCode=dag）：DagProcessList / DagDesigner / DagInstanceList / DagInstanceDetail / DagGraph（X6 画布）/ NodeDetailDrawer。

---
更新时间：2026-11-28
