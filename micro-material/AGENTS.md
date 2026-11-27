# micro-material 模块开发指南

## 模块概述

micro-material 是 sh-microapp 微应用集合中的素材（物料）管理模块，提供素材与素材组的树形组织、上传/外链两类来源、文件替换与版本快照、业务引用绑定、所有权转移、热度统计与素材选择器能力，文件实体存储复用 micro-fileos。

- **GroupId**: `com.wkclz.microapp`
- **ArtifactId**: `micro-material`
- **API 前缀**: `/micro-material`
- **模块依赖**: `micro-fileos`（删除/替换素材时同步删除 OSS 文件，`FileosDeleteApi`）、`sh-redis`（IdGenerator / Pub-Sub 分组缓存）

## 架构设计

```
REST（manage 管理端 / customer 客户端）
    ↓
Service   MdmMaterialService（素材）/ MdmMaterialGroupService（分组）
          MdmMaterialRefService（引用）/ MdmMaterialVersionService（版本）
          MdmMaterialTransferLogService（转移日志）
    ↓
api       MaterialApi / MaterialApiImpl（bind / unbind / check，供宿主业务绑定素材引用）
    ↓
Mapper    5 张表 Mapper + XML
Cache     MaterialGroupCache（Redis Pub/Sub 频道 sh:micro:material:group:cache:refresh）
    ↓
micro-fileos（文件删除 FileosDeleteApi）/ RedisIdGenerator（materialCode = m_ 前缀自增）
```

## 目录结构

```
micro-material/src/main/java/com/wkclz/micro/material/
├── MaterialAutoConfig.java        # 自动配置（@ComponentScan + @MapperScan）
├── api/
│   ├── MaterialApi.java           # 对外接口：bind / unbind / check
│   └── MaterialApiImpl.java
├── bean/
│   ├── entity/
│   │   ├── MdmMaterial.java           # 素材（表 mdm_material）
│   │   ├── MdmMaterialGroup.java      # 素材分组（表 mdm_material_group）
│   │   ├── MdmMaterialRef.java        # 素材引用（表 mdm_material_ref）
│   │   ├── MdmMaterialVersion.java    # 素材版本（表 mdm_material_version）
│   │   └── MdmMaterialTransferLog.java# 转移日志（表 mdm_material_transfer_log）
│   ├── dto/                           # 5 个实体对应 Dto
│   ├── req/                           # 24 个 Req（含嵌套 item 的 BatchCreateReq）
│   └── resp/                          # 9 个 Resp（含 GroupTree / Distribution）
├── cache/
│   └── MaterialGroupCache.java        # 分组树缓存（Redis Pub/Sub + 本地 Map）
├── mapper/                            # 5 个 Mapper 接口
├── rest/
│   ├── Route.java                     # 路由常量（@Router，manage/customer 分组）
│   ├── MaterialRest.java              # 素材 CRUD / 移动 / 替换文件 / 可见性
│   ├── MaterialGroupRest.java         # 分组树 CRUD / 拖拽移动 / 排序
│   ├── MaterialRefRest.java           # 引用绑定/解绑/清单/占用检查
│   ├── MaterialVersionRest.java       # 版本列表 / 回滚
│   ├── MaterialTransferRest.java      # 所有权转移 / 日志
│   ├── MaterialStatsRest.java         # 热度排行 / 分布统计
│   └── MaterialPickerRest.java        # 素材选择器（客户端）
└── service/                           # 5 个 Service（extends BaseService）
```

## API 端点清单

前缀 `/micro-material`，共 31 个端点（manage 29 + customer 2）：

