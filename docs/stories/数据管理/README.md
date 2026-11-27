# 数据管理

| 序号 | 名称 | 优先级 | 状态 | 文件 |
|------|------|--------|------|------|
| 001 | 字典类型增删改查 | P0 | 已完成 | [001-字典类型增删改查.md](./001-字典类型增删改查.md) |
| 002 | 字典通用查询 | P1 | 已完成 | [002-字典通用查询.md](./002-字典通用查询.md) |
| 003 | 字典跨环境复制 | P1 | 已完成 | [003-字典跨环境复制.md](./003-字典跨环境复制.md) |
| 004 | 字典项批量保存 | P0 | 已完成 | [004-字典项批量保存.md](./004-字典项批量保存.md) |
| 005 | 序列号生成 | P0 | 已完成 | [005-序列号生成.md](./005-序列号生成.md) |
| 006 | 物料增删改查 | P0 | 已完成 | [006-物料增删改查.md](./006-物料增删改查.md) |
| 007 | 物料分组树 | P1 | 已完成 | [007-物料分组树.md](./007-物料分组树.md) |
| 008 | 物料引用与转移 | P1 | 已完成 | [008-物料引用与转移.md](./008-物料引用与转移.md) |
| 009 | 表单规则管理 | P0 | 已完成 | [009-表单规则管理.md](./009-表单规则管理.md) |
| 010 | 表单AOP校验 | P0 | 已完成 | [010-表单AOP校验.md](./010-表单AOP校验.md) |

> 状态核对日期：2026-11-28。micro-dict 字典类型 CRUD/Copy/Paste/批量保存/公共查询已实现（见 `micro-dict/AGENTS.md`，注意实际端点为 `/dict/create`、`/dict/item/save`、`/common/dict(s)/list`）；micro-seq 序列号生成已实现（`MdmSequenceService.genSequences`，Service 层 API，无 REST 端点）；micro-material 物料/分组/引用/转移/版本已实现（`MaterialRest`/`MaterialGroupRest`/`MaterialRefRest`/`MaterialTransferRest`/`MaterialVersionRest`）；micro-form 表单/规则/校验器模板已实现，AOP 拦截见 `FormRuleAop`。
