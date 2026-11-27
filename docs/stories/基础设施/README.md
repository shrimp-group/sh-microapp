# 基础设施

| 序号 | 名称 | 优先级 | 状态 | 文件 |
|------|------|--------|------|------|
| 001 | LiteFlow链与脚本 | P1 | 已完成 | [001-LiteFlow链与脚本.md](./001-LiteFlow链与脚本.md) |
| 002 | 函数脚本引擎 | P1 | 已完成 | [002-函数脚本引擎.md](./002-函数脚本引擎.md) |
| 003 | K8s资源管理 | P1 | 已完成 | [003-K8s资源管理.md](./003-K8s资源管理.md) |
| 004 | 自动化测试执行 | P2 | 已完成 | [004-自动化测试执行.md](./004-自动化测试执行.md) |
| 005 | 数据源与元数据 | P1 | 已完成 | [005-数据源与元数据.md](./005-数据源与元数据.md) |
| 006 | SQL与DDL操作 | P1 | 已完成 | [006-SQL与DDL操作.md](./006-SQL与DDL操作.md) |
| 007 | micro-flowable流程引擎 | P1 | 已完成 | [007-micro-flowable流程引擎.md](./007-micro-flowable流程引擎.md) |
| 008 | micro-dag业务编排 | P1 | 已完成 | [008-micro-dag业务编排.md](./008-micro-dag业务编排.md) |
| 009 | micro-reportSQL报表 | P1 | 已完成 | [009-micro-reportSQL报表.md](./009-micro-reportSQL报表.md) |

> 状态核对日期：2026-11-28。micro-liteflow（Chain/Script/Common Rest）、micro-fun（category/function 含在线测试 `/function/test`）、micro-k8s（Config/Cluster/Kind/Pod Rest）、micro-autotest（`/api/list`、`/run`、`/report/md|html`）、micro-dbview（datasource/metadata/sql/ddl/permission/diff，见 `micro-dbview/AGENTS.md`）均已实现；注意 story 中关联 API 路径与实际端点命名存在差异，以各模块 Route 为准。micro-flowable 对接 sh-flowable-server（见模块 AGENTS.md）；micro-dag（流程定义/发布/实例调度/人工干预/系统 handler/时间驱动，见 `micro-dag/AGENTS.md`）与 micro-report（SQL 报表定义/参数/结果字段/动态查询/Excel 导出，见 `micro-report/AGENTS.md`）story 于 2026-11-28 补建。
