# 审计校验

| 序号 | 名称 | 优先级 | 状态 | 文件 |
|------|------|--------|------|------|
| 001 | 变更审计日志 | P0 | 已完成 | [001-变更审计日志.md](./001-变更审计日志.md) |
| 002 | 删除合规校验 | P0 | 已完成 | [002-删除合规校验.md](./002-删除合规校验.md) |

> 状态核对日期：2026-11-28。micro-audit 已实现 AuditApi/AuditImpl（create/modify/delete/batch）+ AuditCompareUtil 字段差异对比 + ChangeLogRest 分页查询；micro-rmcheck 已实现 RmCheckApi 删除合规校验 + 规则/规则项管理（RmCheckRuleRest、RmCheckRuleItemRest）。
