# 业务规则索引

> 最后更新：`2026-06-11`

| 规则名 | 规则描述 | 适用范围 |
|--------|----------|----------|
| 字典类型唯一性 | dictType 必须唯一 | micro-dict |
| 字典级联更新 | 修改 dictType 时级联更新子表 | micro-dict |
| 字典删除约束 | 字典类型下存在字典项时禁止删除 | micro-dict |
| 文件大小限制 | 单文件不超过 50MB | micro-fileos |
| 文件类型校验 | Magic Bytes 与扩展名必须匹配 | micro-fileos |
| Hash去重 | 相同内容文件复用存储路径 | micro-fileos |
| 脱敏规则匹配 | 按 JSONPath 匹配响应字段进行脱敏 | micro-mask |
| 删除依赖检查 | 删除前检查是否被其他表引用 | micro-rmcheck |
| 序列号唯一性 | 按 dictType + prefix 生成唯一序列号 | micro-seq |
| 支付超时取消 | 超时未支付的订单自动取消 | micro-pay |
| 乐观锁控制 | 更新时必须传 version 字段 | 全局 |
| 逻辑删除 | 删除时写入时间戳而非物理删除 | 全局 |
