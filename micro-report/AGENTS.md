# micro-report 报表模块开发指南

## 模块概述

micro-report 是低代码报表模块，提供基于 SQL 脚本的报表定义、管理与查询执行能力。用户编写 SQL 查询脚本，系统自动解析参数和结果字段，支持动态查询、分页、参数校验和 Excel 导出。

| 属性 | 值 |
|------|------|
| 模块名 | micro-report |
| API 前缀 | `/micro-report` |
| 表前缀 | `report_` |
| 实体前缀 | `Report` |
| 核心依赖 | sh-mybatis, sh-redis, sh-spring, sh-web, easyexcel, jsqlparser |

---

## 数据库表

| 表名 | 说明 | 实体类 |
|------|------|--------|
| report_definition | 报表定义（核心主表） | ReportDefinition |
| report_definition_his | 报表定义历史版本 | ReportDefinitionHis |
| report_definition_param | 报表参数定义 | ReportDefinitionParam |
| report_definition_result | 报表结果字段定义 | ReportDefinitionResult |

### 表间关系

```
report_definition (1) ── (N) report_definition_param
     report_code ──────────> report_code

report_definition (1) ── (N) report_definition_result
     report_code ──────────> report_code

report_definition (1) ── (N) report_definition_his
     id ─────────────────────> data_id
```

---

## 目录结构

```
micro-report/
├── pom.xml
├── AGENTS.md
└── src/main/
    ├── java/com/wkclz/micro/report/
    │   ├── ReportAutoConfig.java         # 自动配置
    │   ├── bean/
    │   │   ├── entity/
    │   │   │   ├── ReportDefinition.java
    │   │   │   ├── ReportDefinitionHis.java
    │   │   │   ├── ReportDefinitionParam.java
    │   │   │   └── ReportDefinitionResult.java
    │   │   └── dto/
    │   │       ├── ReportDefinitionDto.java
    │   │       ├── ReportDefinitionParamDto.java
    │   │       └── ReportDefinitionResultDto.java
    │   ├── mapper/
    │   │   ├── ReportDefinitionMapper.java
    │   │   ├── ReportDefinitionHisMapper.java
    │   │   ├── ReportDefinitionParamMapper.java
    │   │   └── ReportDefinitionResultMapper.java
    │   ├── service/
    │   │   ├── ReportDefinitionService.java   # 报表定义 CRUD + SQL 测试
    │   │   ├── ReportDefinitionHisService.java
    │   │   ├── ReportDefinitionParamService.java  # 参数 CRUD + 自动提取
    │   │   ├── ReportDefinitionResultService.java # 结果字段 CRUD + 自动提取
    │   │   └── ReportExecService.java         # 报表执行引擎
    │   ├── helper/
    │   │   ├── ReportSqlHelper.java          # SQL 解析与执行引擎
    │   │   └── ReportExportHelper.java       # Excel 导出
    │   ├── rest/
    │   │   ├── Route.java                    # 路由常量
    │   │   ├── ReportDefinitionRest.java
    │   │   ├── ReportDefinitionHisRest.java
    │   │   ├── ReportDefinitionParamRest.java
    │   │   ├── ReportDefinitionResultRest.java
    │   │   └── ReportExecRest.java
    │   └── cache/
    │       └── ReportCache.java              # 报表定义缓存
    └── resources/
        ├── META-INF/spring/
        │   └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
        └── mapper/
            ├── ReportDefinitionMapper.xml
            ├── ReportDefinitionHisMapper.xml
            ├── ReportDefinitionParamMapper.xml
            └── ReportDefinitionResultMapper.xml
```

---

## API 端点

### 管理端

| 端点 | 方法 | 说明 |
|------|------|------|
| `/definition/page` | GET | 报表定义分页查询 |
| `/definition/detail` | GET | 报表定义详情 |
| `/definition/create` | POST | 新增报表定义 |
| `/definition/update` | POST | 修改报表定义 |
| `/definition/remove` | POST | 删除报表定义 |
| `/definition/test` | POST | SQL 测试 |
| `/definition/his/page` | GET | 历史版本分页 |
| `/definition/his/detail` | GET | 历史版本详情 |
| `/definition/param/list` | GET | 参数列表 |
| `/definition/param/create` | POST | 新增参数 |
| `/definition/param/update` | POST | 修改参数 |
| `/definition/param/remove` | POST | 删除参数 |
| `/definition/param/extract` | POST | 参数自动提取 |
| `/definition/result/list` | GET | 结果字段列表 |
| `/definition/result/create` | POST | 新增结果字段 |
| `/definition/result/update` | POST | 修改结果字段 |
| `/definition/result/remove` | POST | 删除结果字段 |
| `/definition/result/extract` | POST | 结果字段自动提取 |

