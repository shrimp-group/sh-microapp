# micro-dict 模块开发指南

本文档帮助开发者快速理解 `micro-dict` 模块的架构设计、核心功能和开发规范。

## 📦 模块概述

`micro-dict` 是数据字典管理服务模块，提供字典类型和字典项的 CRUD 管理，支持跨环境 Copy/Paste 迁移，通过 Redis Pub/Sub 实现多实例缓存一致性。

### 核心特性

- **字典类型管理**: 字典分类（dictCtg）和字典类型（dictType）的增删改查
- **字典项批量保存**: 自动 diff 新旧数据，精确执行 insert/update/delete
- **跨环境迁移**: Copy/Paste 机制，支持字典结构整体导出导入
- **多字典查询**: 公共接口支持逗号分隔多 dictType 一次性查询（上限 50 个）
- **Redis Pub/Sub 缓存**: `DictCache` 基于频道广播实现多实例缓存同步，3 秒防抖
- **智能级联更新**: 修改 dictType 时自动级联更新子表 `mdm_dict_item`

---

## 🏗️ 架构设计

```
┌───────────────────────────────────────────────┐
│              REST Controller                   │
│  DictRest | DictItemRest | CommonDictRest     │
└───────────────────────────────────────────────┘
                      ↓
┌───────────────────────────────────────────────┐
│               Service Layer                    │
│  MdmDictService | MdmDictItemService          │
│  (extends BaseService，继承通用 CRUD)           │
└───────────────────────────────────────────────┘
                      ↓
┌───────────────────────────────────────────────┐
│             Mapper Layer (MyBatis)             │
│  MdmDictMapper | MdmDictItemMapper            │
└───────────────────────────────────────────────┘
                      ↓
┌───────────────────────────────────────────────┐
│               Cache Layer                      │
│  DictCache (Redis Pub/Sub + 内存 Map)          │
└───────────────────────────────────────────────┘
```

---

## 📁 目录结构

```
micro-dict/src/main/java/com/wkclz/micro/dict/
├── DictAutoConfig.java           # 自动配置（@ComponentScan + @MapperScan）
├── bean/
│   ├── entity/
│   │   ├── MdmDict.java          # 字典类型实体（dictCtg, dictType, description）
│   │   └── MdmDictItem.java      # 字典项实体（dictType, dictValue, dictLabel, elType, enableFlag）
│   └── dto/
│       ├── MdmDictDto.java       # 字典类型 DTO（扩展 items、dictTypes）
│       └── MdmDictItemDto.java   # 字典项 DTO
├── cache/
│   └── DictCache.java            # 字典缓存，Redis Pub/Sub 监听
├── mapper/
│   ├── MdmDictMapper.java        # 字典 Mapper（分页/缓存/Copy/Paste 查询）
│   └── MdmDictItemMapper.java    # 字典项 Mapper（含 updateDictTypeBatch）
├── rest/
│   ├── DictRest.java             # 字典类型 CRUD + Copy/Paste
│   ├── DictItemRest.java         # 字典项批量保存
│   ├── CommonDictRest.java       # 公共字典查询（无需权限）
│   └── Route.java                # 路由常量定义（前缀 /micro-dict）
└── service/
    ├── MdmDictService.java       # 字典类型服务（含 parse 核心逻辑）
    └── MdmDictItemService.java   # 字典项服务（含 dictItemSave diff 逻辑）
```

---

## 🔑 核心组件说明

### 1. REST API 端点（前缀 `/micro-dict`）

**字典类型管理 (DictRest)**:

| 端点 | 方法 | 说明 |
|------|------|------|
| `/dict/page` | GET | 分页查询（支持 dictCtg/dictType/description 模糊查询）|
| `/dict/info` | GET | 详情 |
| `/dict/create` | POST | 新增（dictType 唯一校验）|
| `/dict/update` | POST | 修改（含 dictType 变更时级联更新子表）|
| `/dict/remove` | POST | 删除（存在字典项时拒绝删除）|
| `/dict/copy` | GET | 复制字典结构为 JSON |
| `/dict/paste` | POST | 粘贴导入（自动 diff 增量写入）|
| `/dict/options` | GET | 获取所有字典类型选项列表 |

**字典项管理 (DictItemRest)**:

| 端点 | 方法 | 说明 |
|------|------|------|
| `/dict/item/list` | GET | 按 dictType 查询字典项列表（不分页）|
| `/dict/item/save` | POST | 批量保存（自动计算新增/修改/删除）|

**公共查询 (CommonDictRest)**:

| 端点 | 方法 | 说明 |
|------|------|------|
| `/common/dict/list` | GET | 单字典查询 |
| `/common/dicts/list` | GET | 多字典查询（逗号分隔，上限 50）|

### 2. DictCache - Redis Pub/Sub 缓存

```java
// 缓存结构: Map<dictType, Map<dictValue, dictLabel>>
static volatile Map<String, Map<String, String>> CACHE_DICT;

// API
String value = dictCache.get("GENDER", "MALE");  // → "男"
Map<String, String> map = dictCache.get("GENDER"); // → {"MALE":"男", "FEMALE":"女"}
dictCache.clearCache();  // 发布 Redis 消息，所有实例刷新
```

- 实现 `MessageListener`，订阅 `shrimp:micro:dict:cache:refresh` 频道
- `@PostConstruct` 注册监听器并加载缓存
- `loadCache()` 使用 `synchronized` + 3 秒防抖，避免高频刷新
- 通过 Redis Pub/Sub 确保多实例部署时缓存一致

