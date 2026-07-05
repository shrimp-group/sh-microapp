# API 索引

> 最后更新：`2026-06-11`

## 数据管理类 API

| 方法 | 路径 | 说明 | 模块 |
|------|------|------|------|
| GET | /micro-dict/page | 字典类型分页查询 | micro-dict |
| POST | /micro-dict/insert | 创建字典类型 | micro-dict |
| PUT | /micro-dict/update | 更新字典类型 | micro-dict |
| DELETE | /micro-dict/delete | 删除字典类型 | micro-dict |
| GET | /micro-dict/item/page | 字典项分页查询 | micro-dict |
| POST | /micro-dict/item/batch-save | 字典项批量保存 | micro-dict |
| GET | /micro-seq/next | 获取下一个序列号 | micro-seq |
| GET | /micro-material/page | 物料分页查询 | micro-material |
| GET | /micro-form/page | 表单分页查询 | micro-form |

## 文件文档类 API

| 方法 | 路径 | 说明 | 模块 |
|------|------|------|------|
| POST | /micro-file/upload | 文件上传 | micro-fileos |
| GET | /micro-file/download | 文件下载 | micro-fileos |
| GET | /micro-file/sign | 签名链接 | micro-fileos |
| POST | /micro-pdf/render | PDF渲染 | micro-pdf |

## 消息通知类 API

| 方法 | 路径 | 说明 | 模块 |
|------|------|------|------|
| GET | /micro-msg/notification/page | 通知分页查询 | micro-msg |
| GET | /micro-mask/rule/page | 脱敏规则分页查询 | micro-mask |

## 审计校验类 API

| 方法 | 路径 | 说明 | 模块 |
|------|------|------|------|
| GET | /micro-audit/page | 变更日志分页查询 | micro-audit |
| POST | /micro-rmcheck/check | 合规检查 | micro-rmcheck |

## 支付类 API

| 方法 | 路径 | 说明 | 模块 |
|------|------|------|------|
| POST | /micro-pay/order/create | 创建支付订单 | micro-pay |
| POST | /micro-pay/callback | 支付回调 | micro-pay |

## 微信类 API

| 方法 | 路径 | 说明 | 模块 |
|------|------|------|------|
| POST | /micro-wxapp/login | 小程序登录 | micro-wxapp |
| GET | /micro-wxmp/message | 公众号消息处理 | micro-wxmp |

## 基础设施类 API

| 方法 | 路径 | 说明 | 模块 |
|------|------|------|------|
| GET | /micro-liteflow/chain/page | LiteFlow链分页查询 | micro-liteflow |
| POST | /micro-fun/execute | 函数执行 | micro-fun |
| GET | /micro-k8s/resource | K8s资源查询 | micro-k8s |
| POST | /micro-autotest/execute | 自动化测试执行 | micro-autotest |
| POST | /micro-report/exec | 报表查询执行 | micro-report |