| 分组 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 素材 | GET | `/manage/material/page` | 分页（自动附加租户与用户过滤） |
| 素材 | GET | `/manage/material/info` | 详情（PRIVATE 可见性校验查看权限） |
| 素材 | POST | `/manage/material/create` | 上传来源创建（sourceType=UPLOAD） |
| 素材 | POST | `/manage/material/batch-create` | 批量创建（单项失败不中断，失败项仅回填 fileName） |
| 素材 | POST | `/manage/material/link-create` | 外链来源创建（sourceType=LINK，附链接状态检测） |
| 素材 | POST | `/manage/material/update` | 修改（编辑权限校验） |
| 素材 | POST | `/manage/material/remove` | 删除（UPLOAD 来源同步删 OSS 文件，删失败仅告警） |
| 素材 | POST | `/manage/material/restore` | 恢复（按 version 乐观锁回写） |
| 素材 | POST | `/manage/material/move` | 批量移动分组 |
| 素材 | POST | `/manage/material/replace-file` | 替换文件：旧文件快照入版本表 → 删旧文件 → 更新 → 清理历史版本 |
| 素材 | POST | `/manage/material/visibility` | 批量切换可见性（PRIVATE/PUBLIC） |
| 素材 | POST | `/manage/material/link-check` | 外链状态检测（VALID/INVALID/UNKNOWN） |
| 分组 | GET | `/manage/group/tree` | 分组树（SYSTEM + 个人分组合并） |
| 分组 | GET | `/manage/group/info` | 分组详情 |
| 分组 | POST | `/manage/group/create` | 创建分组 |
| 分组 | POST | `/manage/group/update` | 修改分组 |
| 分组 | POST | `/manage/group/remove` | 删除分组（含子级/占用校验） |
| 分组 | POST | `/manage/group/move` | 分组拖拽换父 |
| 分组 | POST | `/manage/group/sort` | 分组排序（按 id 顺序写 sort） |
| 引用 | POST | `/manage/ref/bind` | 绑定业务引用（materialCode + bizType + bizCode） |
| 引用 | POST | `/manage/ref/unbind` | 解绑引用 |
| 引用 | GET | `/manage/ref/list` | 素材的引用清单 |
| 引用 | GET | `/manage/ref/check` | 引用占用检查（删除前确认） |
| 版本 | GET | `/manage/version/list` | 版本快照列表 |
| 版本 | POST | `/manage/version/rollback` | 回滚到指定版本 |
| 转移 | POST | `/manage/transfer/create` | 所有权转移（写转移日志） |
| 转移 | GET | `/manage/transfer/log` | 转移日志查询 |
| 统计 | GET | `/manage/stats/hot` | 热度排行 |
| 统计 | GET | `/manage/stats/distribution` | 类型分布统计 |
| 选择器 | GET | `/customer/picker/list` | 素材选择器分页（仅可见素材） |
| 选择器 | GET | `/customer/picker/groups` | 选择器分组树 |

## 核心机制

### 数据模型

| 表 | 关键字段 | 说明 |
|----|----------|------|
| `mdm_material` | material_code（m_ 前缀 Redis 自增）、material_type（IMAGE/VIDEO/AUDIO/DOCUMENT/OTHER）、source_type（UPLOAD/LINK）、file_id（关联 micro-fileos）、link_url / link_status、visibility（PRIVATE/PUBLIC）、group_code、user_code / tenant_code | 素材主表 |
| `mdm_material_group` | group_code、parent_code（顶级为 0）、group_type（SYSTEM/PERSONAL）、user_code | 分组树，SYSTEM 组全租户共享 |
| `mdm_material_version` | material_code、version_no、file_id / file_name / file_size | 替换文件时的历史快照，超出保留数自动清理 |
| `mdm_material_ref` | material_code、biz_type、biz_code、ref_desc | 业务引用关系，check 用于删除前占用校验 |
| `mdm_material_transfer_log` | material_code、from_user_code、to_user_code、operator_code | 所有权转移审计 |

均继承 BaseEntity 基础字段（id/sort/create_*/update_*/remark/version/deleted）。

### 权限与可见性

- `checkViewPermission`：PRIVATE 素材仅所有者可查看，否则报"素材不存在或无权操作"
- `checkEditPermission`：编辑/删除/替换等操作校验所有者身份
- 分组树按 `tenantCode:userCode`（个人）+ `tenantCode:SYSTEM`（系统）双 key 缓存合并

### 分组缓存（MaterialGroupCache）

- Redis Pub/Sub 频道 `sh:micro:material:group:cache:refresh`，启动时 `@PostConstruct` 订阅并加载
- 分组变更后 `clearCache()` 广播刷新，所有实例 `onMessage()` 重载本地 Map

## 依赖关系

- **micro-fileos**：素材删除/替换文件时调用 `FileosDeleteApi` 清理 OSS 文件（失败降级为 warn，不阻断事务）
- **sh-redis**：RedisIdGenerator（编码生成）、RedisMessageListenerContainer（分组缓存广播）
- **sh-core / sh-mybatis**（传递）：IdentityContext（租户/用户上下文）、BaseService/PageQuery
- **被依赖**：工程内暂无其他 micro-* 模块依赖本模块；`MaterialApi`（bind/unbind/check）供宿主业务系统绑定素材引用

## 开发注意事项

1. 素材编码由 `RedisIdGenerator.generateIdWithPrefix("m_")` 生成，非数据库自增
2. 上传类接口只登记文件元数据，文件本体先经 micro-fileos 上传后携 fileId 建档
3. 删除素材对 UPLOAD 来源会同步删 OSS 文件，但文件删除失败仅 log.warn 不回滚
4. `replaceFile` 先写版本快照再更新主表，`cleanupOldVersions` 自动清理超出保留数的历史版本
5. 批量创建为逐项 try-catch：失败项不抛异常，返回仅含 fileName 的占位对象
6. 分组删除前需校验子分组与素材占用（`MdmMaterialGroupService.remove`）
7. 异常处理统一使用 `ValidationException.of(...)`，REST 返回 `R<T>`；事务注解在 Service 层

---

**最后更新时间**: 2026-11-28
