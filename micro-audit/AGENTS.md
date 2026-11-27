# micro-audit 模块开发指南

## 模块概述

micro-audit 是 sh-microapp 微应用集合中的数据变更审计模块，为任意 `BaseEntity` 子类实体提供变更日志记录（新增/修改/删除）与字段级差异对比查询能力。

- **GroupId**: `com.wkclz.microapp`
- **ArtifactId**: `micro-audit`
- **API 前缀**: `/micro-audit`
- **数据库表**: `mdm_change_log`（变更记录）

## 架构设计

```
业务代码（宿主应用）
    ↓ 注入
AuditApi（对外 API 接口） → AuditImpl（API 实现）
    ↓ 依赖
AuditCompareUtil   # 表名推导 + 实体快照 JSON 序列化（反射 getter）
RedisIdGenerator   # 批次号生成（audit_ 前缀）
MdmChangeLogService → MdmChangeLogMapper → mdm_change_log
    ↑ 查询
ChangeLogRest（变更记录分页/详情）
```

| 模式 | 应用 | 说明 |
|------|------|------|
| 快照式审计 | `AuditImpl` | 变更前后完整对象序列化为 JSON（`dataFrom`/`dataTo`）落库 |
| 泛型 API | `AuditApi` | 所有方法基于 `BaseEntity` 泛型，业务方无侵入调用 |
| 反射对比 | `getLogPage` | 结合表列元数据（columnComment）逐字段对比新旧值 |

## 目录结构

```
src/main/java/com/wkclz/micro/audit/
├── AuditAutoConfig.java           # 自动配置（@ComponentScan + @MapperScan）
├── api/
│   ├── AuditApi.java              # 对外审计接口（create/modify/delete/getLogPage）
│   └── impl/
│       └── AuditImpl.java         # 接口实现（批次号/快照落库/字段对比）
├── bean/
│   ├── dto/
│   │   ├── ChangeLog.java         # 变更日志 DTO（泛型 T，含 diff items）
│   │   └── ChangeLogItem.java     # 单字段差异项（columnName/columnDesc/oldValue/newValue）
│   ├── entity/
│   │   └── MdmChangeLog.java      # 变更记录实体（表 mdm_change_log）
│   ├── req/
│   │   ├── ChangeLogPageReq.java  # 分页查询请求
│   │   └── ChangeLogInfoReq.java  # 详情查询请求
│   └── resp/
│       ├── ChangeLogPageResp.java # 分页响应（含 dataFromEntity/dataToEntity）
│       └── ChangeLogInfoResp.java # 详情响应
├── mapper/
│   └── MdmChangeLogMapper.java
├── rest/
│   ├── Route.java                 # 路由常量（@Router 注解）
│   └── ChangeLogRest.java         # 变更记录查询 REST
├── service/
│   └── MdmChangeLogService.java   # extends BaseService
└── utils/
    └── AuditCompareUtil.java      # 表名推导 + 实体 JSON 快照
```

resources：`mapper/MdmChangeLogMapper.xml`、`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

## API 端点清单

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/micro-audit/change/log/page` | 变更记录分页查询（dataFrom/dataTo 解析为 JSONObject 返回） |
| GET | `/micro-audit/change/log/info` | 变更记录详情（按 id） |

## 核心机制

### AuditApi（业务方使用）

```java
@Autowired AuditApi auditApi;

String batchNo = auditApi.getBatchNo();          // audit_ 前缀批次号（RedisIdGenerator）

auditApi.create(entity);                          // INSERT 日志（dataTo 快照，dataVersion=0）
auditApi.create(entity, batchNo);                 // 指定批次号；支持 List 批量重载

auditApi.modify(from, to);                        // UPDATE 日志（from/to 双快照，dataVersion=from.version+1）
auditApi.modify(froms, tos, batchNo);             // 批量：from/to 数量必须一致且 id 对应

auditApi.delete(from);                            // DELETE 日志（dataFrom 快照）
auditApi.delete(froms, batchNo);                  // 批量重载

PageData<ChangeLog<Entity>> page = auditApi.getLogPage(dto);  // 按 clazz 反射对比，输出字段级 diff
```

### 快照与表名规则

| 项 | 规则 | 实现 |
|------|------|------|
| 表名 | 实体简单类名驼峰转下划线（`MdmDictItem` → `mdm_dict_item`） | `AuditCompareUtil.getTableName` |
| 快照 | 反射全部 getter 取值 → fastjson2 JSON 字符串 | `AuditCompareUtil.getDataValueJson` |
| 版本号 | 落库前 `version` 递增 +1（null 视为 0） | `AuditImpl` create/modify/delete |
| 操作类型 | `INSERT` / `UPDATE` / `DELETE` | `operateType` 字段 |

### getLogPage 字段级对比

1. `TableInfoService.getColumnInfos4Options` 获取物理表列元数据（列名驼峰化 + 列注释），`ChangeLogItem.columnDesc` 即来源于此
2. `BeanUtil.getJavaField(clazz)` 取 getter 映射，逐列反射调用新旧实体
3. 旧值为 null 或旧值 ≠ 新值时生成差异项；两者均为 null 则跳过
4. 对比完成后 `dataFrom`/`dataTo` 置 null（已解析为 items）

## 数据库表

### mdm_change_log（变更记录）

| 字段 | Java 字段 | 说明 |
|------|----------|------|
| `id` | `id` | 主键 |
| `batch_no` | `batchNo` | 批次号（一次业务操作的多条日志共用） |
| `table_name` | `tableName` | 变更表名 |
| `data_id` | `dataId` | 变更数据主键 |
| `data_version` | `dataVersion` | 变更后数据版本 |
| `operate_type` | `operateType` | 操作类型（INSERT/UPDATE/DELETE） |
| `data_from` | `dataFrom` | 原数据 JSON 快照（UPDATE/DELETE） |
| `data_to` | `dataTo` | 目标数据 JSON 快照（INSERT/UPDATE） |
| 其余 | — | BaseEntity 标准字段（tenant_code/sort/审计字段/version） |

查询扩展条件（`MdmChangeLog` 继承 `BaseEntity` 的时间范围/关键字）：`timeFrom`/`timeTo`/`keyword`，XML 中实现 `getChangeLogList` 分页查询。

## 依赖关系

- **sh-core**: BaseEntity、R、ValidationException、PageData
- **sh-mybatis**: BaseService、BaseMapper、PageQuery、TableInfoService（列元数据）
- **sh-redis**: RedisIdGenerator（批次号生成）
- **sh-tool**: BeanUtil（反射字段）、StringUtil（驼峰/下划线转换）
- **fastjson2**: 快照序列化与反序列化

模块间依赖：当前 sh-microapp 内无其他 micro-* 模块引用 `com.wkclz.micro.audit`，`AuditApi` 由宿主主应用注入使用。

## 开发注意事项

1. **调用时机**：`create/modify/delete` 应在业务数据写库的同事务内调用，异常快照随事务回滚
2. **modify 前置条件**：`from`/`to` 均必须携带 `id` 且一致，否则抛 `ValidationException`
3. **batchNo**：不传时自动生成；一次业务操作（如批量导入）建议复用同一 batchNo 便于关联追溯
4. **快照为全量 JSON**：`dataFrom`/`dataTo` 存实体全字段，敏感字段（密钥等）落库前需自行脱敏
5. **对比依赖反射 getter**：实体若有无副作用异常的 getter 会跳过该字段并记录 error 日志
6. **异常处理**：统一 `ValidationException.of("消息")`，REST 返回 `R<T>`

---

**最后更新时间**: 2026-11-28
