# micro-mask 模块开发指南

## 模块概述

micro-mask 是 sh-microapp 微应用集合中的数据脱敏模块，通过 `ResponseBodyAdvice` 在响应写出前按规则自动脱敏 JSON 数据，支持正则匹配脱敏、JS 脚本脱敏与兜底脱敏三种方式，规则按「请求方法 + 请求 URI + JSONPath」定位脱敏字段。

- **GroupId**: `com.wkclz.microapp`
- **ArtifactId**: `micro-mask`
- **API 前缀**: `/micro-mask`
- **外部依赖**: `spring-boot-starter-jackson`、Guava（本地缓存）、fastjson2（JSONPath 解析，传递依赖）

## 架构设计

```
宿主应用 Controller 响应
    ↓
MaskResponseAdvice（@ControllerAdvice + ResponseBodyAdvice）
  ├── URL 匹配：按 userCode + method + uri 匹配启用的脱敏规则（Guava 本地缓存）
  ├── JSONPath 定位：递归解析对象/数组，定位 maskJsonPath 字段
  └── 脱敏执行：正则优先 → JS 脚本次之 → 兜底规则
    ↓
MaskCache（规则缓存，Redis 时间戳 + 本地 Map 双级）
    ↓
mdm_mask_rule（脱敏规则表）
```

| 组件 | 职责 |
|------|------|
| `MaskResponseAdvice` | 响应拦截与脱敏执行核心 |
| `MaskCache` | 规则缓存：DB → 本地 Map，Redis key `sh:micro:mask:cache:time` 广播变更时间戳 |
| `MdmMaskRuleService` | 规则单表服务（extends BaseService） |

## 目录结构

```
micro-mask/src/main/java/com/wkclz/micro/mask/
├── MaskAutoConfig.java            # 自动配置（@ComponentScan + @MapperScan）
├── bean/
│   ├── entity/
│   │   └── MdmMaskRule.java       # 脱敏规则实体（表 mdm_mask_rule）
│   ├── dto/
│   │   └── MdmMaskRuleDto.java
│   ├── req/                       # Create/Update/Page/Info/Test Req
│   └── resp/                      # Page/Resp/Test Resp
├── cache/
│   └── MaskCache.java             # 规则缓存（Redis 时间戳 + 12s 定时同步）
├── config/
│   └── MaskResponseAdvice.java    # 响应自动脱敏 Advice
├── mapper/
│   └── MdmMaskRuleMapper.java     # 含 rules4Cache() 缓存加载
├── rest/
│   ├── Route.java                 # 路由常量（@Router）
│   ├── MaskRuleRest.java          # 规则 CRUD（增删改后清两级缓存）
│   └── MaskMockRest.java          # 规则测试/验证
└── service/
    └── MdmMaskRuleService.java
```

## API 端点清单

前缀 `/micro-mask`，共 7 个端点：

| 分组 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 规则 | GET | `/rule/page` | 规则分页 |
| 规则 | GET | `/rule/info` | 规则详情 |
| 规则 | POST | `/rule/create` | 创建规则（enableFlag 为空时默认 1），成功后清缓存 |
| 规则 | POST | `/rule/update` | 修改规则，成功后清缓存 |
| 规则 | POST | `/rule/remove` | 删除规则（逻辑删除），成功后清缓存 |
| 测试 | POST | `/rule/test` | 按规则对 mockValue 试算脱敏结果，并标注命中的脱敏方式 |
| 测试 | GET | `/rule/verify` | 返回固定 mock JSON，配合 Advice 实测脱敏效果 |

## 核心机制

### 响应自动脱敏（MaskResponseAdvice）

1. **跳过条件**：body 为基础类型/包装类型（`IGNORE_TYPES`）直接放行
2. **规则匹配缓存**：key 为 `userCode:method:uri`（匿名用户为 `anonymous`），Guava Cache `expireAfterAccess(12h)`、maximumSize 1024；命中规则按 `requestMethod` 相等 + `requestUri` AntPathMatcher 匹配筛选
3. **脱敏执行**：响应体经 ObjectMapper 序列化后按 `{` / `[` 分流为 JSONObject / JSONArray，对每条规则的 `maskJsonPath` 递归定位并回写脱敏结果；数组响应仅处理以 `$[*]` 开头的 JSONPath
4. **脱敏优先级**（`maskByString`）：`maskRuleRegular` 正则（命中部分替换为等长 `*`）→ `maskRuleScript` JS 脚本（`JsUtil.exec(script, str)`）→ 兜底（<4 位 `***`；<8 位 首字符+`****`；否则前 3 位+`****`）
5. **容错**：脱敏处理异常时 log.error 并返回原始响应，不影响接口可用性

### 双级缓存与多实例同步

- `MaskCache`：DB `rules4Cache()` 全量加载 → 本地 Map（key = maskRuleCode）；`clearCache()` 写 Redis 时间戳（1 分钟过期）并立即重建本地缓存
- 其他实例由 `@Scheduled(fixedDelay=12s)` 轮询 Redis 时间戳感知变更（1 分钟内的变更才处理），重建后置 `CLEAR_FLAG`
- `MaskResponseAdvice` 同样 12s 定时检查 `CLEAR_FLAG`，失效自身 Guava 规则匹配缓存

### 数据库表 mdm_mask_rule

| 字段 | 说明 |
|------|------|
| `mask_rule_code` | 规则编码（缓存 key，唯一） |
| `mask_rule_name` | 规则名称 |
| `request_method` | 请求方法（GET/POST…，全匹配） |
| `request_uri` | 请求路径（AntPathMatcher 模式） |
| `mask_json_path` | 脱敏数据路径（JSONPath，支持 `$[*]` 数组通配） |
| `mask_rule_regular` | 脱敏正则（优先） |
| `mask_rule_script` | JS 脱敏函数（次之） |
| `enable_flag` | 可用状态（1=启用，缓存 SQL 只取启用规则） |
| `mock_value` | 示例值（供 `/rule/test` 试算） |

均继承 BaseEntity 基础字段（id/sort/create_*/update_*/remark/version/deleted）。

## 依赖关系

- **sh-redis**：StringRedisTemplate（缓存变更时间戳广播）
- **sh-core / sh-tool / sh-web**（传递）：BaseEntity、IdentityContext（userCode）、JsUtil、RegularTool、RequestHelper（AntPathMatcher 匹配）
- **被依赖**：工程内暂无其他 micro-* 模块硬依赖本模块（根文档提及的 micro-dict 脱敏字典关联为数据层面约定，无代码依赖）；宿主应用引入后全局响应自动脱敏生效

## 开发注意事项

1. `MaskResponseAdvice` 对所有 Controller 响应生效（`supports` 恒 true），需脱敏的接口按规则表配置，无需改代码
2. 规则增删改必须触发 `maskCache.clearCache()` + `MaskResponseAdvice.clearCache()`，本模块 REST 已内置；若他处直接改表，规则最长约 12s~1 分钟延迟生效
3. `identity` 场景下规则匹配缓存按用户隔离；无登录态时统一按 `anonymous` 匹配
4. JSONPath 解析结果若仍是对象/数组，当前不支持（log.warn 跳过）；数组字段必须以 `$[*]` 开头
5. 异常处理统一使用 `ValidationException.of(...)`，REST 返回 `R<T>`

---

**最后更新时间**: 2026-11-28
