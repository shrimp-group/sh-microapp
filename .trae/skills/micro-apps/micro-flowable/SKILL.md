---
name: micro-flowable
description: 当需要操作 micro-flowable 模块时使用此技能 —— 对接 sh-flowable-server 的流程引擎业务侧模块。当用户需要实现流程设计/部署/节点配置、流程申请与草稿、待办已办审批（通过/驳回/转办/委派/认领）、实例撤回与历史查询、审批意见、AOP 异常监控，或修改 com.wkclz.micro.flowable 包下任何代码时触发。
---

# Micro-Flowable 模块

sh-flowable-server 的业务侧对接壳，提供流程设计管理、流程发起与审批流转、异常监控能力。运行态数据全部实时透传引擎，不在本地缓存。

## 适用场景

- 修改 `micro-flowable` 包（com.wkclz.micro.flowable）下任何 Java 文件
- 流程设计 CRUD / 部署 / 旧版本清理 / 节点配置
- 业务申请（发起/草稿）、待办已办、6 种审批动作、实例历史、审批意见
- 调试 client 异常拦截落库、BPMN 解析、分类归一化问题

## 架构概览

```
micro-flowable（业务侧）
  ├── 本地 6 张表（分类 1 + 设计态 2 + 业务态 2 + 异常 1）
  ├── 管理端：设计 CRUD / 部署 / 节点配置 / 分类 / 透传查询
  ├── 业务端：申请 / 待办已办 / 审批动作 / 实例历史 / 审批意见
  └── 异常监控：FlowableClientAspect AOP 拦截 client 调用 + 落库 + 查询/标记
        ↓ sh-flowable-client（HttpExchange 声明式 HTTP，5 个 client）
sh-flowable-server（独立部署，内嵌 Flowable 8.0.0，不对外暴露 Ingress）
```

## 数据实体（6 个，建表脚本 src/main/resources/sql/micro-flowable-ddl.sql）

| 实体/表 | 说明 |
|------|------|
| FlowableCategory（flowable_category） | 流程分类 |
| FlowableProcessDesign（flowable_process_design） | 流程设计（设计态，含 BPMN XML） |
| FlowableNodeConfig（flowable_node_config） | 节点配置（设计态，保存时从 XML 解析生成） |
| FlowableApply（flowable_apply） | 流程申请单（业务态） |
| FlowableApproval（flowable_approval） | 审批意见（业务态） |
| FlowableErrorLog（flowable_error_log） | client 调用异常日志 |

## REST API 端点（前缀 /micro-flowable，共 44 个）

### 管理端

| 端点 | 方法 | 说明 |
|------|------|------|
| `/admin/design/page` `/info` | GET | 设计分页/详情（含节点） |
| `/admin/design/create` `/update` `/remove` | POST | 设计创建/修改/删除 |
| `/admin/design/deploy` | POST | 部署设计 |
| `/admin/design/remove-old-versions` | POST | 清理旧版本（cascade=false 默认保留已完结历史 / true 级联删除运行实例与全部历史） |
| `/admin/node/list` `/info` | GET | 节点配置列表/详情 |
| `/admin/node/update` | POST | 节点配置修改 |
| `/admin/category/page` `/list` | GET | 分类分页/下拉列表 |
| `/admin/category/create` `/update` `/remove` | POST | 分类创建/修改/删除 |
| `/admin/definition/page` `/info` `/list` | GET | 流程定义透传查询（实时查引擎） |
| `/admin/deploy/page` | GET | 部署记录透传分页 |
| `/admin/deploy/remove` | POST | 删除部署记录（固定 cascade=true） |

### 业务端