### 3. MdmDictService - Paste 核心逻辑

```
parse(dtos) 流程:
1. 将导入数据按 dictType 查出已有数据
2. Map<dictType, Dto> 做 O(1) 查找（性能优化）
3. 分别计算 dicts2Insert / dicts2Update / items2Insert / items2Update
4. 差异字段逐个对比，只更新真正变化的记录
5. 事务内批量执行
```

### 4. MdmDictItemService - dictItemSave Diff

```
dictItemSave(dto) 流程:
1. 获取历史所有 items → oldItems, oldValues Set
2. 新提交 items → newItems, newValues Set
3. inserts = newItems 中有而 oldValues 中没有的
4. deletes  = oldItems 中有而 newValues 中没有的
5. updates  = 两者都有但字段发生变化的
6. 事务内执行 insertBatch + updateByIdSelective + deleteByIds
```

---

## 🛠️ 开发指南

### 使用 DictCache 编程获取字典值

```java
@Autowired
private DictCache dictCache;

// 获取单个字典值
String label = dictCache.get("GENDER", "MALE");

// 获取整个字典 Map（用于前端下拉框等）
Map<String, String> genderMap = dictCache.get("GENDER");
```

### 调用公共字典查询 API

```java
// 单字典查询
GET /micro-dict/common/dict/list?dictType=GENDER

// 多字典查询
GET /micro-dict/common/dicts/list?dictType=GENDER,STATUS,ROLE_TYPE
```

### 跨环境迁移字典

```java
// 1. 在源环境复制
GET /micro-dict/dict/copy?dictType=GENDER
// 返回 JSON 数据

// 2. 在目标环境粘贴
POST /micro-dict/dict/paste
Body: [上一步返回的 JSON 数组]
```

### 添加新的 Mapper 方法

1. 在 `*Mapper.java` 接口中声明方法
2. 在 `*Mapper.xml` 中编写 SQL
3. 在对应 Service 中调用（注意事务注解）

---

## 📊 数据库表结构

### mdm_dict（字典类型）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| dict_ctg | String | 字典分类（分组）|
| dict_type | String | 字典类型（大写蛇形，如 `GENDER`）|
| description | String | 类型描述 |
| sort | Integer | 排序 |
| version | Integer | 乐观锁版本号 |

### mdm_dict_item（字典项）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| dict_type | String | 所属字典类型 |
| dict_value | String | 字典值（key）|
| dict_label | String | 字典标签（显示值）|
| el_type | String | 前端元素类型 |
| description | String | 描述 |
| enable_flag | Integer | 生效状态（1=启用）|
| sort | Integer | 排序 |

---

## ⚠️ 注意事项

1. **dictType 命名规范**: 存储为大写蛇形命名（如 `ORDER_STATUS`），接口支持驼峰输入自动转换
2. **删除字典前置条件**: 必须先删除所有字典项后才能删除字典类型
3. **dictItemSave 是全量覆盖**: 传入的 items 列表会完全替代原有数据，不在列表中的项会被删除
4. **缓存刷新时机**: `create`/`update`/`remove`/`dictItemSave`/`paste` 操作后都会调用 `dictCache.clearCache()`
5. **事务注解位置**: `@Transactional` 放在 Service 层方法上，Rest 层也加（如 create/update/remove）
6. **Copy/Paste 不处理 ID**: 复制时不导出 ID，粘贴时以 dictType+dictValue 为唯一键做匹配

---

## 📝 最佳实践

1. **dictType 统一大写**: 保证查询和使用的一致性
2. **优先使用 DictCache**: 高频读取场景用缓存，避免每次查 DB
3. **多字典查询用 common/dicts/list**: 一次性查出多个字典，减少请求次数
4. **Copy/Paste 做跨环境迁移**: 避免手动在不同环境重复录入字典数据
5. **dictItemSave 批量操作**: 前端一次性提交所有字典项，后端自动 diff

---

## 🔧 依赖关系

```xml
<!-- pom.xml -->
<dependency>com.wkclz.iam:iam-contract-api</dependency>       <!-- PrincipalContext -->
<dependency>com.wkclz.framework:sh-mybatis</dependency>  <!-- BaseService/BaseMapper -->
<dependency>com.wkclz.framework:sh-redis</dependency>    <!-- Redis Pub/Sub -->
```

- `sh-core`: BaseEntity、ValidationException、R 返回对象
- `sh-mybatis`: BaseService、BaseMapper、PageQuery
- `sh-redis`: StringRedisTemplate、RedisMessageListenerContainer
- `iam-contract-api`: PrincipalContext（paste 操作获取当前用户）

---

## 🆘 常见问题

### Q: 调用 common/dict/list 返回空
A: 检查 dictType 是否为大写，接口会对驼峰输入自动转大写蛇形

### Q: 多实例部署时缓存不一致
A: 确认 Redis Pub/Sub 频道 `shrimp:micro:dict:cache:refresh` 可正常通信

### Q: paste 操作未生效
A: paste 是增量更新，只更新有变化的字段；检查数据格式是否与 copy 返回一致

### Q: 修改 dictType 后子表数据丢失
A: `dictUpdate` 会自动调用 `updateDictTypeBatch` 级联更新子表 dictType

### Q: 删除字典类型报错"请先删除字典枚举"
A: `mdm_dict_item` 表中仍存在该 dictType 的记录，需先通过 dictItemSave 清空

---

**最后更新时间**: 2026-04-26
