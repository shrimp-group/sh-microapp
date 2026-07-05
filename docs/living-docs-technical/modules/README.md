# 模块索引

> 最后更新：`2026-06-11`

## 数据管理类

| 模块名 | 职责 | 技术栈 | 入口路径 |
|--------|------|--------|----------|
| micro-dict | 数据字典管理 | Spring Boot + MyBatis + Redis | /micro-dict |
| micro-seq | 序列号生成 | Spring Boot + MyBatis + Redis | /micro-seq |
| micro-material | 物料管理 | Spring Boot + MyBatis + Redis | /micro-material |
| micro-form | 表单规则管理 | Spring Boot + MyBatis + AOP | /micro-form |

## 文件文档类

| 模块名 | 职责 | 技术栈 | 入口路径 |
|--------|------|--------|----------|
| micro-fileos | 文件存储 | Spring Boot + OSS/S3 | /micro-file |
| micro-pdf | PDF生成 | Spring Boot + Thymeleaf + Flying Saucer | /micro-pdf |

## 消息通知类

| 模块名 | 职责 | 技术栈 | 入口路径 |
|--------|------|--------|----------|
| micro-msg | 消息通知 | Spring Boot + MyBatis | /micro-msg |
| micro-mask | 数据脱敏 | Spring Boot + ResponseAdvice | /micro-mask |

## 审计校验类

| 模块名 | 职责 | 技术栈 | 入口路径 |
|--------|------|--------|----------|
| micro-audit | 变更审计 | Spring Boot + MyBatis | /micro-audit |
| micro-rmcheck | 删除合规校验 | Spring Boot + MyBatis | /micro-rmcheck |

## 支付类

| 模块名 | 职责 | 技术栈 | 入口路径 |
|--------|------|--------|----------|
| micro-pay | 支付集成 | Spring Boot + 微信支付/支付宝 SDK | /micro-pay |

## 微信类

| 模块名 | 职责 | 技术栈 | 入口路径 |
|--------|------|--------|----------|
| micro-wxapp | 微信小程序 | Spring Boot + WxJava | /micro-wxapp |
| micro-wxmp | 微信公众号 | Spring Boot + WxJava | /micro-wxmp |

## 基础设施类

| 模块名 | 职责 | 技术栈 | 入口路径 |
|--------|------|--------|----------|
| micro-liteflow | 规则引擎 | Spring Boot + LiteFlow | /micro-liteflow |
| micro-fun | 函数管理 | Spring Boot + ScriptEngine | /micro-fun |
| micro-k8s | K8s管理 | Spring Boot + Kubernetes Client | /micro-k8s |
| micro-autotest | 自动化测试 | Spring Boot + Mock | /micro-autotest |
| micro-report | 报表管理 | Spring Boot + MyBatis + Excel | /micro-report |
| micro-dbview | 数据视图 | Spring Boot + 动态数据源 | /micro-dbview |