| 端点 | 方法 | 说明 |
|------|------|------|
| `/apply/create` | POST | 申请发起（直接启动流程） |
| `/apply/create_draft` | POST | 存草稿（DRAFT 状态，不启动流程） |
| `/apply/submit` | POST | 草稿提交（仅草稿可提交，仅本人申请单） |
| `/apply/page` `/info` | GET | 我的申请分页/详情（回填当前节点/审批人） |
| `/task/todo/page` `/done/page` `/info` | GET | 待办/已办分页/任务详情 |
| `/task/complete` `/reject` | POST | 通过/驳回 |
| `/task/claim` `/unclaim` | POST | 认领/取消认领 |
| `/task/transfer` `/delegate` | POST | 转办/委派 |
| `/approval/list` | GET | 审批意见时间线 |
| `/instance/page` `/info` | GET | 流程实例透传分页/详情 |
| `/instance/withdraw` | POST | 撤回申请 |
| `/history/instance/page` `/task/page` `/activity/list` | GET | 历史实例/历史任务/历史活动透传查询 |

### 异常监控（ErrorLogRest）

| 端点 | 方法 | 说明 |
|------|------|------|
| `/error/page` `/info` | GET | 异常日志分页/详情 |
| `/error/handle` | POST | 标记处理 |

## 配置项（sh.flowable.*）

```yaml
sh:
  flowable:
    server-url: http://sh-flowable-server-svc:8080  # 未配置回退 localhost:8080 并 WARN
    connect-timeout: 5000
    read-timeout: 30000
    error-log:
      enabled: true        # 异常日志落库开关
      include-stack: true  # 是否记录异常堆栈
```

## 核心机制速记

- 透传端点：运行态数据（定义/实例/任务/历史）实时透传 sh-flowable-server，不在本地缓存
- 审批意见：所有审批动作（通过/驳回/转办/委派/认领/撤回）均记录到本地 flowable_approval 表
- 异常拦截：`FlowableClientAspect` 统一拦截 5 个 client 接口（`execution(* com.wkclz.flowable.client.flowable.*Client.*(..))`），失败/异常落库 flowable_error_log 并抛业务异常；ErrorType 按约定推导（GET → QUERY_ERROR；POST 且方法名 start 前缀 → START_ERROR；deploy、remove 前缀 → DEPLOY_ERROR；其余 POST → APPROVE_ERROR），新增 client 方法零标注自动归类
- BPMN 解析：保存时先归一化 process id/name 为 designCode/designName，再解析 XML 自动生成节点配置；更新时仅 XML 变更才解析，按 nodeKey 差量同步（insertBatch + updateByIdSelective + deleteByIds，保留人工审批配置）；解析失败不阻塞保存，归一化校验失败会阻塞保存
- 流程分类：design.category 存分类编码；部署时经 normalizeCategory 将编码写入 BPMN `definitions targetNamespace`（引擎 ProcessDefinition.category 取自该属性）并透传部署分类，仅改写部署副本不回写设计表
- design_version vs version：design_version 是设计版本号，version 是乐观锁字段，注意区分

## 重要约束

1. **乐观锁**：更新操作必须传 `version` 字段
2. **依赖注入**：统一使用 `@Resource`，禁止 `@Autowired`
3. **常量**：业务编码前缀（`apply_`/`flow_`/`flow_cat_`）、空审批人占位符（`-`）、审批人类型统一引用 `FlowableConstants`，禁止散落字面量
4. **共享方法**：`FlowableApplyService.selectByProcInsId`/`updateStatusByProcInsId`、`FlowableNodeConfigService.selectByDesignCode(s)`/`groupByDesignCode`、`FlowableProcessDesignService.selectByDesignCode` 供 Rest 层复用，避免 Rest 层内联拼查询
5. **部署配置**：业务应用侧 `sh.flowable.server-url` 指向集群内 Service DNS；flowable-server 侧 `iam.session.white-list=/**`（一期集群内不鉴权）；勿为 sh-flowable-server 创建 Ingress

## 常见问题

| 问题 | 原因/解决 |
|------|-----------|
| server-url 回退 localhost | 未配置 sh.flowable.server-url，启动日志有 WARN，配置集群内 Service DNS |
| 定义查询分类过滤不一致 | 分类经 targetNamespace 归一化，仅改分类后需重新部署才生效 |
| 节点配置丢失人工设置 | XML 变更才重新解析并按 nodeKey 差量同步，未变更不会覆盖人工配置 |
| 草稿提交报"仅草稿可提交"/"无权操作他人申请单" | submit 仅限 DRAFT 状态且本人申请单 |

---
更新时间：2026-11-28
