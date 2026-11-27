---
name: micro-report
description: 当需要操作 micro-report 模块时使用此技能 —— 低代码 SQL 报表模块。当用户需要实现报表定义 CRUD、SQL 脚本测试、参数/结果字段自动提取与校验、动态查询执行、分页、Excel 导出、报表定义缓存（ReportCache），或修改 com.wkclz.micro.report 包下任何代码时触发。
---

# Micro-Report 模块

低代码报表模块：用户编写 SQL 查询脚本，系统自动解析参数（`#{paramName}`）和结果字段，支持动态查询、分页、参数校验和 Excel 导出。

## 适用场景

- 修改 `micro-report` 包（com.wkclz.micro.report）下任何 Java 文件
- 报表定义/历史版本/参数/结果字段的管理
- 调试 SQL 执行引擎（ReportSqlHelper）、参数校验、导出、缓存刷新问题

## 架构概览

```
Rest             ReportDefinitionRest / ReportDefinitionHisRest
                 ReportDefinitionParamRest / ReportDefinitionResultRest / ReportExecRest
    ↓
Service          ReportDefinitionService / ReportDefinitionParamService
                 ReportDefinitionResultService / ReportExecService
    ↓
Helper           ReportSqlHelper（SQL 解析与执行引擎）/ ReportExportHelper（Excel 导出）
    ↓
Mapper           ReportDefinitionMapper / ReportDefinitionHisMapper
                 ReportDefinitionParamMapper / ReportDefinitionResultMapper
缓存             ReportCache（启动加载 + Redis Pub/Sub 刷新，3 秒防抖）
```

**核心依赖**：sh-mybatis, sh-redis, sh-spring, sh-web, easyexcel, jsqlparser

## 数据实体（4 个，表前缀 report_）

| 实体 | 表 | 说明 |
|------|------|------|
| ReportDefinition | report_definition | 报表定义（核心主表，SQL 脚本 + 返回类型 OBJECT/LIST/PAGE） |
| ReportDefinitionHis | report_definition_his | 报表定义历史版本（data_id 关联主表 id） |
| ReportDefinitionParam | report_definition_param | 报表参数定义（report_code 关联） |
| ReportDefinitionResult | report_definition_result | 报表结果字段定义（report_code 关联） |

## REST API 端点（前缀 /micro-report，共 22 个）

### 管理端（ReportDefinitionRest 等）

| 端点 | 方法 | 说明 |
|------|------|------|
| `/definition/page` `/detail` | GET | 报表定义分页/详情 |
| `/definition/create` `/update` `/remove` | POST | 定义创建/修改/删除 |
| `/definition/test` | POST | SQL 测试 |
| `/definition/his/page` `/his/detail` | GET | 历史版本分页/详情 |
| `/definition/param/list` | GET | 参数列表 |
| `/definition/param/create` `/update` `/remove` | POST | 参数新增/修改/删除 |
| `/definition/param/extract` | POST | 参数自动提取（正则解析 `#{paramName}`） |
| `/definition/result/list` | GET | 结果字段列表 |
| `/definition/result/create` `/update` `/remove` | POST | 结果字段新增/修改/删除 |
| `/definition/result/extract` | POST | 结果字段自动提取（JSqlParser 解析 SELECT 列） |

### 执行端（ReportExecRest）

| 端点 | 方法 | 说明 |
|------|------|------|
| `/exec/options` | GET | 报表选项列表 |
| `/exec/info` | GET | 报表详情（含参数和结果元数据） |
| `/exec/query` | GET | 执行报表查询 |
| `/exec/export` | POST | 导出 Excel |

## 核心机制速记

- SQL 安全检查（ReportSqlHelper）：使用 Druid SQLParser，仅允许 SELECT 语句
- 参数提取：正则匹配 `#{paramName}` 占位符
- 结果列提取：JSqlParser 解析 SELECT 列名
- 动态 SQL 执行：将 SQL 注册为 MyBatis MappedStatement，通过 SqlSession 执行
- 分页查询：支持自动 COUNT 和自定义 COUNT 脚本
- MyBatis 标签清理：`cleanMyBatisTags` 清理动态标签前先处理 CDATA 包裹和 XML 实体转义（`&lt;`/`&gt;`/`&amp;`/`&apos;`/`&quot;`），确保转义形式的标签（如 `&lt;if test="..."&gt;`）也能被正确移除；支持 `<choose>/<when>/<otherwise>/<trim>` 标签
- 驼峰转换：支持自动将下划线命名转为驼峰
- 参数校验：required 必填校验；number 类型校验数字格式；validateScript 支持自定义 JS 校验逻辑
- 缓存（ReportCache）：启动时加载所有启用的报表定义到内存；Redis Pub/Sub 广播缓存刷新、3 秒防抖；管理端增删改操作后自动触发刷新
- Excel 导出（ReportExportHelper）：基于 EasyExcel，按结果字段定义生成表头，自适应列宽

## 核心工作流

1. **创建报表定义** → 编写 SQL 脚本，设置返回类型（OBJECT/LIST/PAGE）
2. **SQL 测试** → `/definition/test` 验证 SQL 可用性
3. **提取参数/结果字段** → `/definition/param/extract`、`/definition/result/extract` 自动提取，或手动添加
4. **启用报表** → 设置 enableFlag=1（触发缓存刷新）
5. **执行报表** → `/exec/query` 动态查询
6. **导出 Excel** → `/exec/export`

## 前端枚举（供 UI 实现）

| 枚举 | 值 |
|------|------|
| ReturnType | OBJECT=对象, LIST=列表, PAGE=分页 |
| FieldType | string=字符串, number=数字, date=日期, datetime=日期时间 |
| ParamFormType | TEXT, NUMBER, DATE, DATETIME, SELECT, SELECT_MULTIPLE, RADIO, TEXTAREA |
| ResultFormType | TEXT, NUMBER, DATE, DATETIME, IMAGE |

## 常见问题

| 问题 | 原因/解决 |
|------|-----------|
| SQL 保存/执行报非 SELECT | 引擎仅允许 SELECT 语句（Druid 解析校验） |
| 查询结果未生效新定义 | 报表定义有内存缓存，确认增删改后 Redis Pub/Sub 频道正常通信（3 秒防抖） |
| 参数校验不通过 | 检查 required 必填、number 类型格式、validateScript 自定义 JS 逻辑 |
| 动态标签未清理 | 确认标签为标准 MyBatis 动态标签；XML 实体转义形式已支持（如 `&lt;if&gt;`） |
| 导出表头与预期不符 | 表头按结果字段定义生成，检查 result 字段配置顺序与 label |

---
更新时间：2026-11-28
