# micro-form 模块开发指南

## 模块概述

micro-form 是 sh-microapp 微应用集合中的动态表单与接口校验规则模块，提供两部分能力：一是动态表单定义（表单 + 输入项，供前端渲染），二是基于 AOP 的接口参数校验规则（方法+URI 匹配、JSONPath 取值、多类型验证器拦截），所有配置数据驱动、缓存热更新。

- **GroupId**: `com.wkclz.microapp`
- **ArtifactId**: `micro-form`
- **API 前缀**: `/micro-form`
- **外部依赖**: `spring-boot-starter-aop`（FormRuleAop 参数拦截）

## 架构设计

```
FormRuleAop（@Aspect, Order(MIN_VALUE)，拦截宿主全部 Controller）
    ↓ method + uri
FormRuleCache（Redis 时间戳 + 本地 Map 热更新缓存）
    ↓ MdmFormRuleDto（规则 → 校验项 → 验证器 三级组装）
ValidatorFactory（ValidatorTypeEnum → IValidator 静态工厂）
    ↓ JSONPath.eval 取值后逐验证器校验
23 种内置验证器（Required/数值/长度/日期/格式/DIY/TEMPLATE）
    ↑ 配置管理
FormRest / FormRuleRest / FormRuleValidatorRest / FormRuleValidatorTemplateRest（管理端 CRUD）
CommonFormRest / CommonFormRuleRest（客户端接入查询）
```

## 目录结构

```
src/main/java/com/wkclz/micro/form/
├── FormAutoConfig.java                    # 自动配置（@ComponentScan + @MapperScan）
├── aop/
│   └── FormRuleAop.java                   # 环绕通知：接口参数校验拦截
├── bean/
│   ├── dto/                               # MdmFormDto / MdmFormItemDto / MdmFormRuleDto
│   │   │                                  # MdmFormRuleFieldDto / MdmFormRuleFieldValidatorDto
│   │   └── MdmFormRuleValidatorTemplateDto.java
│   ├── entity/                            # 6 张表实体（见数据库表）
│   ├── enums/
│   │   └── ValidatorTypeEnum.java         # 23 种验证器类型
│   ├── req/                               # 管理端 CRUD Req + 客户端 Common*Req
│   └── resp/                              # 管理端 CRUD Resp + 客户端 Common*Resp
├── cache/
│   ├── FormCache.java                     # 表单定义缓存（sh:micro:form:cache:time）
│   └── FormRuleCache.java                 # 校验规则缓存（sh:micro:formRule:cache:time）
├── mapper/                                # 6 个 Mapper
├── rest/
│   ├── Route.java                         # 路由常量（@Router 注解）
│   ├── FormRest.java                      # 表单管理 + DB 列查询
│   ├── FormRuleRest.java                  # 校验规则管理
│   ├── FormRuleValidatorRest.java         # 规则校验项+验证器配置
│   ├── FormRuleValidatorTemplateRest.java # 验证器模板管理
│   ├── CommonFormRest.java                # 客户端表单查询
│   └── CommonFormRuleRest.java            # 客户端规则清单查询
├── service/                               # 6 个单表 Service + FormTableInfoService
└── validator/
    ├── IValidator.java                    # 验证器接口（null=通过，否则返回错误信息）
    ├── ValidatorFactory.java              # 静态工厂 + registerValidator 扩展点
    └── impl/                              # 23 个验证器实现
```

resources：`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`、`mapper/*.xml`

## API 端点清单（前缀 `/micro-form`，共 21 个）

| 分组 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 表单管理 | GET | `/form/page` | 表单分页 |
| 表单管理 | GET | `/form/info` | 表单详情 |
| 表单管理 | POST | `/form/create` | 创建表单 |
| 表单管理 | POST | `/form/update` | 更新表单 |
| 表单管理 | POST | `/form/remove` | 删除表单 |
| 表单管理 | GET | `/form/db/columns` | 查询物理表列元数据（FormTableInfoService） |
| 规则管理 | GET | `/form/rule/page` | 校验规则分页 |
| 规则管理 | GET | `/form/rule/info` | 校验规则详情 |
| 规则管理 | POST | `/form/rule/create` | 创建校验规则 |
| 规则管理 | POST | `/form/rule/update` | 更新校验规则 |
| 规则管理 | POST | `/form/rule/remove` | 删除校验规则 |
| 规则配置 | GET | `/form/rule/field_and_validator` | 按 formRuleCode 查校验项与验证器 |
| 规则配置 | POST | `/form/rule/field_and_validator/save` | 保存校验项与验证器配置 |
| 验证器模板 | GET | `/form/rule/validator/template/page` | 模板分页 |
| 验证器模板 | GET | `/form/rule/validator/template/info` | 模板详情 |
| 验证器模板 | POST | `/form/rule/validator/template/create` | 创建模板 |
| 验证器模板 | POST | `/form/rule/validator/template/update` | 更新模板 |
| 验证器模板 | POST | `/form/rule/validator/template/remove` | 删除模板 |
| 客户端 | GET | `/common/form/list` | 表单+输入项列表 |
| 客户端 | GET | `/common/form/detail` | 表单+输入项详情 |
| 客户端 | GET | `/common/form/rule` | 按 apiMethod+apiUri 查验证器清单 |

## 核心机制

### AOP 参数校验（FormRuleAop）

