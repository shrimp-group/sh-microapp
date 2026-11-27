# micro-autotest 模块开发指南

## 模块概述

micro-autotest 是 sh-microapp 微应用集合中的 REST 接口自动化测试模块，运行时扫描宿主应用指定包下的 Controller，自动生成参数、Mock 依赖后通过 MockMvc 真实调用接口，并输出 Markdown/HTML 测试报告。

- **GroupId**: `com.wkclz.microapp`
- **ArtifactId**: `micro-autotest`
- **API 前缀**: `/micro-autotest`
- **包路径**: `com.wkclz.micro.autotest`
- **无数据库表**：纯运行时能力，不落库

## 架构设计

```
AutoTestRest（REST 入口）
    ↓
ApiScanner          # 扫描 @RestController/@Controller，解析 URI/参数/@Schema 描述/@Router 归属模块
    ↓
TestExecutor        # MockMvc 逐接口执行：Mock 依赖 → 生成参数 → 发起请求 → 统计结果
    ├── MockHelper              # Mockito.mock 控制器依赖 Bean，执行后 resetAll
    ├── TestDataGenerator       # 按类型生成测试数据（原始值/时间/枚举首值/POJO 反射实例）
    └── AutoMockBeanPostProcessor  # 可选开关：把 `.mapper.` Bean 定义替换为 Mockito.mock
    ↓
ReportGenerator     # 生成 MD / HTML 报告，可落盘保存
```

## 目录结构

```
src/main/java/com/wkclz/micro/autotest/
├── AutoTestAutoConfig.java            # 自动配置（@ComponentScan）
├── bean/
│   ├── dto/
│   │   ├── ApiInfo.java               # 接口信息（uri/method/name/desc/module/params/returnType）
│   │   ├── ApiParamInfo.java          # 参数信息（name/type/requestBody/pathVariable/requestParam）
│   │   ├── TestCaseResult.java        # 单用例结果（httpStatus/costTimeMs/requestBody/responseBody/errorMessage）
│   │   └── TestReport.java            # 汇总报告（总数/成功/4xx fail/5xx error/总耗时）
│   ├── req/
│   │   ├── ApiListReq.java            # 接口列表请求（packagePath）
│   │   └── RunReq.java                # 执行请求（packagePath/reportDir）
│   └── resp/
│       ├── ApiListResp.java
│       └── TestReportResp.java
├── executor/
│   └── TestExecutor.java              # MockMvc 执行引擎
├── mock/
│   ├── AutoMockBeanPostProcessor.java # BeanDefinition 级 Mapper Mock 开关
│   ├── MockHelper.java                # 控制器依赖 Mock 与参数生成
│   └── TestDataGenerator.java         # 测试数据生成（SKIP_FIELDS: id/version/deleted）
├── report/
│   └── ReportGenerator.java           # MD/HTML 报告生成与落盘
├── rest/
│   ├── Route.java                     # 路由常量（@Router 注解）
│   └── AutoTestRest.java              # REST 入口
└── scanner/
    └── ApiScanner.java                # Controller / Router 注解扫描
```

resources：`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

## API 端点清单

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/micro-autotest/api/list` | 扫描并返回接口清单（参数 packagePath，为空取默认包） |
| POST | `/micro-autotest/run` | 执行测试（body: packagePath/reportDir），报告保存在 `latestReport` |
| GET | `/micro-autotest/report` | 最近一次测试报告 JSON |
| GET | `/micro-autotest/report/md` | 最近一次测试报告 Markdown（`text/markdown`） |
| GET | `/micro-autotest/report/html` | 最近一次测试报告 HTML（`text/html`） |

## 核心机制

### 接口扫描（ApiScanner）

| 项 | 规则 |
|------|------|
| 默认包 | `@SpringBootApplication` 注解类所在包，兜底为自身包前两段 |
| 接口识别 | `@RestController`/`@Controller` 类 + `@RequestMapping`/`@GetMapping`/`@PostMapping`/`@PutMapping`/`@DeleteMapping` 方法 |
| 前缀 | 类级 `@RequestMapping` value[0] |
| 接口名 | URI 去除 `/ - { } *` 后驼峰化 |
| 描述 | 方法/路由常量字段上的 `@Schema(description)`，Route 常量描述按 URI 匹配覆盖 |
| 模块归属 | Controller 包匹配 `@Router` 注解接口所在包时写入 `module` |
| 参数 | 解析 `@RequestBody`/`@PathVariable`/`@RequestParam` 标记 |

### 测试执行（TestExecutor）

1. `MockMvcBuilders.webAppContextSetup(webApplicationContext)` 构建执行环境（进程内调用，无网络）
2. 每个接口执行前 `MockHelper.mockControllerDependencies` 将控制器字段依赖替换为 Mockito mock（执行后 `resetAll`）
3. 参数由 `TestDataGenerator` 按类型生成：基础类型取固定值（int=1/String=""）、时间取当前、枚举取首值、POJO 反射无参构造递归填充（深度≤3，跳过 id/version/deleted）；GET 请求将 Req 对象字段展开为 `a.b` 形式 query 参数
4. 结果统计：2xx 计成功，4xx 计 fail，5xx 计 error

### Mapper 自动 Mock（AutoMockBeanPostProcessor）

实现 `BeanDefinitionRegistryPostProcessor`，受静态开关 `enableMock()/disableMock()` 控制（默认关闭）：开启时注册阶段将类名含 `.mapper.` 的 Bean 定义替换为 `Mockito.mock` 实例，用于隔离数据库的冒烟测试。

### 报告生成（ReportGenerator）

- `generateMd` / `generateHtml`：汇总表 + 明细表 + 失败用例详情（请求体/响应体/错误信息）
- `saveReport(report, dirPath)`：落盘为 `autotest_report_yyyyMMdd_HHmmss.md` 与 `.html`
- `latestReport` 为内存 volatile 变量，多实例部署时报告不共享，需以 `/run` 后立即获取为准

## 依赖关系

- **sh-web / sh-redis / sh-core**：框架基础能力（R、@Router、ClassUtil）
- **spring-test**：MockMvc / MockHttpServletResponse（编译期依赖）
- **mockito-core**：Bean Mock
- **spring-boot-starter-test**：test scope
- **wiremock-standalone**：pom 已声明，当前代码未引用（预留）

模块间依赖：sh-microapp 内无其他 micro-* 模块引用 `com.wkclz.micro.autotest`，仅通过 REST 端点对外提供能力。

## 开发注意事项

1. **建议环境**：模块通过 MockMvc 走完整 Filter/Interceptor 链，认证拦截未放行时接口会 4xx，建议仅在测试环境启用，且按需调整鉴权白名单
2. **@PathVariable 未赋值**：路径变量占位符按原样保留（如 `{id}`），含路径变量的接口可能 4xx，属预期
3. **副作用风险**：`/run` 会真实调用所有扫描到的接口（依赖已 Mock、参数合法但业务校验可能失败），POST/DELETE 接口需评估幂等性后再执行
4. **报告不落库**：报告仅存内存 + 可选落盘（`reportDir`），服务重启即丢失
5. **Mapper Mock 开关**：`AutoMockBeanPostProcessor.enableMock()` 需在 Bean 注册前调用（如启动早期），对运行中容器无效
6. **异常处理**：REST 返回 `R<T>`；`/report/md`、`/report/html` 为外部协议固定签名，豁免 Req/Resp 封装直接返回字符串

---

**最后更新时间**: 2026-11-28
