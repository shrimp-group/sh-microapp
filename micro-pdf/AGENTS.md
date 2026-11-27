# micro-pdf 模块开发指南

## 模块概述

micro-pdf 是 sh-microapp 微应用集合中的 PDF 生成模块，基于 Thymeleaf 模板 + flying-saucer（iText5）将 HTML 模板渲染为 PDF。提供模板管理（CRUD + Mock 在线预览）与面向宿主应用的生成 API（`PdfApi`）。

- **GroupId**: `com.wkclz.microapp`
- **ArtifactId**: `micro-pdf`
- **API 前缀**: `/micro-pdf`
- **数据表**: `mdm_pdf_template`（PDF 模板）

## 架构设计

```
REST            PdfTemplateRest（模板 CRUD + Mock 预览）
API             PdfApi（responsePdf / writePdf，供宿主应用调用）
    ↓
Helper          PdfHelper（JSON→Context / Thymeleaf 渲染 / ITextRenderer 出 PDF / 响应输出）
Cache           PdfTemplateCache（Redis 时间戳 + 本地 Map + 定时轮询刷新）
    ↓
Service         MdmPdfTemplateService → MdmPdfTemplateMapper（getPdfTemplateList / get4Cache）
```

| 环节 | 实现 | 说明 |
|------|------|------|
| 模板引擎 | `PdfHelper.thymeleafRenderer` | 模板字符串（非文件）+ JSON 数据渲染 HTML；`<body>` 强制注入宋体 `SimSun` |
| PDF 渲染 | `PdfHelper.pdfRenderer` | `ITextRenderer`（PDF 1.7），Unix 且配置了字体路径时注册 simsun 字体，输出到临时目录 |
| 缓存 | `PdfTemplateCache` | Redis key `sh:micro:pdf:cache:time` 记录变更时间戳，各实例 `@Scheduled`（12 秒，首次延迟 40 秒）轮询比对，`init()` 内 5 秒防抖；`clearCache()` 写 Redis 时间戳并立即刷新本地 |

## 目录结构

```
micro-pdf/src/main/java/com/wkclz/micro/pdf/
├── PdfAutoConfig.java                 # 自动配置（@ComponentScan + @MapperScan）
├── api/
│   └── PdfApi.java                    # 对外生成 API（@Component）：writePdf / responsePdf
├── bean/
│   ├── entity/
│   │   └── MdmPdfTemplate.java        # 模板实体（mdm_pdf_template，含手写 copy/copyIfNotNull）
│   ├── req/                           # Page/Info/Create/Update/Remove/Mock 六个 Req
│   └── resp/                          # PdfTemplatePageResp / PdfTemplateInfoResp
├── cache/
│   └── PdfTemplateCache.java          # 模板缓存（Redis 时间戳 + 定时轮询）
├── config/
│   └── PdfConfig.java                 # shrimp.pdf.simsun.path 字体路径配置
├── helper/
│   └── PdfHelper.java                 # getContext / thymeleafRenderer / pdfRenderer / pdfResponse
├── mapper/
│   └── MdmPdfTemplateMapper.java      # getPdfTemplateList / get4Cache
└── rest/
    ├── Route.java                     # 路由常量（@Router）
    └── PdfTemplateRest.java           # 模板管理 REST
```

resources：`META-INF/spring/...AutoConfiguration.imports`、`mapper/MdmPdfTemplateMapper.xml`

## API 端点清单

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/micro-pdf/template/page` | 模板分页 |
| GET | `/micro-pdf/template/info` | 模板详情 |
| POST | `/micro-pdf/template/create` | 新增模板（校验 mockData 必须为合法 JSON） |
| POST | `/micro-pdf/template/update` | 修改模板（同上校验） |
| POST | `/micro-pdf/template/remove` | 删除模板 |
| POST | `/micro-pdf/template/mock` | 在线预览：`templateContext + mockData` 直接渲染输出 PDF 流 |

## 核心机制

### 生成流程

```
templateCode → PdfTemplateCache 取模板
    → PdfHelper.getContext(data)      # JSON 字符串 → Thymeleaf Context（非 JSON 抛 ValidationException）
    → PdfHelper.thymeleafRenderer     # 模板字符串渲染为 HTML，body 注入宋体
    → PdfHelper.pdfRenderer           # ITextRenderer.layout() → createPDF → 临时目录文件
    → PdfHelper.pdfResponse           # Content-Disposition: inline + application/pdf 写响应流
```

- 模板内容存库（`template_context` 字段），支持 Thymeleaf 语法（`th:text`、`th:each` 等）
- `PdfApi.writePdf(templateCode, data)` 返回临时文件全路径；`responsePdf(...)` 直接写 `HttpServletResponse`
- 生成的 PDF 文件写入 `FileUtil.getTempPath("pdf")` 临时目录，不落对象存储

### 宿主应用使用方式

```java
@Autowired PdfApi pdfApi;

// 直接输出到响应
pdfApi.responsePdf("order_pdf", jsonData, response);

// 只生成，拿临时文件路径自行处理
String pdfPath = pdfApi.writePdf("order_pdf", jsonData);
```

## 数据库表

### mdm_pdf_template

| 字段 | Java 字段 | 说明 |
|------|----------|------|
| `template_code` | `templateCode` | 模板编码（唯一，Service 内 duplicateCheck） |
| `template_name` | `templateName` | 模板名称 |
| `template_context` | `templateContext` | 模板内容（Thymeleaf HTML 字符串） |
| `mock_data` | `mockData` | 模拟数据（JSON，Mock 预览用） |

## 配置项

```yaml
shrimp:
  pdf:
    simsun:
      path: /usr/share/fonts/zh/simsun.ttf   # 宋体字体路径（默认值），仅 Unix 系统加载
```

## 依赖

- `sh-redis`：StringRedisTemplate（缓存时间戳）
- `spring-boot-starter-validation`：Req 参数校验
- `org.xhtmlrenderer:flying-saucer-pdf-itext5`：HTML→PDF 渲染
- `org.thymeleaf:thymeleaf`：模板引擎

## 注意事项

1. 中文字体：模板渲染依赖宋体，Linux 部署需保证 `shrimp.pdf.simsun.path` 指向的字体文件存在；Windows 本地开发不加载外部字体（`isUnix()` 判断）
2. `mockData` / 生成入参 `data` 必须是合法 JSON 对象字符串，解析失败抛 `ValidationException`
3. 模板编码唯一性由 `MdmPdfTemplateService.duplicateCheck` 保证（新增/修改均校验）
4. 缓存为"本地 Map + Redis 时间戳轮询"模式（非 Pub/Sub），多实例最长 12 秒延迟生效；管理端改完模板如需立即生效可调用 `PdfTemplateCache.clearCache()`
5. 缓存对不存在的 templateCode 也会写入空占位，避免反复查库；模板新增后占位需等 `init()` 刷新
6. 生成的 PDF 临时文件无自动清理逻辑，依赖宿主的临时目录清理策略

**最后更新时间**: 2026-11-28
