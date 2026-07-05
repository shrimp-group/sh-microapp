# sh-microapp 项目上下文

> 本文件为 AI 助手提供项目全局上下文，帮助 AI 快速理解项目全貌。

## 项目概述
sh-microapp 是微应用集合项目，每个模块承接相对独立的业务能力，被主应用依赖后使用。基于 Spring Boot 4.x + Java 25 构建，统一依赖 sh-framework 框架。

## 技术栈
- 语言：Java 25
- 运行时：JDK 25
- 框架：Spring Boot 4.0.6
- 构建：Maven
- ORM：MyBatis 4.0.1 + PageHelper
- 缓存：Redis (Lettuce)

## 核心业务领域
- **数据管理**：字典(micro-dict)、序列号(micro-seq)、物料(micro-material)、表单规则(micro-form)
- **文件文档**：文件存储(micro-fileos)、PDF生成(micro-pdf)
- **消息通知**：消息(micro-msg)、数据脱敏(micro-mask)
- **审计校验**：变更审计(micro-audit)、删除合规(micro-rmcheck)
- **支付**：微信支付/支付宝(micro-pay)
- **微信**：小程序(micro-wxapp)、公众号(micro-wxmp)
- **基础设施**：规则引擎(micro-liteflow)、函数管理(micro-fun)、K8s(micro-k8s)、自动化测试(micro-autotest)、报表(micro-report)、数据视图(micro-dbview)

## 关键约束
- 所有模块必须通过 AutoConfiguration.imports 注册自动配置
- 实体必须继承 BaseEntity，Mapper 必须继承 BaseMapper，Service 必须继承 BaseService
- REST 接口返回 R<T> 统一响应对象
- 逻辑删除使用 deleted 字段，乐观锁使用 version 字段
- 缓存使用 Redis Pub/Sub 广播刷新，3 秒防抖
- 所有第三方依赖版本由 sh-bom 统一管理

## 外部依赖
- sh-framework：基础框架（sh-core、sh-mybatis、sh-web、sh-redis、sh-spring 等）
- Spring Boot 4.0.6：应用框架
- MyBatis 4.0.1：ORM 框架
- Redis (Lettuce)：缓存与消息
- PageHelper：分页查询
- 微信支付 SDK / 支付宝 SDK：支付集成
- WxJava：微信小程序/公众号 SDK
- LiteFlow：规则引擎
- XXL-Job：定时任务
