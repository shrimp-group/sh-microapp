# micro-dag 分布式业务编排引擎

## 概述

嵌入式轻量分布式 DAG 编排引擎：流程定义/发布、实例发起、分布式对等调度（CAS 领取 + 租约故障转移）、
失败自动重试（FIXED/EXPONENTIAL 退避）、人工干预治理（中断/改参/跳过/重置/重跑/从节点重跑/终止）、
重跑清理钩子、唯一跟踪编码回填、自研分支表达式引擎、节点分类（SYSTEM/DEMO/BUSINESS）、
系统节点 handler 化（开始/结束/分支/定时器）、开始节点时间驱动（cron 定时自动发起实例）。

设计文档：`docs/superpowers/specs/2026-11-14-micro-dag-design.md`

## 业务方接入

1. 引入依赖：

```xml
<dependency>
    <groupId>com.wkclz.microapp</groupId>
    <artifactId>micro-dag</artifactId>
</dependency>
```

2. 实现 NodeHandler 并注册为 Spring Bean（默认分类 BUSINESS，无需额外标注；demo handler 标 DEMO，
   系统 handler 标 SYSTEM）：

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

3. 执行建表脚本 `sql/dag_init.sql`，然后调用 `POST /micro-dag/process/create`（后端生成 dagCode）+ `/process/version/publish`
   创建流程，`POST /micro-dag/process/version/update` 编辑 DRAFT 草稿，`POST /micro-dag/instance/launch` 发起实例。
4. 可选：执行 `sql/dag_demo.sql` 灌入 14 条全场景演示流程（幂等，dag_code 前缀 dag_demo_，配合 `dag.demo-enabled=true` 使用，教程见 docs/living-docs-technical/modules/micro-dag-demo-tutorial.md）

## 配置项（dag.*）

| 配置 | 默认值 | 说明 |
|---|---|---|
| dag.enabled | true | 引擎开关（调度器/Worker/监听器），false 时仅 REST 可用 |
| dag.scheduler.interval-ms | 5000 | 轮询兜底间隔 |
| dag.scheduler.batch-size | 20 | 单轮单实例最多领取任务数 |
| dag.worker.threads | CPU*2 | 执行线程池大小 |
| dag.lease.fixed-ms | 120000 | 租约兜底时长 |
| dag.channel | auto | auto=事件唤醒+轮询兜底 / db=仅轮询 / pubsub=仅事件 |
| dag.demo-enabled | false | demo handler 注册开关（见下） |
| dag.trigger.enabled | true | START 节点时间驱动触发器开关 |
| dag.trigger.interval-ms | 30000 | 时间驱动触发器轮询间隔 |
| dag.log.max-chars | 8000 | 单节点过程日志缓冲上限（字符数），超出丢弃并标记截断 |
| dag.log.flush-interval-ms | 3000 | 过程日志定时落库间隔 |

## 系统 Handler（内置，永久生效）

`handler/system` 包内置 4 个系统 handler，无条件注册（不受 dag.demo-enabled 影响），category=SYSTEM。
START/END/BRANCH 节点统一走 worker 执行链路；存量定义（特殊节点无 handler）由调度器直达成功兼容。

| nodeCode | 名称 | 说明 | 入参 | 输出 |
|---|---|---|---|---|
| system-start | 系统-开始节点 | 透传入参供下游 `${out.<node>.*}` 引用；支持时间驱动；参数定义 triggerType/cron/triggerParams | 节点 params | 入参透传 |
| system-end | 系统-结束节点 | 直接成功 | 任意 | `{}` |
| system-branch | 系统-分支节点 | 出边条件求值明细（launch 时 branches 快照进 inputParams；下游就绪判断逻辑不变） | branches（toCode→whenExpr，launch 自动写入） | `{branches: {toCode: bool}, selected: [toCode]}` |
| system-timer | 系统-定时器 | 等待到点后成功（分片 sleep + 协作式中断），全局定时器节点无需业务实现；参数定义 delayMs/delayUntil | delayMs（0~86400000）/ delayUntil（ISO-8601） | `{waitedMs, targetTime}` |

