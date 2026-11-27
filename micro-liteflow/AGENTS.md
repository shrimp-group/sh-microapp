# micro-liteflow 模块开发指南

## 模块概述

micro-liteflow 是 sh-microapp 微应用集合中的规则引擎模块，基于 LiteFlow（com.yomahub）实现规则链（Chain）与脚本（Script）的数据库化管理，通过 `liteflow-rule-sql` 的 SQL 规则源 + 轮询刷新机制，让规则与脚本无需重启即可生效。

- **GroupId**: `com.wkclz.microapp`
- **ArtifactId**: `micro-liteflow`
- **API 前缀**: `/micro-liteflow`
- **外部依赖**: `liteflow-spring-boot-starter` / `liteflow-rule-sql` / `liteflow-script-groovy` / `liteflow-script-javax` / `liteflow-script-aviator`（版本由 sh-bom 统一管理）

## 架构设计

```
宿主应用引入 micro-liteflow
  ├── LiteFlowAutoConfig      # @ComponentScan + @MapperScan 自动装配
  ├── liteflow-rule-sql       # SQL 规则源：读取 liteflow_chain / liteflow_script 装载规则
  ├── 轮询刷新（polling）      # 每 59s 轮询 DB，规则变更自动生效（无需重启）
  ├── REST 管理端             # 规则链 / 脚本 CRUD（维护 DB 数据）
  └── 脚本引擎                # Groovy / Javax(JS) / Aviator 三种脚本语言支持
```

| 组件 | 职责 |
|------|------|
| `LiteflowChainService` | 规则链单表服务（extends BaseService），分页/增删改 |
| `LiteflowScriptService` | 脚本单表服务（extends BaseService），分页/增删改 |
| `liteflow-rule-sql` | LiteFlow 官方 SQL 规则源，按 `application.yml` 映射字段读取两张表 |
| `CommonLiteFlowRest` | 预留的通用查询控制器（当前无端点） |
| `ACmp` / `BCmp` / `CCmp` / `LiteFlowDemo` / `LiteFlowTestRest` | 演示组件（示例代码，展示 EL 规则与节点用法） |

## 目录结构

```
micro-liteflow/src/main/
├── java/com/wkclz/micro/liteflow/
│   ├── LiteFlowAutoConfig.java        # 自动配置（@ComponentScan + @MapperScan）
│   ├── bean/
│   │   ├── entity/
│   │   │   ├── LiteflowChain.java     # 规则链实体（表 liteflow_chain）
│   │   │   └── LiteflowScript.java    # 脚本实体（表 liteflow_script）
│   │   ├── req/                       # Create/Update/Page/Info Req（Chain、Script 各 4 个）
│   │   ├── resp/                      # Page/Info Resp（Chain、Script 各 2 个）
│   │   └── dto/                       # LiteflowChainDto / LiteflowScriptDto
│   ├── mapper/
│   │   ├── LiteflowChainMapper.java
│   │   └── LiteflowScriptMapper.java
│   ├── demo/                          # 演示组件（示例代码，保留）
│   │   ├── ACmp.java / BCmp.java / CCmp.java   # 演示节点（@Component "a"/"b"/"c"）
│   │   └── LiteFlowDemo.java          # 演示执行器（注入 FlowExecutor 执行 chain1）
│   ├── rest/
│   │   ├── Route.java                 # 路由常量（@Router）
│   │   ├── LiteflowChainRest.java     # 规则链 CRUD
│   │   ├── LiteflowScriptRest.java    # 脚本 CRUD
│   │   ├── CommonLiteFlowRest.java    # 通用查询（预留，暂无端点）
│   │   └── LiteFlowTestRest.java      # 演示测试端点（GET /public/liteflow/test）
│   └── service/
│       ├── LiteflowChainService.java
│       └── LiteflowScriptService.java
└── resources/
    ├── config/application.yml         # liteflow SQL 规则源配置
    ├── mapper/                        # 分页查询 XML
    └── META-INF/spring/...AutoConfiguration.imports
```

