# Story: SQL 报表

## 描述
作为报表管理员，编写 SQL 查询脚本定义报表，系统自动解析参数与结果字段，配置参数校验规则后供业务用户动态查询、分页与 Excel 导出。

## 参与者
| 角色 | 说明 |
|------|------|
| 报表管理员 | 创建报表定义、编写 SQL、管理参数与结果字段 |
| 业务用户 | 通过执行端选择报表、填参数、查询结果、导出 Excel |
| 系统 | micro-report 后端服务：SQL 解析引擎 + 报表定义缓存 + Excel 导出 |

## 流程图
```mermaid
flowchart TD
    A[报表管理员创建报表定义] --> B[编写 SQL 脚本<br/>设置返回类型 OBJECT/LIST/PAGE]
    B --> C[POST /definition/test SQL 测试<br/>仅允许 SELECT]
    C --> D[提取参数 /definition/param/extract<br/>正则匹配 #{paramName} 占位符]
    C --> E[提取结果字段 /definition/result/extract<br/>JSqlParser 解析 SELECT 列名]
    D --> F[可手动增改参数<br/>required/类型/JS 校验脚本 validateScript]
    E --> G[可手动增改结果字段<br/>fieldType: string/number/date/datetime]
    F --> H[启用报表 enableFlag=1]
    G --> H
    H --> I[ReportCache 加载<br/>Redis Pub/Sub 广播刷新 3 秒防抖]
    I --> J[业务用户查询]
    J --> K[GET /exec/query<br/>参数必填/number/JS 校验]
    K --> L{返回类型}
    L -->|OBJECT| M[返回单对象]
    L -->|LIST| N[返回列表]
    L -->|PAGE| O[分页：自动 COUNT 或自定义 COUNT 脚本]
    M --> P[POST /exec/export<br/>EasyExcel 按结果字段生成表头 + 自适应列宽]
    N --> P
    O --> P
```

## 验收标准
- [ ] 报表定义管理：Given 报表定义, When page/detail/create/update/remove, Then 定义 CRUD 正常且增删改后触发 ReportCache 刷新（Redis Pub/Sub，3 秒防抖）
- [ ] SQL 测试：Given SQL 脚本, When POST /definition/test, Then 仅允许 SELECT 语句，非 SELECT 拒绝
- [ ] 参数自动提取：Given SQL 含 #{paramName} 占位符, When POST /definition/param/extract, Then 自动解析参数生成参数定义，也可手动 create/update/remove
- [ ] 结果字段自动提取：Given SELECT 语句, When POST /definition/result/extract, Then JSqlParser 自动解析结果列生成结果字段定义，也可手动维护
- [ ] 参数校验：Given 参数定义 required/number 类型/validateScript, When /exec/query 传参, Then 必填缺失、number 格式错误、JS 校验不通过均拒绝执行
- [ ] 动态查询：Given 启用的报表与合法参数, When GET /exec/query, Then SQL 注册为 MyBatis MappedStatement 动态执行；OBJECT/LIST/PAGE 三种返回类型正确；PAGE 支持自动 COUNT 与自定义 COUNT 脚本；MyBatis 动态标签（if/choose/when/otherwise/trim 及转义形式）被清理后正确执行；下划线列名自动转驼峰
- [ ] 执行元数据：Given GET /exec/info, Then 返回报表详情含参数与结果字段元数据；GET /exec/options 返回报表选项列表
- [ ] Excel 导出：Given 查询结果, When POST /exec/export, Then EasyExcel 按结果字段定义生成表头，支持自适应列宽
- [ ] 历史版本：Given 定义被修改, When 查询 /definition/his/page|detail, Then 可追溯历史版本

## 关联模块
- micro-report

## 关联 API
- `/micro-report/definition/page|detail|create|update|remove|test`
- `/micro-report/definition/his/page|detail`
- `/micro-report/definition/param/list|create|update|remove|extract`
- `/micro-report/definition/result/list|create|update|remove|extract`
- `/micro-report/exec/options|info|query|export`

## 优先级
P1

## 状态
已完成

## 备注
- 表：report_definition / report_definition_his / report_definition_param / report_definition_result（前缀 report_）
- 核心依赖：sh-mybatis、sh-redis、easyexcel、Druid（SQL 安全检查 isPureSelect，仅允许 SELECT）、jsqlparser（结果列提取）
- 前端页面结构预留（views/report/definition + exec），由业务前端工程实现
