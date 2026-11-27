# 技术债务索引

> 最后更新：2026-11-27
> 下一可用序号：002（序号 = 现有条目最大序号 + 1）
> 状态值映射：表格展示为 Open / In Progress / Resolved（与本地模板一致），工具化校验请使用小写 open / in-progress / resolved；文件路径格式为 `分类目录/序号-简述.md`。新增/解决流程见 [docs/dev-process.md](../dev-process.md) 技术债务管理小节，本地模板见 [templates/common/](templates/common/)。

## 统计概览
| 状态 | 数量 |
|------|------|
| Open | 1 |
| In Progress | 0 |
| Resolved | 0 |
| **合计** | 1 |

## 按严重程度分布
| 严重程度 | Open | In Progress | Resolved | 合计 |
|----------|------|-------------|----------|------|
| Critical | 0 | 0 | 0 | 0 |
| High | 1 | 0 | 0 | 1 |
| Medium | 0 | 0 | 0 | 0 |
| Low | 0 | 0 | 0 | 0 |

## Open 债务
| ID | 分类 | 严重程度 | 简述 | 文件 |
|----|------|----------|------|------|
| TD-2026-0912-01 | 其他潜在风险 | High | DDL 表达式默认值 round-trip 语义静默损坏 | 其他潜在风险/001-表达式默认值.md |

## In Progress 债务
| ID | 分类 | 严重程度 | 简述 | 文件 |
|----|------|----------|------|------|

## Resolved 债务
| ID | 分类 | 严重程度 | 简述 | 解决日期 | 文件 |
|----|------|----------|------|----------|------|

## Harness 对齐专项清单（align-harness-standards）

> 2026-11-26 参照 sh-harness 工程扫描产出，按方向独立优化，一次只执行一个方向。状态流转：Open（待优化）→ Done（已优化）。详见 `.trae/specs/align-harness-standards/`。

| 方向 | 清单文件 | 条目数 | 优先级分布 | 状态 |
|------|----------|--------|-----------|------|
| 1. 质量门禁与静态检查落地 | harness-alignment/01-quality-gates.md | 6 | P0×3 P1×2 P2×1 | Done（条目 5/6 用户确认不执行） |
| 3. 代码规范符合度（P3C/java.md） | harness-alignment/03-code-standards.md | 20 | P1×12 P2×8 | Done（2026-11-27） |
| 4. 冗余代码精减 | harness-alignment/04-redundant-code.md | 15 | P0×1 P1×4 P2×10 | Done（2026-11-27；实体/Dto copy 按用户裁定全部保留，净精减 7 个零引用文件约 130 行；条目 7/8 转设计评审/框架对齐，13 待业务确认） |
| 5. 技术债务管理机制对齐 | harness-alignment/05-tech-debt-process.md | 7 | P1×3 P2×4 | Done（2026-11-27，含附带发现补录 micro-dbview） |
| 6. 文档体系对齐 | harness-alignment/06-docs-alignment.md | 9 | P0×2 P1×1 P2×2（另有 4 条归类项） | Open |
| 7. 构建与依赖规范 | harness-alignment/07-build-deps.md | 14 | P1×2 P2×12 | Open |
