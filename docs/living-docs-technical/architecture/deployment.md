# 部署架构

> 最后更新：`2026-06-11`

## 部署方式

sh-microapp 作为微应用集合，以 Maven 依赖方式被主应用引入，不独立部署。

## 模块加载机制

- 通过 Spring Boot AutoConfiguration 自动配置
- 每个模块的 XxxAutoConfig 注册于 META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
- 主应用只需引入 Maven 依赖即可自动加载模块