1. 切点：`@within(@Controller || @RestController)`，`@Order(Integer.MIN_VALUE)` 最先执行，作用于宿主应用全部接口
2. 以 `HTTP method + requestURI` 查 `FormRuleCache`，无规则或无校验项直接放行
3. 过滤 `HttpServletRequest/HttpServletResponse/MultipartFile` 后取**第一个业务参数**序列化为 JSON
4. 每个校验项 `fieldCode`（自动补 `$.` 前缀）经 `JSONPath.eval` 从参数取值
5. 逐验证器校验（`ValidatorTypeEnum.valueOf(validatorType)` → `ValidatorFactory.getValidator`），任一失败立即返回 `R.error(错误信息)`，全部通过才放行

### 验证器体系（23 种 ValidatorTypeEnum）

| 分类 | 类型 |
|------|------|
| 必填 | REQUIRED |
| 数值 | INTEGER_GT/GE/LT/LE、FLOAT_GT/GE/LT/LE |
| 长度 | STRING_GT/GE/LT/LE（字符串长度） |
| 日期 | DATE(yyyy-MM-dd)、DATETIME、TIME(HH:mm:ss) |
| 格式 | EMAIL、MOBILE、URL、DOMAIN、IP、ID_CARD、JSON |
| 扩展 | DIY（validatorPattern 正则 + validatorFunction JS 函数，经 `JsUtil.exec` 执行）、TEMPLATE（validatorPattern 正则模板） |

错误信息取验证器配置的 `msgTemplate`，未配置时回退默认文案。自定义验证器通过 `ValidatorFactory.registerValidator(type, validator)` 覆盖注册。

### 缓存热更新（FormCache / FormRuleCache）

- 双缓存结构一致：Redis key 存刷新时间戳（`sh:micro:form:cache:time` / `sh:micro:formRule:cache:time`），本地静态 Map 存数据
- `@Scheduled(fixedDelay = 12s)` 轮询 Redis 时间戳：本地更新或超过 1 分钟不处理；`init()` 内置 5 秒防抖
- `clearCache()`：写 Redis 时间戳（1 分钟过期）→ 本实例立即 `init()`，其他实例在 12 秒内轮询生效
- 规则缓存按 `apiMethod:apiUri` 组装三级结构（规则 → fields → validators）

## 数据库表

| 表名 | 实体 | 关键字段 | 说明 |
|------|------|----------|------|
| `mdm_form` | MdmForm | formCode、formName | 表单定义 |
| `mdm_form_item` | MdmFormItem | formCode、itemGroup、itemCode、itemName、inputType、fieldType、dictType、label、min/max、minLength/maxLength、placeholder、required、defaultValue、rules、clearable | 表单输入项（渲染属性，dictType 关联数据字典） |
| `mdm_form_rule` | MdmFormRule | formRuleCode、formRuleName、apiMethod、apiUri | 接口校验规则（method+uri 唯一定位） |
| `mdm_form_rule_field` | MdmFormRuleField | formRuleCode、fieldCode、fieldName | 校验项（fieldCode 为 JSONPath） |
| `mdm_form_rule_field_validator` | MdmFormRuleFieldValidator | formRuleCode、fieldCode、validatorType、validatorPattern、validatorFunction、templateCode、msgTemplate | 校验项验证器配置 |
| `mdm_form_rule_validator_template` | MdmFormRuleValidatorTemplate | templateCode、templateName、validatorPattern、validatorFunction | 验证器模板库 |

均含 BaseEntity 标准字段（tenant_code/sort/审计字段/version）。

## 依赖关系

- **sh-core / sh-mybatis / sh-tool**：BaseEntity、BaseService、BeanUtil、JsUtil（JS 执行）、JSONPath 依赖 fastjson2
- **sh-redis**: StringRedisTemplate（缓存刷新时间戳）
- **spring-boot-starter-aop**: FormRuleAop 切面

模块间依赖：sh-microapp 内无其他 micro-* 模块引用 `com.wkclz.micro.form`；`mdm_form_item.dictType` 业务上关联 micro-dict 字典类型，为数据级引用非代码依赖。

## 开发注意事项

1. **宿主接入**：引入依赖即对全部 Controller 生效，无规则的接口零开销（缓存 Map 一次 get 后直接放行）
2. **仅校验第一个参数**：`FormRuleAop` 暂只对第一个业务参数做 JSONPath 校验，多参数接口需把待校验字段放在首个参数
3. **fieldCode**：JSONPath 表达式，不以 `$` 开头时自动补 `$.`，支持嵌套取值（如 `$.user.name`）
4. **DIY 验证器 JS**：`validatorFunction` 为 JS 函数体，入参为字段值解析出的 JSONObject，返回 `"true"` 表示通过，空返回回退 `msgTemplate`；注意 JS 执行来自配置数据，应仅由管理员维护
5. **缓存生效延迟**：修改规则后其他实例最长 12 秒生效（@Scheduled 轮询周期），需即时生效可调用各 Service 的 clearCache
6. **Route 常量**：路由统一在 `rest/Route.java` 维护，`@Router(module = "micro-form", prefix = "/micro-form")`
7. **异常处理**：校验失败返回 `R.error(...)` 不抛异常；管理端统一 `ValidationException.of("消息")`

---

**最后更新时间**: 2026-11-28
