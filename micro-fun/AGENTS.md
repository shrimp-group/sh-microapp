# micro-fun 模块开发指南

## 模块概述

micro-fun 是 sh-microapp 微应用集合中的函数管理模块，提供函数分类（树形）与函数定义（多语言脚本）的管理能力，并通过 GraalVM Polyglot / Groovy / QLExpress 多脚本引擎执行函数体，支持线上测试执行。

- **GroupId**: `com.wkclz.microapp`
- **ArtifactId**: `micro-fun`
- **API 前缀**: `/micro-fun`
- **外部依赖**: GraalVM Polyglot（JS/Python/Ruby）、Groovy、QLExpress
- **数据库表**: `fun_category`（函数分类）、`fun_function`（函数体）

## 架构设计

```
FunCategoryRest / FunFunctionRest（分类与函数管理 REST）
    ↓
FunCategoryService / FunFunctionService    # 单表 CRUD + 树组装 + funCode 查重
    ↓
ScriptService                              # funCode → ScriptEngine 实例（ConcurrentMap 缓存）
    ├── JavaScriptEngineImpl               # GraalVM js
    ├── PythonEngineImpl                   # GraalVM python-community
    ├── RubyEngineImpl                     # GraalVM ruby-community
    ├── GroovyEngineImpl                   # org.codehaus.groovy
    ├── QLExpressEngineImpl                # com.alibaba QLExpress
    └── NonEngineImpl                      # 函数不存在的占位实现
```

| 模式 | 应用 | 说明 |
|------|------|------|
| 多语言引擎 | `ScriptEngine` 抽象类 + 5 种实现 | 按 `funLanguage` switch 分发 |
| 模板拼装 | `JavaScriptEngineImpl.initContext()` | `function {funCode}({funParams}) { {funBody} }` 模板字符串 |
| 实例缓存 | `ScriptService.FUN_CACHE` | funCode → ScriptEngine（ConcurrentHashMap.computeIfAbsent） |

## 目录结构

```
src/main/java/com/wkclz/micro/fun/
├── FunAutoConfig.java                 # 自动配置（@ComponentScan + @MapperScan）
├── bean/
│   ├── dto/
│   │   ├── FunCategoryDto.java        # 分类 DTO（含 children 树结构）
│   │   └── FunFunctionDto.java        # 函数 DTO
│   ├── entity/
│   │   ├── FunCategory.java           # 函数分类实体（表 fun_category）
│   │   └── FunFunction.java           # 函数体实体（表 fun_function）
│   ├── req/                           # 分类/函数 CRUD Req + FunFunctionTestReq
│   └── resp/                          # 分类/函数 Resp（含 FunCategoryTreeResp）
├── engine/
│   ├── ScriptEngine.java              # 抽象基类（funReturn 类型映射 + exec 模板）
│   ├── ScriptService.java             # 引擎获取与缓存入口
│   └── impl/
│       ├── GraalvmLanguage.java       # GraalVM 语言常量
│       ├── JavaScriptEngineImpl.java  # JS 引擎（GraalVM Context）
│       ├── PythonEngineImpl.java
│       ├── RubyEngineImpl.java
│       ├── GroovyEngineImpl.java
│       ├── QLExpressEngineImpl.java
│       └── NonEngineImpl.java         # 函数不存在占位
├── mapper/
│   ├── FunCategoryMapper.java
│   └── FunFunctionMapper.java
├── rest/
│   ├── Route.java                     # 路由常量（@Router 注解）
│   ├── FunCategoryRest.java           # 分类管理 REST
│   └── FunFunctionRest.java           # 函数管理 REST
└── service/
    ├── FunCategoryService.java        # 分类树组装 + ctg_ 编码生成
    └── FunFunctionService.java        # 函数 CRUD + funCode 唯一校验
```

resources：`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`、`mapper/*.xml`

## API 端点清单（前缀 `/micro-fun`，共 14 个）

| 分组 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 分类 | GET | `/category/list` | 分类列表 |
| 分类 | GET | `/category/tree` | 分类树（pcode 层级组装） |
| 分类 | GET | `/category/info` | 分类详情 |
| 分类 | POST | `/category/create` | 创建分类（自动生成 `ctg_` 前缀 categoryCode） |
| 分类 | POST | `/category/update` | 更新分类 |
| 分类 | POST | `/category/remove` | 删除分类 |
| 分类 | GET | `/category/options` | 分类选项（树形） |
| 函数 | GET | `/function/page` | 函数分页 |
| 函数 | GET | `/function/info` | 函数详情 |
| 函数 | POST | `/function/create` | 创建函数（funCode 唯一校验） |
| 函数 | POST | `/function/update` | 更新函数 |
| 函数 | POST | `/function/remove` | 删除函数 |
| 函数 | GET | `/function/options` | 函数选项列表 |
| 函数 | POST | `/function/test` | 测试执行函数（funCode + funBody 等直接传入执行） |

