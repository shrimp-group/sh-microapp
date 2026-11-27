# micro-rmcheck 模块开发指南

## 模块概述

micro-rmcheck 是 sh-microapp 微应用集合中的删除校验模块（Remove Check），通过"规则 + 检查项"配置化的方式，在删除业务数据前校验该数据是否仍被其他表引用，被引用则阻断删除并返回提示信息。

- **GroupId**: `com.wkclz.microapp`
- **ArtifactId**: `micro-rmcheck`
- **API 前缀**: `/micro-rmcheck`
- **数据表**: `rm_check_rule`（删除检查规则）、`rm_check_rule_item`（删除检查规则-检查项）

## 架构设计

```
宿主应用（删除业务数据前）
    ↓ rmCheckApi.check(EntityClass.class, entity)
API             RmCheckApi（反射取字段值 + 规则/检查项匹配）
    ↓
Mapper          RmCheckRuleMapper（getRmCheckRules4Check，按 tableName）
                RmCheckRuleItemMapper（getRmCheckRuleItem4Check，按 ruleCode）
                CheckMapper.rmCheck（动态 SQL 探测目标表）
REST            RmCheckRuleRest / RmCheckRuleItemRest（规则与检查项管理）
```

| 组件 | 职责 |
|------|------|
| `RmCheckRule` | 定义"哪张表、哪个字段"参与删除校验（ruleCode / tableName / columnName / enableFlag） |
| `RmCheckRuleItem` | 定义"去哪些表检查哪个字段"（ruleCode / checkTableName / checkColumnName / noticeMessage / enableFlag） |
| `CheckMapper.rmCheck` | `SELECT id FROM ${checkTableName} WHERE deleted = 0 AND ${checkColumnName} = #{checkValue} LIMIT 1` |
| `RmCheckApi.check` | 命中任一检查项即抛 `ValidationException`（优先用 `noticeMessage`，否则默认"您存在删除的内容仍然被依赖，请处理完成后再删除!"） |

## 目录结构

```
micro-rmcheck/src/main/java/com/wkclz/micro/rmcheck/
├── RmcheckAutoConfig.java             # 自动配置（@ComponentScan + @MapperScan）
├── api/
│   └── RmCheckApi.java                # 对外校验 API（@Component）：check(clazz, data)
├── bean/
│   ├── dto/                           # RmCheckRuleDto / RmCheckRuleItemDto（检查参数载体）
│   ├── entity/
│   │   ├── RmCheckRule.java           # 规则实体（含手写 copy/copyIfNotNull）
│   │   └── RmCheckRuleItem.java       # 检查项实体（含手写 copy/copyIfNotNull）
│   ├── req/                           # 规则与检查项的 Page/Info/Create/Update/List Req
│   └── resp/                          # RmCheckRulePageResp / RmCheckRuleResp / RmCheckRuleItemResp
├── mapper/
│   ├── CheckMapper.java               # 动态表探测 SQL
│   ├── RmCheckRuleMapper.java         # getRmCheckRuleList / getRmCheckRules4Check
│   └── RmCheckRuleItemMapper.java     # getRmCheckRuleItemList / getRmCheckRuleItem4Check
├── rest/
│   ├── Route.java                     # 路由常量（@Router）
│   ├── RmCheckRuleRest.java           # 规则管理 REST
│   └── RmCheckRuleItemRest.java       # 检查项管理 REST
└── service/
    ├── RmCheckRuleService.java        # 规则服务（ruleCode 生成 / 表存在性校验 / 删除自检）
    └── RmCheckRuleItemService.java    # 检查项服务
```

resources：`META-INF/spring/...AutoConfiguration.imports`、`mapper/CheckMapper.xml`、`mapper/RmCheckRuleMapper.xml`、`mapper/RmCheckRuleItemMapper.xml`

## API 端点清单

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/micro-rmcheck/rule/page` | 规则分页（带 tableSchema 条件） |
| GET | `/micro-rmcheck/rule/info` | 规则详情 |
| POST | `/micro-rmcheck/rule/create` | 新增规则（ruleCode 由 RedisIdGenerator 生成 `rm_rule_` 前缀） |
| POST | `/micro-rmcheck/rule/update` | 修改规则（ruleCode 不可改） |
| POST | `/micro-rmcheck/rule/remove` | 删除规则（删除前对规则记录自身执行一次 check 自检） |
| GET | `/micro-rmcheck/rule/item/list` | 按 ruleCode 查询检查项列表 |
| GET | `/micro-rmcheck/rule/item/info` | 检查项详情 |
| POST | `/micro-rmcheck/rule/item/create` | 新增检查项 |
| POST | `/micro-rmcheck/rule/item/update` | 修改检查项 |
| POST | `/micro-rmcheck/rule/item/remove` | 删除检查项 |

## 核心机制：删除校验流程

`RmCheckApi.check(Class<P extends BaseEntity> clazz, P data)`：

1. 实体类名驼峰转蛇形得到 `tableName`
2. 按 `tableName` 查 `rm_check_rule`（`enableFlag` 生效的规则），无规则直接放行
3. 对每条规则：按 `columnName` 反射调用实体的 getter（`get` + 下划线转驼峰首字母大写）取得待检值；配置异常（无对应 getter）抛 `ValidationException`
4. 按 `ruleCode` 查 `rm_check_rule_item`（`enableFlag` 生效的检查项）
5. 对每个检查项：跳过空表名/空列名/自表（`tableName == checkTableName`），执行 `CheckMapper.rmCheck` 探测目标表
6. 探测命中（返回 id 非空）→ 抛 `ValidationException`，消息取检查项 `noticeMessage`

### 宿主应用使用方式

```java
@Autowired RmCheckApi rmCheckApi;

// 删除业务数据前调用，实体类名需与表名对应（CamelCase → snake_case）
rmCheckApi.check(MdmDict.class, dictEntity);
```

规则配置示例：删除 `mdm_dict`（规则）时，检查 `mdm_dict_item.dict_code`（检查项）中是否仍引用待删编码。

## 配置校验（Service 层）

- 规则唯一性：`tableName + columnName` 组合唯一（`duplicateCheck`）
- 表存在性：新增/修改规则时经 `TableInfoService.getTables` 校验 `tableName` 在当前数据源中真实存在
- 删除自检：`RmCheckRuleService.customRemove` 在删除规则前以规则记录自身执行 `rmCheckApi.check`，规则被引用时同样阻断

## 依赖

- `sh-redis`：RedisIdGenerator（ruleCode 生成）
- `sh-mybatis`：BaseService / BaseMapper / PageQuery / ShMyBatisConfig（tableSchema）/ TableInfoService
- `sh-web`：RemoveReq

## 注意事项

1. `CheckMapper.rmCheck` 的表名/列名使用 `${}` 拼接（动态 SQL），来源为管理端配置的规则数据而非用户实时输入——务必限定该配置入口权限，避免引入注入风险
2. 被检查表需具备 `deleted` 逻辑删除字段（SQL 固定带 `deleted = 0` 条件），否则探测 SQL 执行失败
3. 检查项跳过"自表"比对（`tableName` 与 `checkTableName` 忽略大小写相等时不检查），避免删除规则记录时自阻断
4. 实体 getter 反射失败会直接抛异常中断校验，新增实体字段后需同步核对规则 `columnName` 拼写
5. `enableFlag` 默认值由 REST 层填充（未传时置 1，即启用）
6. 模块内无缓存，每次 check 实时查库；配置量级小，无需担心性能

**最后更新时间**: 2026-11-28
