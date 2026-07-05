# micro-autotest

自动化 REST API 测试模块，基于 Spring Boot 4.x，零配置即可对项目中的 RESTful 接口发起自动化测试。

## 功能特性

- **运行时接口扫描** — 自动扫描 `@RestController` 下的所有 REST 接口，分析参数注解（`@RequestBody` / `@PathVariable` / `@RequestParam`）
- **实时用例生成** — 无需编写测试代码，根据接口参数结构自动生成测试数据
- **零配置 Mock** — 自动 Mock MyBatis Mapper、Service、Redis 等外部依赖
- **测试报告** — 生成 Markdown 和 HTML 两种格式的测试报告

## 引入依赖

```xml
<dependency>
    <groupId>com.wkclz.microapp</groupId>
    <artifactId>micro-autotest</artifactId>
</dependency>
```

## 使用方法

### 1. 查看扫描到的接口

```
GET /micro-autotest/api/list
```

可选参数 `packagePath`，不传则默认扫描主类（`@SpringBootApplication`）所在包。

```bash
# 扫描默认包
curl http://localhost:8080/micro-autotest/api/list

# 扫描指定包
curl "http://localhost:8080/micro-autotest/api/list?packagePath=com.wkclz.demo"
```

返回示例：

```json
{
  "code": 200,
  "data": [
    {
      "method": "GET",
      "uri": "/demo/user/list",
      "name": "demo_user_list",
      "desc": "用户列表",
      "params": [],
      "returnType": "com.wkclz.core.base.R"
    },
    {
      "method": "POST",
      "uri": "/demo/user/insert",
      "name": "demo_user_insert",
      "desc": "新增用户",
      "params": [
        {
          "name": "user",
          "type": "com.wkclz.demo.entity.User",
          "requestBody": true,
          "pathVariable": false,
          "requestParam": false
        }
      ],
      "returnType": "com.wkclz.core.base.R"
    }
  ]
}
```

### 2. 执行自动化测试

```
POST /micro-autotest/run
```

可选参数：

| 参数 | 说明 |
|------|------|
| `packagePath` | 指定扫描包路径，不传则使用默认包 |
| `reportDir` | 报告保存目录，不传则不保存文件 |

```bash
# 执行测试（不保存报告文件）
curl -X POST http://localhost:8080/micro-autotest/run

# 执行测试并保存报告到指定目录
curl -X POST "http://localhost:8080/micro-autotest/run?reportDir=/tmp/autotest"
```

返回示例：

```json
{
  "code": 200,
  "data": {
    "startTime": "2026-05-20T10:30:00",
    "endTime": "2026-05-20T10:30:02",
    "totalCostTimeMs": 1500,
    "totalApiCount": 10,
    "successCount": 8,
    "failCount": 1,
    "errorCount": 1,
    "results": [
      {
        "uri": "/demo/user/list",
        "method": "GET",
        "desc": "用户列表",
        "success": true,
        "httpStatus": 200,
        "costTimeMs": 120,
        "responseBody": "{\"code\":200,...}"
      }
    ]
  }
}
```

### 3. 查看测试报告

| 端点 | 格式 | 说明 |
|------|------|------|
| `GET /micro-autotest/report` | JSON | 获取最新测试报告数据 |
| `GET /micro-autotest/report/md` | Markdown | 获取 Markdown 格式报告 |
| `GET /micro-autotest/report/html` | HTML | 获取 HTML 格式报告（浏览器直接查看） |

```bash
# JSON 报告
curl http://localhost:8080/micro-autotest/report

# Markdown 报告
curl http://localhost:8080/micro-autotest/report/md

# HTML 报告（浏览器打开）
open http://localhost:8080/micro-autotest/report/html
```

## Mock 策略

### 自动 Mock 范围

| 依赖类型 | Mock 方式 | 说明 |
|----------|-----------|------|
| MyBatis Mapper | Mockito Mock | 包路径含 `.mapper.` 的 Bean |
| Service 层 | Mockito Mock | 控制器注入的依赖自动 Mock |
| Redis | Mockito Mock | RedisTemplate 等 Bean 自动 Mock |
| 外部 HTTP API | WireMock | 需配置 WireMock 服务 |

### 测试数据生成规则

| Java 类型 | 生成的值 |
|-----------|----------|
| `boolean` / `Boolean` | `true` |
| `int` / `Integer` | `1` |
| `long` / `Long` | `1L` |
| `double` / `Double` | `1.0` |
| `String` | `""` |
| `LocalDateTime` | 当前时间 |
| `LocalDate` | 当前日期 |
| `Enum` | 第一个枚举值 |
| `List` / `Set` / `Map` | 空集合 |
| POJO | 递归反射构造，深度 3 层 |

框架管理字段（`id`, `version`, `deleted`）自动跳过。

### 容器级 Mock（可选）

如需在 Spring 容器启动时就将 Mapper Bean 替换为 Mock，在应用启动前调用：

```java
AutoMockBeanPostProcessor.enableMock();
```

## 报告文件

执行测试时指定 `reportDir` 参数，会在该目录下生成：

```
autotest_report_20260520_103000.md
autotest_report_20260520_103000.html
```

- **MD 报告** — 包含 Summary 表格、Test Details 表格、Failed Cases 详情
- **HTML 报告** — 响应式布局，颜色编码（绿色=成功，红色=失败，橙色=错误），可直接在浏览器查看

## 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 4.x | 基础框架 |
| Spring MockMvc | — | HTTP 请求模拟 |
| Mockito | — | Bean Mock |
| WireMock | 3.13.1 | 外部 API Mock |
| fastjson2 | — | JSON 序列化 |
| Java | 25 | 运行环境 |
