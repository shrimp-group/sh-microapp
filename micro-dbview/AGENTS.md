# micro-dbview 模块开发指南

## 模块概述

`micro-dbview` 是数据库管理工具微应用模块，支持管理多个 MySQL 数据库，提供表结构查看、索引查看、SQL 执行、表结构变更等功能。

| 属性 | 值 |
|------|------|
| API 前缀 | `/micro-dbview` |
| 核心依赖 | sh-mybatis, sh-dynamicdb, sh-redis |

## 核心能力

| 功能 | 说明 |
|------|------|
| 数据源管理 | 多 MySQL 数据源配置 CRUD、连接测试 |
| 元数据查询 | 数据库/表/字段/索引列表、建表 DDL 查看 |
| SQL 执行 | 分级权限控制、危险操作检测、结果集限制、执行历史 |
| DDL 操作 | 表单驱动的表结构变更（8 种 DDL + 预览） |
| SQL 提示 | 后端元数据 API 供前端 Monaco Editor 实现补全 |

## 数据库表

| 表名 | 说明 |
|------|------|
| `dbview_datasource` | 数据源配置（JDBC URL、用户名、AES 加密密码） |
| `dbview_datasource_permission` | 数据源权限映射（用户+数据源 → READ_ONLY/READ_WRITE/DDL） |
| `dbview_sql_history` | SQL 执行历史记录 |

## 权限等级

| 等级 | 标识 | 允许的 SQL 类型 |
|------|------|----------------|
| 只读 | READ_ONLY | SELECT, SHOW, DESC, EXPLAIN |
| 读写 | READ_WRITE | READ_ONLY + INSERT, UPDATE, DELETE |
| DDL | DDL | READ_WRITE + ALTER, CREATE, DROP, RENAME |

## 配置项

| 配置键 | 默认值 | 说明 |
|--------|--------|------|
| `sh.dbview.max-rows` | 1000 | SQL 查询默认最大行数 |
| `sh.dbview.max-rows-limit` | 10000 | SQL 查询最大行数上限 |
| `sh.dbview.sql-timeout-seconds` | 30 | SQL 执行超时时间 |
| `sh.dbview.metadata-cache-ttl` | 300 | 元数据缓存 TTL（秒） |
| `sh.dbview.aes-key` | — | 数据源密码加密密钥 |
| `sh.dbview.history-retain-days` | 30 | 执行历史保留天数 |

## API 路由

### 数据源管理 `/micro-dbview/datasource/*`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /datasource/page | 分页查询 |
| GET | /datasource/info | 详情 |
| POST | /datasource/create | 新增 |
| POST | /datasource/update | 更新 |
| POST | /datasource/remove | 删除 |
| GET | /datasource/options | 选项列表 |
| POST | /datasource/test-connection | 测试连接 |

### 权限管理 `/micro-dbview/permission/*`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /permission/page | 分页查询 |
| POST | /permission/create | 新增 |
| POST | /permission/update | 更新 |
| POST | /permission/remove | 删除 |
| GET | /permission/my-permissions | 当前用户权限 |

### 元数据查询 `/micro-dbview/metadata/*`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /metadata/schemas | 数据库列表 |
| GET | /metadata/tables | 表列表 |
| GET | /metadata/table-detail | 表详情 |
| GET | /metadata/columns | 字段列表 |
| GET | /metadata/indexes | 索引列表 |
| GET | /metadata/table-ddl | 建表 DDL |
| POST | /metadata/refresh-cache | 刷新缓存 |

### SQL 执行 `/micro-dbview/sql/*`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /sql/execute | 执行 SQL |
| GET | /sql/history/page | 执行历史分页 |

### DDL 操作 `/micro-dbview/ddl/*`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /ddl/add-column | 添加字段 |
| POST | /ddl/drop-column | 删除字段 |
| POST | /ddl/modify-column | 修改字段 |
| POST | /ddl/add-index | 添加索引 |
| POST | /ddl/drop-index | 删除索引 |
| POST | /ddl/rename-table | 重命名表 |
| POST | /ddl/comment-table | 修改表注释 |
| POST | /ddl/comment-column | 修改字段注释 |
| POST | /ddl/preview | DDL 预览 |
| POST | /ddl/execute-ddl | 执行 DDL 字符串（预览确认后执行） |

## 交互工作流

用户通过以下步骤完成表结构浏览与变更：

```
步骤 1：选择数据源 → GET /datasource/options
步骤 2：浏览表列表 → GET /metadata/tables?datasourceId=1
步骤 3：查看表详情 → GET /metadata/table-detail?datasourceId=1&tableName=xxx
步骤 4：查看建表语句 → GET /metadata/table-ddl?datasourceId=1&tableName=xxx
步骤 5：查看字段详情 → GET /metadata/columns?datasourceId=1&tableName=xxx
步骤 6：调整字段 → POST /ddl/preview 生成 DDL 预览
步骤 7：执行变更 → POST /ddl/execute-ddl 执行确认后的 DDL
```

**DDL 预览与执行闭环**：`preview` 生成 DDL → 用户确认 → `execute-ddl` 执行 DDL 字符串，确保预览和执行一致。执行成功后自动刷新元数据缓存。

**COMMENT_COLUMN 自动补全**：修改字段注释时，若未提供 columnType，后端自动从元数据获取当前字段类型和 nullable 属性，生成完整 MODIFY COLUMN 语句。

## sh-dynamicdb 集成

本模块通过实现 `DynamicDataSourceFactory` 接口集成 sh-dynamicdb：

- `DbviewDataSourceFactory`：根据 datasourceId 从 `dbview_datasource` 表加载配置创建数据源
- 所有对目标数据库的操作在独立线程中执行，通过 `DynamicDataSourceHolder.set(key)` + try/finally 清理
- 数据源配置变更时自动销毁旧连接池

## 安全设计

- 密码 AES 加密存储（`sh.dbview.aes-key` 配置密钥）
- SQL 执行权限分级控制
- 危险 SQL 检测（UPDATE/DELETE 无 WHERE、DROP TABLE、TRUNCATE）
- 结果集行数限制（默认 1000，上限 10000）
- 禁止一次执行多条 SQL