### 执行端

| 端点 | 方法 | 说明 |
|------|------|------|
| `/exec/options` | GET | 报表选项列表 |
| `/exec/info` | GET | 报表详情（含参数和结果元数据） |
| `/exec/query` | GET | 执行报表查询 |
| `/exec/export` | POST | 导出 Excel |

---

## 核心机制

### SQL 执行引擎 (ReportSqlHelper)

- **SQL 安全检查**：使用 Druid SQLParser，仅允许 SELECT 语句
- **参数提取**：正则匹配 `#{paramName}` 占位符
- **结果列提取**：使用 JSqlParser 解析 SELECT 列名
- **动态 SQL 执行**：将 SQL 注册为 MyBatis MappedStatement，通过 SqlSession 执行
- **分页查询**：支持自动 COUNT 和自定义 COUNT 脚本
- **MyBatis 标签清理**：`cleanMyBatisTags` 方法清理动态标签前，先处理 CDATA 包裹和 XML 实体转义（`&lt;`/`&gt;`/`&amp;`/`&apos;`/`&quot;`），确保转义形式的标签（如 `&lt;if test="..."&gt;`）也能被正确移除；同时支持 `<choose>/<when>/<otherwise>/<trim>` 等标签
- **驼峰转换**：支持自动将下划线命名转为驼峰

### 参数校验

- **必填校验**：required 字段控制
- **类型校验**：number 类型校验数字格式
- **JS 脚本校验**：validateScript 字段支持自定义 JS 校验逻辑

### 缓存机制 (ReportCache)

- 启动时加载所有启用的报表定义到内存
- 通过 Redis Pub/Sub 广播缓存刷新，3 秒防抖
- 管理端增删改操作后自动触发缓存刷新

### Excel 导出 (ReportExportHelper)

- 基于 EasyExcel，根据结果字段定义生成表头
- 支持自适应列宽

---

## 业务流程

1. **创建报表定义** -> 编写 SQL 脚本，设置返回类型（OBJECT/LIST/PAGE）
2. **SQL 测试** -> 验证 SQL 是否可用
3. **提取参数** -> 从 SQL 自动提取参数，或手动添加
4. **提取结果字段** -> 从 SQL 自动提取结果列，或手动添加
5. **启用报表** -> 设置 enableFlag=1
6. **执行报表** -> 通过 /exec/query 端点查询
7. **导出 Excel** -> 通过 /exec/export 端点导出

---

## UI 预留

前端页面结构设计（供 sh-demo-ui 实现）：

```
views/report/
├── definition/                    # 报表定义管理
│   ├── index.vue                  # 列表页
│   └── components/
│       ├── edit.vue               # 编辑弹窗（含 Tab 页签）
│       ├── basic.vue              # 基础信息 Tab
│       ├── sql.vue                # SQL 脚本 Tab（含测试按钮）
│       ├── paramList.vue          # 参数列表 Tab
│       ├── paramEdit.vue          # 参数编辑弹窗
│       ├── resultList.vue         # 结果字段列表 Tab
│       └── resultEdit.vue         # 结果字段编辑弹窗
└── exec/                          # 报表执行
    ├── index.vue                  # 主页面（左右分栏）
    └── components/
        ├── reportList.vue         # 左侧报表选择列表
        └── reportExec.vue         # 右侧动态查询+结果展示+导出
```

前端枚举：

| 枚举 | 值 |
|------|------|
| ReturnType | OBJECT=对象, LIST=列表, PAGE=分页 |
| FieldType | string=字符串, number=数字, date=日期, datetime=日期时间 |
| ParamFormType | TEXT, NUMBER, DATE, DATETIME, SELECT, SELECT_MULTIPLE, RADIO, TEXTAREA |
| ResultFormType | TEXT, NUMBER, DATE, DATETIME, IMAGE |

---

**最后更新时间**: 2026-06-17
