# 依赖索引

> 最后更新：`2026-06-11`

## 框架依赖

| 依赖名 | 版本 | 用途 |
|--------|------|------|
| sh-bom | 5.0.0-SNAPSHOT | BOM 版本管理 |
| sh-core | 5.0.0-SNAPSHOT | 核心基础（BaseEntity、R、CommonException） |
| sh-tool | 5.0.0-SNAPSHOT | 工具集（加密、字符串、日期） |
| sh-mybatis | 5.0.0-SNAPSHOT | ORM（BaseMapper、BaseService） |
| sh-web | 5.0.0-SNAPSHOT | Web 扩展（ErrorHandler、RestHelper） |
| sh-redis | 5.0.0-SNAPSHOT | Redis（RedisHelper、RedisLock） |
| sh-spring | 5.0.0-SNAPSHOT | Spring 扩展（SnowflakeHelper、MailUtil） |

## 第三方依赖

| 依赖名 | 版本 | 用途 |
|--------|------|------|
| Spring Boot | 4.0.6 | 应用框架 |
| MyBatis | 4.0.1 | ORM 框架 |
| PageHelper | - | 分页查询 |
| Lettuce | - | Redis 客户端 |
| WxJava | - | 微信 SDK |
| LiteFlow | - | 规则引擎 |
| XXL-Job | - | 定时任务 |

## 模块间依赖

| 模块 | 依赖模块 | 说明 |
|------|----------|------|
| micro-form | micro-dict | 表单字典选项 |
| micro-mask | micro-dict | 脱敏字典 |
| micro-seq | micro-dict | 序列号字典类型 |
| micro-pdf | micro-fileos | PDF 文件存储 |
| micro-wxapp | micro-fileos | 小程序媒体上传 |