## 核心机制

### 函数定义模型（fun_function）

| 字段 | 说明 |
|------|------|
| `categoryCode` | 所属分类 |
| `funCode` | 函数编码（唯一索引，同时作为脚本函数名） |
| `funParams` | 参数列表（拼入函数签名的形参串，如 `name,age`） |
| `funLanguage` | 语言：JavaScript / Python / Groovy / QLExpress / Ruby |
| `funBody` | 函数体脚本 |
| `funReturn` | 返回类型：String / Integer / Long / Double / BigDecimal |
| `funMockData` | 模拟数据 |
| `visible` / `defaultFlag` | 可见 1/0、内置标识 |

### ScriptService 引擎获取

```java
@Autowired ScriptService scriptService;

// 线上调用：按 funCode 从缓存取引擎（函数不存在时返回 NonEngineImpl，再判 funBody 为空抛异常）
ScriptEngine engine = scriptService.getEngine("fun_xxx");
Object result = engine.exec(paramJson);          // 返回值按 funReturn 映射的类型转换

// 测试执行：直接以传入的 FunFunction 构建引擎，不读缓存（配合 /function/test 端点）
ScriptEngine testEngine = scriptService.getEngineTest(fun);
```

- `ScriptEngine` 基类将 `funReturn` 字符串映射为 Java 类型（仅支持 String/Integer/Long/Double/BigDecimal）
- `JavaScriptEngineImpl`：GraalVM `Context.create()` 评估拼装后的函数模板，执行时 `getBindings("js").getMember(funCode).execute(param)`
- 语言分发不支持的类型抛 `ValidationException`（暂不支持的语言）

### 执行结果类型

`exec(param)` 返回 `Object`，按 `funReturn` 声明经 `Value.as(Class)` / 引擎转换；REST 层直接 `R<Object>` 返回。

## 数据库表

### fun_category（函数分类）

| 字段 | 说明 |
|------|------|
| `pcode` | 父分类编码（树形） |
| `category_code` | 分类编码（`ctg_` 前缀，RedisIdGenerator 生成） |
| `category_name` | 分类名称 |
| `description` / `visible` | 描述 / 可见 1/0 |

### fun_function（函数体）

见"函数定义模型"，含 BaseEntity 标准字段。

## 依赖关系

- **GraalVM Polyglot 23.1.2**：polyglot / graal-sdk / truffle-api；python-community、ruby-community（pom 类型）
- **org.graalvm.js:js 23.0.7**：js 坐标仅发布至 23.0.x，无法随 polyglot 版本统一（pom 内注释说明）
- **QLExpress / Groovy**：表达式与脚本引擎
- **sh-core / sh-mybatis / sh-tool**：BaseEntity、BaseService、PageQuery
- **sh-redis**：RedisIdGenerator（分类编码生成）

模块间依赖：sh-microapp 内无其他 micro-* 模块引用 `com.wkclz.micro.fun`，通过 REST 端点或直接注入 `ScriptService` 供宿主应用调用。

## 开发注意事项

1. **JDK 25 兼容风险**：pom 已注释说明 GraalVM 23.1.2 与运行 JDK 25 存在兼容性风险，整体升级 25.x 待设计评审（需运行时验证脚本引擎，模块暂无测试）
2. **引擎缓存不失效**：`FUN_CACHE` 无主动失效机制，`/function/update` 修改函数体后线上缓存的引擎不会自动刷新，需重启进程；新函数编码首次 `getEngine` 后缓存生效
3. **funCode 即函数名**：脚本函数名与 funCode 一致，funCode 命名需符合各语言函数名规范
4. **脚本安全**：funBody 为任意可执行脚本（JS/Python/Groovy 等），仅应由管理员维护，避免暴露给不可信输入；Groovy/QLExpress 引擎注意沙箱能力
5. **返回类型受限**：`funReturn` 仅支持 String/Integer/Long/Double/BigDecimal 五种映射，其余返回按引擎默认处理
6. **测试端点**：`/function/test` 不落缓存、直接以请求体中的函数定义构建引擎执行，可用于上线前验证
7. **异常处理**：统一 `ValidationException.of("消息")`，REST 返回 `R<T>`

---

**最后更新时间**: 2026-11-28