### 开始节点时间驱动

START 节点 params 配置触发方式（缺省 MANUAL 手动 launch）：

```json
{"triggerType": "TIMER", "cron": "0 0 2 * * *", "triggerParams": {"bizKey": "value"}}
```

- `DagTriggerScheduler` 轮询 PUBLISHED 定义（dag.trigger.interval-ms），cron（Spring 6 域）到点自动 launch 实例
- 幂等：traceCode = `TIMER-{dagCode}-v{version}-{触发时间}`，同一触发点仅发起一个实例；Redis 锁防集群竞争
- 语义：停机期间错过的历史槽位不补触发，仅补触发最近一个到期槽位；触发状态内存态，重启自动重建；无 TIMER 触发的定义按 dagVersion 负缓存，仅版本变更时重新加载
- `dag.enabled=false` 或 `dag.trigger.enabled=false` 时触发器不运行，手动 launch 不受影响
- 校验：triggerType=TIMER 时 cron 必填且可解析（DagValidator）

## Demo Handler（可选）

`handler/demo` 包内置 6 个演示 handler（category=DEMO），`dag.demo-enabled=true` 时注册到设计器 handler 列表，默认关闭不注册（业务接入时不注册 demo 节点）。
GraalJS 依赖为 optional，classpath 缺失时 `demo-script-exec` 自动跳过注册（版本须与运行 JDK 匹配，23.x 不兼容 JDK 25）。

| nodeCode | 名称 | 入参 | 输出 |
|---|---|---|---|
| demo-log-print | 演示-日志打印 | 任意 | `{echo: 入参}` |
| demo-delay | 演示-延时 | ms（默认 1000，上限 300000） | `{sleptMs}`；演示协作式中断 |
| demo-transform | 演示-数据转换 | 任意 | 入参合并 + `processedAt` |
| demo-http-call | 演示-HTTP调用 | url（必填）、method、headers、body | `{status, body}`（body 截断 2000 字符） |
| demo-script-exec | 演示-JS脚本 | script（定义 `handle(input)`）、input（缺省为全部入参） | handle 返回对象原样输出，标量包装为 `{result}` |
| demo-fail | 演示-可控失败 | failTimes（前 N 次失败，缺省 0）、msg | 前 N 次 `{attempt, msg}` 失败，之后 `{attempt, msg: success}` 成功并回填 trace_code；演示显式失败、RETRYING、过程日志(NODE_LOG)、trace 回填 |

全部 demo handler 均上报过程日志（ctx.log → NODE_LOG），实例监控执行记录时间线可查看正常执行的过程日志（demo-fail 之外此前无上报）。

## 核心机制速记

