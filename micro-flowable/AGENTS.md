# micro-flowable 模块开发指南

## 模块概述

micro-flowable 是 sh-microapp 微应用集合中的流程引擎对接模块，作为 sh-flowable-server 的业务侧对接壳，提供流程设计管理、流程发起与审批流转、异常监控能力。

- **GroupId**: `com.wkclz.microapp`
- **ArtifactId**: `micro-flowable`
- **API 前缀**: `/micro-flowable`
- **外部依赖**: `com.wkclz.flowable:sh-flowable-client:1.0.0-SNAPSHOT`

## 架构设计

```
micro-flowable（业务侧）
  ├── 本地 5 张表（设计态 2 + 业务态 2 + 异常 1）
  ├── 管理端：设计 CRUD / 部署 / 节点配置 / 透传查询
  ├── 业务端：申请 / 待办已办 / 6 种审批动作 / 实例历史 / 审批意见
  └── 异常监控：AOP 拦截 client 调用 + 落库 + 查询/标记
        ↓ sh-flowable-client（HttpExchange 声明式 HTTP）
sh-flowable-server（独立部署，内嵌 Flowable 8.0.0）
```

## 目录结构

```
src/main/java/com/wkclz/micro/flowable/
├── FlowableAutoConfig.java              # 自动配置（@ComponentScan + @MapperScan）
├── aspect/                              # AOP（预留）
├── bean/
│   ├── entity/                          # 5 个实体（extends BaseEntity）
│   ├── enums/                           # 6 个枚举
│   ├── req/                             # 15 个请求 Bean
│   └── resp/                            # 10 个响应 Bean
├── config/
│   └── FlowableErrorLogProperties.java  # 异常日志配置
├── mapper/                              # 5 个 Mapper 接口
├── rest/
│   ├── Route.java                       # 路由常量（36 个端点）
│   ├── ProcessDesignRest.java           # 管理端-设计管理（6 端点）
│   ├── NodeConfigRest.java              # 管理端-节点配置（3 端点）
│   ├── DefinitionPassthroughRest.java   # 管理端-透传查询（5 端点）
│   ├── ApplyRest.java                   # 业务端-申请（3 端点）
│   ├── FlowableTaskRest.java            # 业务端-任务审批（10 端点）
│   ├── InstanceHistoryRest.java         # 业务端-实例历史（6 端点）
│   └── ErrorLogRest.java               # 异常监控（3 端点）
└── service/
    ├── FlowableProcessDesignService.java
    ├── FlowableNodeConfigService.java
    ├── FlowableApplyService.java
    ├── FlowableApprovalService.java
    ├── FlowableErrorLogService.java
    └── FlowableClientWrapper.java       # client 调用包装（异常拦截落库）

src/main/resources/
├── mapper/                              # 5 个 MyBatis XML
└── sql/
    └── micro-flowable-ddl.sql           # 建表脚本
```

## API 端点清单

### 管理端 - 流程设计（ProcessDesignRest）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/micro-flowable/admin/design/create` | 3.流程设计-创建 |
| GET | `/micro-flowable/admin/design/page` | 1.流程设计-分页查询 |
| GET | `/micro-flowable/admin/design/info` | 2.流程设计-详情（含节点） |
| POST | `/micro-flowable/admin/design/update` | 4.流程设计-修改 |
| POST | `/micro-flowable/admin/design/remove` | 5.流程设计-删除 |
| POST | `/micro-flowable/admin/design/deploy` | 6.流程设计-部署 |

### 管理端 - 节点配置（NodeConfigRest）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/micro-flowable/admin/node/list` | 1.节点配置-列表 |
| GET | `/micro-flowable/admin/node/info` | 2.节点配置-详情 |
| POST | `/micro-flowable/admin/node/update` | 3.节点配置-修改 |

### 管理端 - 透传查询（DefinitionPassthroughRest）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/micro-flowable/admin/definition/page` | 1.流程定义-分页查询 |
| GET | `/micro-flowable/admin/definition/info` | 2.流程定义-详情 |
| GET | `/micro-flowable/admin/definition/list` | 3.流程定义-列表 |
| GET | `/micro-flowable/admin/deploy/page` | 4.部署记录-分页查询 |
| POST | `/micro-flowable/admin/deploy/remove` | 5.部署记录-删除 |

