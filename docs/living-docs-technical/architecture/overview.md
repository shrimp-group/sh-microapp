# 架构概览

> 最后更新：`2026-06-11`

## 系统架构

sh-microapp 采用微应用集合架构，每个模块承接相对独立的业务能力，被主应用依赖后使用。

```
主应用 (业务系统)
    ↓ 依赖
sh-microapp (微应用集合)
    ├── 数据管理类: micro-dict, micro-seq, micro-material, micro-form
    ├── 文件文档类: micro-fileos, micro-pdf
    ├── 消息通知类: micro-msg, micro-mask
    ├── 审计校验类: micro-audit, micro-rmcheck
    ├── 支付类: micro-pay
    ├── 微信类: micro-wxapp, micro-wxmp
    └── 基础设施类: micro-liteflow, micro-fun, micro-k8s, micro-autotest, micro-report, micro-dbview
    ↓ 依赖
sh-framework (基础框架)
```

## 技术选型

| 层次 | 技术 | 说明 |
|------|------|------|
| 运行时 | JDK 25 | Java 25 |
| 框架 | Spring Boot 4.0.6 | 应用框架 |
| ORM | MyBatis 4.0.1 + PageHelper | 数据访问 |
| 缓存 | Redis (Lettuce) | 缓存与消息 |
| 构建 | Maven | 项目构建 |
