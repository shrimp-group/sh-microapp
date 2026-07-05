# 数据模型索引

> 最后更新：`2026-06-11`

## 数据管理类

| 实体名 | 说明 | 关联模块 |
|--------|------|----------|
| MdmDict | 字典类型 | micro-dict |
| MdmDictItem | 字典项 | micro-dict |
| MdmSequence | 序列号 | micro-seq |
| MdmMaterial | 物料 | micro-material |
| MdmMaterialGroup | 物料组 | micro-material |
| MdmMaterialVersion | 物料版本 | micro-material |
| MdmMaterialRef | 物料关联 | micro-material |
| MdmForm | 表单定义 | micro-form |
| MdmFormRule | 表单规则 | micro-form |

## 文件文档类

| 实体名 | 说明 | 关联模块 |
|--------|------|----------|
| MdmFileosBucket | 文件桶 | micro-fileos |
| MdmFileosDirectory | 文件目录 | micro-fileos |
| MdmFileosRecord | 文件记录 | micro-fileos |
| MdmPdfTemplate | PDF模板 | micro-pdf |

## 消息通知类

| 实体名 | 说明 | 关联模块 |
|--------|------|----------|
| MsgTemplate | 消息模板 | micro-msg |
| MsgNotification | 消息通知 | micro-msg |
| MsgUserRecord | 用户消息记录 | micro-msg |
| MdmMaskRule | 脱敏规则 | micro-mask |

## 审计校验类

| 实体名 | 说明 | 关联模块 |
|--------|------|----------|
| MdmChangeLog | 变更日志 | micro-audit |
| RmCheckRule | 校验规则 | micro-rmcheck |

## 支付类

| 实体名 | 说明 | 关联模块 |
|--------|------|----------|
| PayOrder | 支付订单 | micro-pay |
| PayWxpayConfig | 微信支付配置 | micro-pay |
| PayAlipayConfig | 支付宝配置 | micro-pay |

## 微信类

| 实体名 | 说明 | 关联模块 |
|--------|------|----------|
| WxappUser | 小程序用户 | micro-wxapp |
| WxmpUser | 公众号用户 | micro-wxmp |

## 基础设施类

| 实体名 | 说明 | 关联模块 |
|--------|------|----------|
| LiteflowChain | LiteFlow链 | micro-liteflow |
| LiteflowScript | LiteFlow脚本 | micro-liteflow |
| FunCategory | 函数分类 | micro-fun |
| FunFunction | 函数定义 | micro-fun |
| K8sConfig | K8s配置 | micro-k8s |
| ReportDefinition | 报表定义 | micro-report |