## API 端点清单

前缀 `/micro-liteflow`，共 10 个端点：

| 分组 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 规则链 | GET | `/chain/page` | 规则链分页（支持 chainName / namespace 过滤） |
| 规则链 | GET | `/chain/info` | 规则链详情 |
| 规则链 | POST | `/chain/create` | 创建规则链（enable 为空时默认 1） |
| 规则链 | POST | `/chain/update` | 修改规则链（copyIfNotNull 增量更新） |
| 规则链 | POST | `/chain/remove` | 删除规则链（逻辑删除） |
| 脚本 | GET | `/script/page` | 脚本分页（支持 scriptId / scriptName 过滤） |
| 脚本 | GET | `/script/info` | 脚本详情 |
| 脚本 | POST | `/script/create` | 创建脚本（enable 为空时默认 1） |
| 脚本 | POST | `/script/update` | 修改脚本 |
| 脚本 | POST | `/script/remove` | 删除脚本 |

## 核心机制

### SQL 规则源（liteflow-rule-sql）

`src/main/resources/config/application.yml` 关键配置：

```yaml
liteflow:
  print-banner: false
  rule-source-ext-data-map:
    applicationName: demo
    sqlLogEnabled: false
    pollingEnabled: true          # 开启轮询自动刷新
    pollingIntervalSeconds: 59    # 轮询间隔 59s
    pollingStartSeconds: 59
    chainTableName: liteflow_chain
    chainCustomSql: SELECT chain_name,chain_desc,el_data,route,namespace
                    FROM liteflow_chain WHERE deleted = 0 AND enable = 1
    scriptTableName: liteflow_script
    scriptCustomSql: SELECT script_id,script_name,script_data,script_type,script_language
                     FROM liteflow_script WHERE deleted = 0 AND enable = 1
```

- 规则加载由 LiteFlow 完成，仅 `enable = 1` 且未逻辑删除的数据生效
- 管理端 REST 维护 DB 数据，引擎每 59s 轮询刷新，规则变更免重启生效
- 脚本语言由 `script_language` 决定，运行时支持 Groovy / Javax(JS) / Aviator

### 数据库表

**liteflow_chain**：`chain_name`（规则名称）、`chain_desc`（描述）、`el_data`（EL 规则数据）、`route`（路由）、`namespace`（命名空间）、`enable`（状态）

**liteflow_script**：`script_id`（脚本 ID）、`script_name`（名称）、`script_data`（脚本数据）、`script_type`（脚本类型）、`script_language`（脚本语言）、`enable`（可用状态）

两表均继承 BaseEntity 基础字段（id/sort/create_*/update_*/remark/version/deleted）。

## 依赖关系

- **LiteFlow 全家桶**：`liteflow-spring-boot-starter`（引擎）、`liteflow-rule-sql`（SQL 规则源）、`liteflow-script-groovy` / `liteflow-script-javax` / `liteflow-script-aviator`（脚本引擎）
- **sh-web**：R / PageData / RemoveReq 等 Web 基础能力（传递引入 sh-core / sh-mybatis / sh-tool）
- **被依赖**：工程内暂无其他 micro-* 模块依赖本模块；宿主应用按需引入后直接使用 LiteFlow `FlowExecutor` 执行规则链

## 开发注意事项

1. 宿主应用需保证数据源可用，`liteflow-rule-sql` 依赖 Spring DataSource 读取规则表
2. `applicationName` 当前为 `demo`，宿主接入时应按应用名调整
3. Service 的 `duplicateCheck` 为代码生成器预留骨架（当前恒通过），如需唯一性校验需自行补齐唯一条件
4. 修改规则/脚本后最长 59s（轮询间隔）后生效，非实时
5. 异常处理统一使用 `ValidationException.of(...)`，REST 返回 `R<T>`

---

**最后更新时间**: 2026-11-28
