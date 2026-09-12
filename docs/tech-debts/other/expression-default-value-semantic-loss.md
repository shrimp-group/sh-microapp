# TD-2026-0912-01 DDL 表达式默认值 round-trip 语义静默损坏

| 属性 | 值 |
|------|------|
| ID | TD-2026-0912-01 |
| 标题 | DDL 表达式默认值 round-trip 语义静默损坏 |
| 状态 | Open |
| 严重程度 | High |
| 所属模块 | micro-dbview/schemadiff |
| 发现日期 | 2026-09-12 |

## 问题描述

DdlParseUtil 解析 `DEFAULT (now())` 时剥掉外层括号，将语义值 `now()` 存入 ColumnDef.defaultValue；SchemaDiffUtil.columnBody 生成 DDL 时，对非数字、非 CURRENT_TIMESTAMP 的默认值一律加单引号，最终生成 `DEFAULT 'now()'`。

该 ALTER 语句在 MySQL 中执行成功（字符串字面量是合法默认值），但列默认值从函数表达式静默变为字符串字面量，round-trip 语义损坏且无任何报错。

## 影响范围

- micro-dbview 表结构对比同步链路（generateSyncDdl 产出的 ADD/MODIFY COLUMN 子句）
- 受影响写法：`DEFAULT (now())`、`DEFAULT (uuid())`、`DEFAULT (unix_timestamp())` 等 MySQL 8.0 表达式默认值
- 不受影响：数字默认值、CURRENT_TIMESTAMP、普通字符串默认值

## 建议方案

- 解析侧保留表达式标记（如不剥外层括号），模型约定"带括号 = 表达式"
- 生成侧对表达式默认值原样输出，不加引号
- 前置条件：先确定 defaultValue 字段的全局语义约定（语义值 or 原文），解析、对比、生成三侧统一后再实施
