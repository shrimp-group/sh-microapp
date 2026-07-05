# 业务模块索引

> 最后更新：`2026-06-11`

| 模块名 | 业务职责 | 关联技术模块 |
|--------|----------|-------------|
| 数据字典 | 管理系统枚举数据，支持跨环境复制与缓存同步 | micro-dict |
| 序列号 | 按业务类型生成唯一序列号 | micro-seq |
| 物料管理 | 物料/物料组/版本/关联的完整生命周期管理 | micro-material |
| 表单规则 | 动态表单校验规则管理，AOP 自动拦截 | micro-form |
| 文件存储 | 多 OSS 提供商文件上传/下载/签名 | micro-fileos |
| PDF生成 | 基于模板的 PDF 文档生成 | micro-pdf |
| 消息通知 | 消息模板/通知/用户记录管理 | micro-msg |
| 数据脱敏 | 响应自动脱敏，保护敏感数据 | micro-mask |
| 变更审计 | 数据变更日志记录与字段差异对比 | micro-audit |
| 删除合规 | 删除前依赖检查，防止误删被引用数据 | micro-rmcheck |
| 支付集成 | 微信支付/支付宝下单、回调、退款 | micro-pay |
| 微信小程序 | 小程序登录/用户管理/媒体上传 | micro-wxapp |
| 微信公众号 | 公众号消息处理/事件处理/用户管理 | micro-wxmp |
| 规则引擎 | LiteFlow 链/脚本管理，规则编排 | micro-liteflow |
| 函数管理 | 多语言脚本引擎动态执行 | micro-fun |
| K8s管理 | Kubernetes 集群配置与资源管理 | micro-k8s |
| 自动化测试 | REST 接口扫描/Mock/测试执行/报告 | micro-autotest |
| 报表管理 | SQL 报表定义/动态查询/Excel 导出 | micro-report |
| 数据视图 | 动态数据源/SQL查询/元数据管理 | micro-dbview |