- 状态推进双通道：完成事件即时回调 + 轮询兜底，汇合于 DB CAS，天然幂等
- 节点参数 schema：NodeHandler.paramSchema() 声明参数定义（NodeParamDef：key/label/type/必填/requiredWhen/范围/选项），随 handler/list 下发，设计器按定义渲染参数表单（表单/JSON 双模式）；DagValidator 保存与发布时按定义校验（必填/范围/CRON/DATETIME/SELECT/KEY_VALUE），schema 未声明的自由键不拦截；免 handler 的 START 节点 cron 校验按 nodeType 兜底
- 就绪领取唯一入口：DagScheduler.scan() 的 casClaim，防重复执行
- 故障转移：租约过期（lease_expire_time < now）→ 重置 PENDING 重派
- 重试：失败且 attempt ≤ maxRetry → RETRYING + next_retry_time，到期由调度提升
- 分支：上游完成后对出边求值（`${ctx.*}` / `${out.<node>.*}` + 比较 + and/or），不满足级联 SKIPPED；级联跳过仅在上游全部确定性不满足（FAILED/MANUAL_STOPPED/链式 SKIPPED/条件求值 false）时触发，上游 PENDING/RUNNING/RETRYING 属未定状态不参与跳过
- 特殊节点直达：仅 START/END/BRANCH 且 handler 为空（存量兼容）领取后直达成功；有 handler 一律走 worker
- 节点分类：SYSTEM 永久生效 / DEMO 受 dag.demo-enabled 限制 / BUSINESS 业务接入默认
- 清理钩子：重跑目标 attempt > 0 → cleanup_flag=1 → 执行前先调 handler.cleanup
- 显式失败：handler 返回 NodeResult.fail(output) → 派发 FAIL（output 含 String 类型 msg 键时作为失败消息；output 写入 output_json，RETRYING/FAILED 均写）
- 未就绪：handler 返回 NodeResult.notReady(output) → 派发 NOT_READY 事件并复用失败重试链路（RETRYING + FIXED/EXPONENTIAL 退避），attempt 耗尽转 FAILED；output 含 String msg 键时作为未就绪原因写入 error_msg
- 过程日志：handler 调 ctx.log(message) 内存缓冲（线程安全，上限 dag.log.max-chars，超限丢弃并置 truncated）；
  flusher 单线程按 dag.log.flush-interval-ms 扫描落库 NODE_LOG 事件（log_text 独立字段，无 detail_json），节点终态 finally 兜底 flush 一次并补 `...[truncated]` 标记；
  落库异常在 flush 内部消化（log.error 显形），防止 scheduleAtFixedRate 任务被异常永久取消导致 flusher 静默停摆
- DDL 迁移（存量库升级时执行，新建库直接用最新表定义）：`ALTER TABLE dag_execution_log MODIFY detail_json JSON;` `ALTER TABLE dag_execution_log ADD COLUMN log_text TEXT COMMENT '过程日志内容(NODE_LOG 专用)';`（迁移前校验存量 detail_json 均为合法 JSON，详见 docs/tasks/micro-dag/02-data-layer.md）

## API 一览（前缀 /micro-dag，详见 rest/Route.java）

流程 page/create · 版本 create/update/publish/detail/list · handler/list · expression/preview ·
实例 launch/page/detail/terminate/rerun-from · 节点 detail/interrupt/params/skip/reset/rerun

## 前端（独立工程 sh-dag-ui，appCode=dag）

- DagProcessList：流程分页列表（一条流程一条记录，聚合状态 EDITING/PUBLISHED 与最新版本列，
  操作仅设计+历史版本），设计动作经全屏弹窗内嵌设计器；无草稿时以最新已发布版本为底稿打开，
  发布按钮收归设计器 footer（关闭弹窗刷新列表）
- VersionHistoryDialog：版本历史弹窗（版本列表 + 全屏只读画布查看，草稿显示"草稿"、发布版本显示 vN）
- DagDesigner：纯设计器（左信息栏[编码/必填名称] / 左 handler 面板 / 中画布[自动布局悬浮] / 右属性面板），保存/发布/关闭为全局按钮固定右下角
- DagInstanceList：实例分页列表（查询/发起/详情/终止），状态中文展示，不做定时刷新（关闭详情弹窗后刷新），详情动作经全屏弹窗内嵌详情组件
- DagGraph：X6 画布封装——节点左右端口磁吸连线（仅 out→in）、mousedown 即选中、
  拖动坐标实时回写 def、拖入与连线均为增量操作（不做全量 render，避免坐标回跳重排）、
  readonly 模式节点本体保持类型配色，执行状态经右上角标圆点（状态色）+ 第二行中文状态文字呈现
- DagInstanceDetail：实例详情（全屏 dialog 内嵌组件，支持 instanceCode prop + route query 兜底）——实例/节点状态中文、
  节点七态色板着色 + 画布状态图例、点击节点经 el-popover 气泡展示概要与干预操作（中断/跳过/重置/重跑），
  执行记录时间线由气泡入口打开 NodeDetailDrawer 弹窗查看（事件枚举中文）
- NodeDetailDrawer：执行记录时间线弹窗（仅承载 logs，事件枚举中文展示，由气泡入口打开）