### 业务端 - 申请（ApplyRest）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/micro-flowable/apply/create` | 1.流程申请-发起 |
| GET | `/micro-flowable/apply/page` | 2.流程申请-我的申请 |
| GET | `/micro-flowable/apply/info` | 3.流程申请-详情 |

### 业务端 - 任务审批（FlowableTaskRest）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/micro-flowable/task/todo/page` | 1.任务审批-待办分页查询 |
| GET | `/micro-flowable/task/done/page` | 2.任务审批-已办分页查询 |
| GET | `/micro-flowable/task/info` | 3.任务审批-详情 |
| POST | `/micro-flowable/task/complete` | 4.任务审批-通过 |
| POST | `/micro-flowable/task/claim` | 5.任务审批-认领 |
| POST | `/micro-flowable/task/unclaim` | 6.任务审批-取消认领 |
| POST | `/micro-flowable/task/reject` | 8.任务审批-驳回 |
| POST | `/micro-flowable/task/transfer` | 9.任务审批-转办 |
| POST | `/micro-flowable/task/delegate` | 10.任务审批-委派 |
| GET | `/micro-flowable/approval/list` | 7.任务审批-意见时间线 |

### 业务端 - 实例历史（InstanceHistoryRest）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/micro-flowable/instance/page` | 1.流程实例-分页查询 |
| GET | `/micro-flowable/instance/info` | 2.流程实例-详情 |
| POST | `/micro-flowable/instance/withdraw` | 6.流程实例-撤回 |
| GET | `/micro-flowable/history/instance/page` | 3.历史实例-分页查询 |
| GET | `/micro-flowable/history/task/page` | 4.历史任务-分页查询 |
| GET | `/micro-flowable/history/activity/list` | 5.历史活动-列表 |

### 异常监控（ErrorLogRest）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/micro-flowable/error/page` | 1.异常日志-分页查询 |
| GET | `/micro-flowable/error/info` | 2.异常日志-详情 |
| POST | `/micro-flowable/error/handle` | 3.异常日志-标记处理 |

## 配置项

```yaml
sh:
  flowable:
    # client 连接配置（FlowableClientProperties）
    base-url: http://sh-flowable-server:8080
    connect-timeout: 5000
    read-timeout: 30000
    # 异常日志配置（FlowableErrorLogProperties）
    error-log:
      enabled: true        # 是否启用异常日志落库
      include-stack: true  # 是否记录异常堆栈
```

## 数据库表

| 表名 | 说明 |
|------|------|
| `flowable_process_design` | 流程设计（设计态） |
| `flowable_node_config` | 节点配置（设计态） |
| `flowable_apply` | 流程申请单（业务态） |
| `flowable_approval` | 审批意见（业务态） |
| `flowable_error_log` | 异常日志 |

建表脚本：`src/main/resources/sql/micro-flowable-ddl.sql`

## 依赖关系

- **sh-flowable-client**（`com.wkclz.flowable:sh-flowable-client:1.0.0-SNAPSHOT`）：5 个 HttpExchange 客户端
- **flowable-bpmn-model / flowable-bpmn-converter**（8.0.0）：BPMN XML 解析（设计上传时提取节点）
- **sh-framework**：BaseEntity / BaseService / BaseMapper / R / PageData 等基础能力

## 开发注意事项

1. **透传端点**：运行态数据（定义/实例/任务/历史）全部实时透传 sh-flowable-server，不在本地缓存
2. **审批意见**：所有审批动作（通过/驳回/转办/委派/认领/撤回）均记录到本地 `flowable_approval` 表
3. **异常拦截**：`FlowableClientWrapper.call()` 统一包装 client 调用，异常时落库 `flowable_error_log` 并抛出业务异常
4. **BPMN 解析**：设计上传/更新时解析 XML 自动生成节点配置，解析失败不阻塞保存
5. **乐观锁**：更新操作必须传 `version` 字段
6. **design_version vs version**：`design_version` 是设计版本号，`version` 是乐观锁字段，注意区分
