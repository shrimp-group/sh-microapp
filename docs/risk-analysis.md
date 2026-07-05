# sh-microapp 系统风险评估报告

**评估日期**：2026-06-02
**评估范围**：sh-microapp 全部 18 个模块源代码
**评估方法**：静态代码审计 + 架构分析

---

## 风险总览

| 严重等级 | 数量 | 涉及模块 |
|----------|------|----------|
| 🔴 严重 (Critical) | 3 | micro-dbview, micro-fun, micro-pay |
| 🟠 高 (High) | 14 | micro-pay, micro-fileos, micro-dbview, micro-seq, micro-mask, micro-fun, micro-wxmp, micro-wxapp |
| 🟡 中 (Medium) | 15 | micro-pay, micro-fileos, micro-dbview, micro-mask, micro-wxapp, 多模块缓存 |
| 🔵 低 (Low) | 8 | 多模块 |

---

## 1. 性能风险

### 1.1 🔴 SQL 执行无超时控制 — micro-dbview

**位置**：[DbviewSqlService.java](micro-dbview/src/main/java/com/wkclz/micro/dbview/service/DbviewSqlService.java)

**问题**：`doExecute()` 创建 `Statement` 时未调用 `stmt.setQueryTimeout()`，长时间运行的 SQL 会阻塞数据库连接和线程资源。虽然 `DbviewConfig` 有 `sqlTimeoutSeconds` 配置项，但代码中未实际使用。

**后果**：恶意或低效 SQL 可无限期占用数据库连接，导致连接池耗尽，影响整个系统的数据库可用性。

**建议**：
```java
Statement stmt = conn.createStatement();
stmt.setQueryTimeout(dbviewConfig.getSqlTimeoutSeconds());
```

### 1.2 🟠 序列号生成 SERIALIZABLE 锁持有时间不可控 — micro-seq

**位置**：[MdmSequenceService.java](micro-seq/src/main/java/com/wkclz/micro/seq/service/MdmSequenceService.java) → `genSequences()`

**问题**：`size` 参数无上限校验，攻击者可传入 `Integer.MAX_VALUE`，在 SERIALIZABLE 隔离级别下长时间持有数据库锁并生成海量序列号。

**后果**：DoS 攻击，阻塞所有需要生成序列号的业务请求。

**建议**：增加 `size` 上限校验（如 `Math.min(size, 1000)`），并增加超时机制。

### 1.3 🟠 CompletableFuture 使用默认 ForkJoinPool — micro-dbview

**位置**：[DbviewSqlService.java](micro-dbview/src/main/java/com/wkclz/micro/dbview/service/DbviewSqlService.java)、[DbviewConnectionService.java](micro-dbview/src/main/java/com/wkclz/micro/dbview/service/DbviewConnectionService.java)

**问题**：`CompletableFuture.supplyAsync()` 使用公共 `ForkJoinPool`，若大量并发 SQL 执行，会耗尽线程池影响其他异步任务。

**建议**：创建专用线程池 `ExecutorService`，限制最大线程数和队列长度。

### 1.4 🟡 数据库连接无池化 — micro-dbview

**位置**：[DbviewConnectionService.java](micro-dbview/src/main/java/com/wkclz/micro/dbview/service/DbviewConnectionService.java)

**问题**：每次操作都创建新连接 `DataSourceInfo.getConnect(info)`，无连接池，频繁操作性能差且可能耗尽数据库连接。

**建议**：使用 HikariCP 等连接池管理动态数据源连接，设置最大连接数和空闲超时。

### 1.5 🟡 缓存全量加载无分页 — 多模块

**位置**：
- [DictCache.java](micro-dict/src/main/java/com/wkclz/micro/dict/cache/DictCache.java) → `loadCache()`
- [BucketCache.java](micro-fileos/src/main/java/com/wkclz/micro/fileos/helper/BucketCache.java) → `loadCache()`
- [MaterialGroupCache.java](micro-material/src/main/java/com/wkclz/micro/material/cache/MaterialGroupCache.java) → `loadCache()`

**问题**：所有缓存模块在 `loadCache()` 时一次性加载全量数据到内存，无分页或懒加载机制。随着业务增长，数据量可能达到数十万级。

**建议**：对大数据量缓存实施分片加载或 LRU 淘汰策略；设置缓存容量上限。

### 1.6 🟡 公众号消息路由每次重建 — micro-wxmp

**位置**：[WxMpConfiguration.java](micro-wxmp/src/main/java/com/wkclz/micro/wxmp/config/WxMpConfiguration.java) → `messageRouter()`

**问题**：每次调用 `messageRouter()` 都 `new WxMpMessageRouter(mpService)` 并重新配置所有规则，造成不必要的对象创建和 GC 压力。

**建议**：将 `WxMpMessageRouter` 缓存在 `MP_ROUTERS` Map 中，与 `MP_SERVICES` 同生命周期。

---

## 2. 内存隐患

### 2.1 🔴 脚本引擎缓存无失效机制 — micro-fun

**位置**：[ScriptService.java](micro-fun/src/main/java/com/wkclz/micro/fun/engine/ScriptService.java) → `FUN_CACHE`

**问题**：`FUN_CACHE` 是 `ConcurrentHashMap`，一旦脚本被缓存，数据库中更新或删除脚本内容后缓存不会刷新。已删除的危险脚本仍可执行，且缓存无限增长。

**后果**：
- 内存泄漏：脚本对象永不被 GC 回收
- 安全漏洞：已废弃的 RCE 脚本继续可执行

**建议**：
1. 增加 Redis Pub/Sub 缓存刷新机制（与其他模块保持一致）
2. 设置缓存容量上限和 LRU 淘汰策略
3. 脚本更新/删除时主动清除缓存

### 2.2 🟠 支付客户端缓存失效逻辑失效 — micro-pay

**位置**：
- [WxpayClientCache.java](micro-pay/src/main/java/com/wkclz/micro/pay/cache/WxpayClientCache.java) → `autoClear()`
- [AlipayClientCache.java](micro-pay/src/main/java/com/wkclz/micro/pay/cache/AlipayClientCache.java) → `autoClear()`

**问题**：`autoClear()` 仅将 `CACHE_TIME` 设为 `null`，但 `CACHE_WXPAY`/`CACHE_ALIPAY` 和 `CACHE_CONFIG` 中的旧数据不会被清除。`getClient()` 因 `containsKey` 为 true 直接返回旧缓存，`autoClear()` 实际上无法清除已缓存的客户端。

**后果**：配置更新后旧客户端继续使用，可能导致支付请求使用过期证书/密钥。

**建议**：`autoClear()` 应同时清除 `CACHE_WXPAY`/`CACHE_ALIPAY` 和 `CACHE_CONFIG` 中的对应条目。

### 2.3 🟠 AlipayClientCache Redis Key 与 WxpayClientCache 冲突 — micro-pay

**位置**：[AlipayClientCache.java](micro-pay/src/main/java/com/wkclz/micro/pay/cache/AlipayClientCache.java) 第28行

**问题**：`ALIPAY_CACHE_KEY = "sh:micro:wxpay:cache:time"` 应为 `alipay`，实际硬编码为 `wxpay`，与 WxpayClientCache 的 key 完全相同。两个缓存模块共用同一个 Redis key，互相干扰清除信号。

**后果**：微信支付配置变更可能触发支付宝缓存刷新，反之亦然，导致不必要的缓存重建和短暂的性能抖动。

**建议**：将 key 修正为 `"sh:micro:alipay:cache:time"`。

### 2.4 🟡 序列号 Integer 溢出 — micro-seq

**位置**：[MdmSequenceService.java](micro-seq/src/main/java/com/wkclz/micro/seq/service/MdmSequenceService.java) → `genSequences()`

**问题**：`sequence` 是 `Integer` 类型，当序列号超过 `Integer.MAX_VALUE`（约21亿）时会溢出，导致序列号重复。

**建议**：将 `sequence` 字段类型改为 `Long`，数据库字段改为 `bigint`。

---

## 3. 线程与并发风险

### 3.1 🟠 MaskCache CLEAR_FLAG 竞态条件 — micro-mask

**位置**：[MaskCache.java](micro-mask/src/main/java/com/wkclz/micro/mask/cache/MaskCache.java) → `getClearFlag()`

**问题**：`CLEAR_FLAG` 是普通 `Boolean`（非 volatile、非原子），`getClearFlag()` 读取并重置不是原子操作。在多线程环境下：
- 一个线程读取后重置，另一个线程可能永远看不到 flag 为 true
- `Boolean` 对象的赋值不是原子的（Java 对象引用赋值在 64 位 JVM 上通常是原子的，但规范不保证）

**后果**：脱敏规则更新可能丢失，`MaskResponseAdvice` 使用过期规则，导致敏感数据未脱敏直接返回。

**建议**：
```java
private static final AtomicBoolean CLEAR_FLAG = new AtomicBoolean(false);

public static boolean getClearFlag() {
    return CLEAR_FLAG.getAndSet(false);
}
```

### 3.2 🟡 缓存 volatile + synchronized TOCTOU 竞态 — 多模块

**位置**：
- [DictCache.java](micro-dict/src/main/java/com/wkclz/micro/dict/cache/DictCache.java)
- [BucketCache.java](micro-fileos/src/main/java/com/wkclz/micro/fileos/helper/BucketCache.java)
- [MaterialGroupCache.java](micro-material/src/main/java/com/wkclz/micro/material/cache/MaterialGroupCache.java)

**问题**：`CACHE_XXX` 是 `volatile` 的，但 `get()` 方法未加锁读取，而 `loadCache()` 是 `synchronized` 的。`get()` 中先检查 `CACHE_DICT == null` 再调用 `loadCache()`，存在 TOCTOU（Time-of-Check to Time-of-Use）竞态。虽然 `volatile` 保证了引用可见性，且 `get()` 返回旧 Map 引用在功能上无害（只是读到旧数据），但不够严谨。

**后果**：功能影响较小，最差情况是短暂读到旧缓存数据。

**建议**：将 `get()` 方法中的 null 检查改为双重检查锁模式，或接受当前行为并在注释中说明。

### 3.3 🟡 MaskCache CACHE_TIME/CACHE_ITEM 非 volatile — micro-mask

**位置**：[MaskCache.java](micro-mask/src/main/java/com/wkclz/micro/mask/cache/MaskCache.java)

**问题**：与 DictCache 不同，MaskCache 的 `CACHE_TIME` 和 `CACHE_ITEM` 没有 `volatile` 修饰，多线程下 `getMasks()` 可能读到过期的缓存引用。

**建议**：为 `CACHE_TIME` 和 `CACHE_ITEM` 添加 `volatile` 修饰符。

### 3.4 🟡 WxMp/WxMa 配置 HashMap 非线程安全 — micro-wxmp/wxapp

**位置**：
- [WxMpConfiguration.java](micro-wxmp/src/main/java/com/wkclz/micro/wxmp/config/WxMpConfiguration.java) → `MP_SERVICES`
- [WxMaConfiguration.java](micro-wxapp/src/main/java/com/wkclz/micro/wxapp/config/WxMaConfiguration.java) → `MA_TENANT_SERVICES`

**问题**：使用 `Maps.newHashMap()` 创建普通 HashMap，`init()` 虽然是 `synchronized`，但 `getMpService()` 中先读后写（check-then-act）不是原子的，可能两个线程同时发现 `mpService == null` 并同时进入 `init()`。

**建议**：将 `MP_SERVICES`/`MA_TENANT_SERVICES` 改为 `ConcurrentHashMap`。

### 3.5 🟡 computeIfAbsent 中调用数据库 — micro-fun

**位置**：[ScriptService.java](micro-fun/src/main/java/com/wkclz/micro/fun/engine/ScriptService.java) → `getEngine()`

**问题**：在 `ConcurrentHashMap.computeIfAbsent` 的 lambda 中调用 `funFunctionService.getFunction()`，若数据库操作耗时，会阻塞其他线程对同一 key 的访问（`computeIfAbsent` 在计算期间对 key 段加锁）。

**建议**：先从缓存读取，miss 时在锁外查询数据库，再通过 `putIfAbsent` 写入。

---

## 4. 安全风险

### 4.1 🔴 远程代码执行 (RCE) — micro-fun

**位置**：
- [ScriptService.java](micro-fun/src/main/java/com/wkclz/micro/fun/engine/ScriptService.java)
- [ScriptEngine.java](micro-fun/src/main/java/com/wkclz/micro/fun/engine/ScriptEngine.java)

**问题**：支持 JavaScript/Groovy/Python/Ruby/QLExpress 五种脚本语言执行，其中 Groovy 和 Ruby 可直接调用 Java 系统命令（`Runtime.exec()`），**无任何沙箱限制**。任何能修改 `fun_body` 数据库记录的人都能获得服务器 shell。

**后果**：攻击者可执行任意系统命令，完全控制服务器，窃取数据、植入后门、横向渗透。

**建议**（按优先级）：
1. **立即**：对脚本执行实施安全管理 — 限制脚本创建/修改权限仅限超级管理员
2. **短期**：为 Groovy/Ruby 添加 `SecurityManager` 或 `GroovyShell` 编译器配置白名单
3. **中期**：使用 GraalVM Polyglot 的沙箱模式，限制 `HostAccess` 和 `FileSystem` 访问
4. **长期**：将脚本执行隔离在独立容器/沙箱进程中，通过消息队列通信

### 4.2 🔴 SQL 注入 — micro-dbview

**位置**：
- [DbviewSqlService.java](micro-dbview/src/main/java/com/wkclz/micro/dbview/service/DbviewSqlService.java) → `doExecute()`
- [DbviewDdlService.java](micro-dbview/src/main/java/com/wkclz/micro/dbview/service/DbviewDdlService.java) → `generateDdl()`

**问题**：
1. `doExecute()` 直接将用户输入的 SQL 字符串通过 `Statement.executeQuery(sql)` 执行
2. `generateDdl()` 中 `columnType`、`defaultValue` 等字段未充分转义直接拼接
3. `escapeSql()` 仅替换单引号为 `\'`，但未转义反斜杠本身

**后果**：攻击者可通过 DDL 拼接注入 `VARCHAR(10); DROP TABLE users--` 等恶意 SQL，破坏数据库结构。

**建议**：
1. DDL 生成使用参数化方式或严格的白名单校验（`columnType` 仅允许预定义类型）
2. `escapeSql()` 增加反斜杠转义和 null 字节过滤
3. SQL 执行增加更严格的权限分级和审计日志

### 4.3 🔴 JSON 注入 — micro-pay

**位置**：[AlipayHelper.java](micro-pay/src/main/java/com/wkclz/micro/pay/helper/AlipayHelper.java) 第83-91行

**问题**：使用 `String.format` 拼接 `bizContent` JSON，`outTradeNo`、`totalAmount`、`subject`、`body` 均未转义直接插入 JSON。攻击者可通过 `body` 参数注入 `"` 和 `}` 来篡改 JSON 结构。

**后果**：攻击者可修改 `product_code`、注入额外支付参数，甚至构造恶意支付请求。

**建议**：使用 `JSONObject` 构建 `bizContent`，而非字符串拼接：
```java
JSONObject bizContent = new JSONObject();
bizContent.put("out_trade_no", outTradeNo);
bizContent.put("total_amount", totalAmount);
// ...
```

### 4.4 🟠 支付回调金额未校验 — micro-pay

**位置**：
- [AlipayHelper.java](micro-pay/src/main/java/com/wkclz/micro/pay/helper/AlipayHelper.java) → `payNotify()`
- [WxpayHelper.java](micro-pay/src/main/java/com/wkclz/micro/pay/helper/WxpayHelper.java) → `wxPayNotify()`

**问题**：
1. 支付宝回调 `payNotify()` 代码中注释了"建议商户务必添加以下校验"，但**完全没有实现**金额校验、seller_id 校验、app_id 校验
2. 微信支付回调 `wxPayNotify()` 中 `totalFee.equals(paymentAmount.multiply(new BigDecimal("100")).intValue())` 使用 `BigDecimal.intValue()` 截断小数，`0.005` 元会变成 0，可能绕过金额校验

**后果**：攻击者可伪造低金额支付通知，以 0.01 元的价格完成大额订单支付。

**建议**：
1. 支付宝回调增加金额、seller_id、app_id 校验
2. 微信支付回调使用 `compareTo()` 而非 `intValue() ==` 比较金额

### 4.5 🟠 openId 硬编码 — micro-pay

**位置**：[WxpayHelper.java](micro-pay/src/main/java/com/wkclz/micro/pay/helper/WxpayHelper.java) 第68行

**问题**：`String openId = "openId"; // TODO`，支付请求中使用了硬编码的假 openId，任何用户发起的支付都会使用同一个 openId。

**后果**：支付绑定到错误的微信用户，可能导致支付纠纷和资金损失。

**建议**：从请求上下文或用户会话中获取真实 openId，删除 TODO 硬编码。

### 4.6 🟠 文件类型校验全面放行 — micro-fileos

**位置**：[FileTypeHelper.java](micro-fileos/src/main/java/com/wkclz/micro/fileos/helper/FileTypeHelper.java) → `validateFileContent()`

**问题**：
1. 未知扩展名（`.html`、`.js`、`.exe`、`.sh`、`.svg`）不在 `MAGIC_BYTES_MAP` 中时返回 `true`，完全绕过内容校验
2. 无扩展名/无文件名时返回 `true`
3. `IOException` 时默认放行

**后果**：攻击者可上传恶意 HTML（含 XSS）、SVG（含嵌入 JS）、可执行文件等，通过签名 URL 分发给其他用户。

**建议**：
1. 实施扩展名白名单（仅允许已知安全类型），未知类型默认拒绝
2. 无扩展名文件默认拒绝
3. `IOException` 时默认拒绝而非放行
4. 增加 SVG 内容校验（禁止嵌入 `<script>`）

### 4.7 🟠 敏感凭据明文内存缓存 — 多模块

**位置**：
- [BucketCache.java](micro-fileos/src/main/java/com/wkclz/micro/fileos/helper/BucketCache.java) — OSS accessKey/secretKey
- [WxpayClientCache.java](micro-pay/src/main/java/com/wkclz/micro/pay/cache/WxpayClientCache.java) — 微信支付私钥/证书
- [AlipayClientCache.java](micro-pay/src/main/java/com/wkclz/micro/pay/cache/AlipayClientCache.java) — 支付宝商户私钥
- [WxMpConfiguration.java](micro-wxmp/src/main/java/com/wkclz/micro/wxmp/config/WxMpConfiguration.java) — 公众号 AppSecret
- [WxMaConfiguration.java](micro-wxapp/src/main/java/com/wkclz/micro/wxapp/config/WxMaConfiguration.java) — 小程序 AppSecret

**问题**：所有敏感凭据（OSS 密钥、支付私钥、AppSecret）以明文 String 缓存在静态 Map 中，任何能访问 JVM 内存的攻击者（如反序列化漏洞、堆转储泄露）都可获取全部密钥。

**建议**：
1. 短期：使用 `char[]` 替代 `String` 存储密钥，使用后立即清零
2. 中期：使用 Vault 或 KMS 管理密钥，运行时按需获取，不长期缓存
3. 长期：将密钥存储在 HSM（硬件安全模块）中

### 4.8 🟠 AES 密钥为空时密码明文存储 — micro-dbview

**位置**：[DbviewConnectionService.java](micro-dbview/src/main/java/com/wkclz/micro/dbview/service/DbviewConnectionService.java) → `encryptPassword()`

**问题**：当 `aesKey` 为空或空白时，`encryptPassword()` 直接返回原始密码，数据库密码将以明文存储在 `dbview_datasource` 表中。

**建议**：AES 密钥为空时抛出异常阻止启动，而非静默降级为明文存储。

### 4.9 🟠 危险 SQL 检测可被绕过 — micro-dbview

**位置**：[DbviewDatasourcePermissionService.java](micro-dbview/src/main/java/com/wkclz/micro/dbview/service/DbviewDatasourcePermissionService.java) → `isDangerous()`

**问题**：仅检查 SQL 是否以 `UPDATE`/`DELETE`/`DROP TABLE`/`TRUNCATE` 开头。攻击者可用注释绕过：`/*comment*/DROP TABLE users` 或 `UPDATE/**/table SET...`。

**建议**：使用 SQL 解析器（如 JSqlParser）进行语法级分析，而非简单的前缀匹配。

### 4.10 🟠 多语句检测可被绕过 — micro-dbview

**位置**：[DbviewSqlService.java](micro-dbview/src/main/java/com/wkclz/micro/dbview/service/DbviewSqlService.java) → `containsMultipleStatements()`

**问题**：去掉末尾分号后检查是否还含分号，但排除了以 `"` 或 `'` 结尾的情况。攻击者可用 `'` 结尾绕过：`SELECT 1; DROP TABLE users--'`。

**建议**：使用 JDBC 的 `allowMultiQueries=false` 参数（MySQL 默认关闭），而非自行检测。

### 4.11 🟠 JDBC URL 未校验协议 — micro-dbview

**位置**：[DbviewConnectionService.java](micro-dbview/src/main/java/com/wkclz/micro/dbview/service/DbviewConnectionService.java)

**问题**：未限制 JDBC URL 协议，攻击者可使用 `jdbc:mysql://evil-server/` 连接到恶意 MySQL 服务器，触发 MySQL Connector/J 的反序列化攻击。

**建议**：校验 JDBC URL 必须以 `jdbc:mysql://` 开头且指向已知内网 IP 段，拒绝外部地址。

### 4.12 🟡 Hash 计算失败返回 null — micro-fileos

**位置**：[FileHashHelper.java](micro-fileos/src/main/java/com/wkclz/micro/fileos/helper/FileHashHelper.java) → `computeHash()`

**问题**：`IOException`/`NoSuchAlgorithmException` 时返回 `null`，调用方若未检查 null 就进行 Hash 去重判断，可能导致所有文件被视为"相同"或"不同"，绕过去重安全检查。

**建议**：Hash 计算失败时抛出异常，而非返回 null。

### 4.13 🟡 sanitizeFileName 路径穿越防护可绕过 — micro-fileos

**位置**：[OssUtil.java](micro-fileos/src/main/java/com/wkclz/micro/fileos/utils/OssUtil.java) → `sanitizeFileName()`

**问题**：`replace("..", "_")` 只替换一次，`....` 替换后变为 `.._`，仍包含 `..`。且未过滤 null 字节 `\0` 和换行符 `\n`/`\r`。

**建议**：使用循环替换 `..` 直到不存在，或使用正则 `fileName.replaceAll("\\.\\.", "_")`；增加 null 字节和换行符过滤。

### 4.14 🟡 支付异常信息直接暴露 — micro-pay

**位置**：[WxpayHelper.java](micro-pay/src/main/java/com/wkclz/micro/pay/helper/WxpayHelper.java) 第112行

**问题**：`throw ValidationException.of(e.getMessage())` 将微信支付异常信息直接返回前端，可能泄露内部配置信息（如 mchId、appid）。

**建议**：记录详细异常到日志，返回给前端的错误信息使用通用描述。

### 4.15 🟡 租户隔离 key 冲突 — micro-material

**位置**：[MaterialGroupCache.java](micro-material/src/main/java/com/wkclz/micro/material/cache/MaterialGroupCache.java) → `get()`

**问题**：通过 `tenantCode + ":" + userCode` 拼接 key，若 `tenantCode` 或 `userCode` 包含 `:` 字符，可能导致 key 冲突，跨租户数据泄露。

**建议**：使用不可打印字符（如 `\0`）作为分隔符，或使用复合 key 对象。

---

## 5. 其他潜在风险

### 5.1 可观测性与运维

#### 🟠 缺乏结构化审计日志 — 全局

**问题**：除 micro-audit 模块提供数据变更审计外，系统缺乏安全事件审计日志（如登录失败、权限拒绝、异常请求）。`logHandler`（micro-wxapp）将用户消息内容原样回显，可能泄露系统内部处理逻辑。

**建议**：
1. 增加安全事件审计日志（登录、权限变更、异常访问）
2. 移除 `logHandler` 中的消息回显逻辑
3. 统一日志格式，增加 traceId 便于链路追踪

#### 🟡 异常处理不规范 — 多模块

**问题**：
- `WxpayClientCache.check()` 抛出 `NullPointerException` 而非 `ValidationException`
- `AlipayClientCache.init()` 抛出 `RuntimeException` 而非 `ValidationException`
- `WxMaConfiguration` 使用 `e.printStackTrace()` 而非 SLF4J

**建议**：统一使用 `ValidationException` 或其他 CommonException 子类，禁止 `e.printStackTrace()`。

### 5.2 业务连续性与容错

#### 🟠 缺乏熔断/降级/限流 — 全局

**问题**：系统无任何熔断（Circuit Breaker）、降级（Fallback）或限流（Rate Limiting）机制。第三方依赖（微信支付、支付宝、OSS、K8s API）失效时，请求会直接阻塞或抛出异常，无优雅降级。

**后果**：微信/支付宝 API 超时可能导致支付请求线程池耗尽，影响整个应用。

**建议**：
1. 对第三方 API 调用增加 Resilience4j 熔断器
2. 对支付回调增加幂等性校验和重试机制
3. 对 REST API 增加限流（如 Guava RateLimiter 或 Sentinel）

#### 🟡 支付 H5 异常被吞 — micro-pay

**位置**：[AlipayHelper.java](micro-pay/src/main/java/com/wkclz/micro/pay/helper/AlipayHelper.java) 第149-151行

**问题**：H5 支付的 `AlipayApiException` 仅 `log.error`，未抛出异常，`alipayResponse` 可能为 null，后续 `alipayResponse.getBody()` 会 NPE。

**建议**：捕获异常后抛出 `ValidationException`，或在调用 `getBody()` 前增加 null 检查。

### 5.3 数据一致性与可靠性

#### 🟠 支付缓存失效导致配置不一致 — micro-pay

**位置**：[WxpayClientCache.java](micro-pay/src/main/java/com/wkclz/micro/pay/cache/WxpayClientCache.java)、[AlipayClientCache.java](micro-pay/src/main/java/com/wkclz/micro/pay/cache/AlipayClientCache.java)

**问题**：`autoClear()` 无法真正清除已缓存的客户端（详见 2.2），加上 AlipayClientCache 与 WxpayClientCache 的 Redis key 冲突（详见 2.3），导致支付配置更新后旧客户端继续使用。

**后果**：更换支付证书后仍使用旧证书，可能导致支付请求签名失败或使用过期密钥。

**建议**：修复 `autoClear()` 逻辑和 Redis key 冲突问题。

#### 🟡 脱敏规则更新丢失 — micro-mask

**位置**：[MaskCache.java](micro-mask/src/main/java/com/wkclz/micro/mask/cache/MaskCache.java)

**问题**：`CLEAR_FLAG` 竞态条件（详见 3.1）+ `CACHE_TIME`/`CACHE_ITEM` 非 volatile（详见 3.3），双重并发问题可能导致脱敏规则更新丢失。

**后果**：敏感数据（手机号、身份证号等）未脱敏直接返回给前端，违反数据隐私法规。

**建议**：使用 `AtomicBoolean` 替代 `Boolean`，添加 `volatile` 修饰符。

### 5.4 合规与法律风险

#### 🟠 个人信息保护合规 — 多模块

**问题**：
1. micro-wxapp/wxmp 收集用户 openid、手机号、昵称、头像等个人信息，未发现明确的隐私政策同意机制
2. micro-mask 的脱敏规则更新可能丢失（见 5.3），导致个人信息泄露
3. micro-audit 记录的数据变更日志包含完整实体快照，可能包含个人信息，未设置数据保留期限

**建议**：
1. 增加用户隐私政策同意记录
2. 审计日志设置数据保留期限（如 6 个月后自动清理）
3. 确保脱敏规则可靠性（修复 MaskCache 并发问题）

#### 🟡 开源组件许可证风险 — 全局

**问题**：项目使用 GraalVM Polyglot（可能受 Oracle 许可证限制）、iText5（AGPL 许可证，与商业使用冲突）、WeChat SDK 等第三方组件，需确认许可证兼容性。

**建议**：
1. 将 iText5 替换为 OpenPDF（LGPL/MPL，对商业友好）
2. 审查 GraalVM 社区版许可证是否允许当前使用场景
3. 建立开源组件许可证审查流程

### 5.5 代码质量与可维护性

#### 🟡 Mapper 目录命名不统一 — 全局

**问题**：部分模块用 `dao/`（micro-audit/mask/seq/pay/pdf/rmcheck/fun/wxmp），部分用 `mapper/`（micro-dict/fileos/form/material/msg/wxapp/k8s），两者功能完全相同但命名不一致，增加新人理解成本。

**建议**：统一为 `mapper/`（与 MyBatis 惯例一致），或至少在 AGENTS.md 中明确说明。

#### 🟡 MdmSequenceService.update 重复断言 — micro-seq

**位置**：[MdmSequenceService.java](micro-seq/src/main/java/com/wkclz/micro/seq/service/MdmSequenceService.java) 第41-42行

**问题**：`Assert.notNull(entity.getId())` 重复调用两次，应为第二次检查 `version`。

**建议**：修正为 `Assert.notNull(entity.getVersion(), ResultCode.UPDATE_NO_VERSION.getMessage())`。

#### 🟡 大量注释掉的旧代码 — micro-pay

**位置**：[WxpayHelper.java](micro-pay/src/main/java/com/wkclz/micro/pay/helper/WxpayHelper.java)

**问题**：包含 V2 版本的支付逻辑大量注释代码，增加代码维护负担和混淆风险。

**建议**：删除注释掉的旧代码，依赖 Git 历史追溯。

#### 🔵 缺乏单元测试 — 全局

**问题**：所有模块无独立测试目录，测试由主应用执行。关键业务逻辑（支付金额校验、SQL 注入防护、脱敏规则匹配）缺乏单元测试覆盖。

**建议**：至少为以下高风险逻辑添加单元测试：
- 支付金额校验
- SQL 注入/多语句检测
- 文件类型校验
- 脱敏规则匹配
- 序列号生成并发正确性

---

## 6. 修复优先级矩阵

| 优先级 | 风险ID | 模块 | 风险描述 | 建议修复时间 |
|--------|--------|------|----------|-------------|
| P0 | 4.1 | micro-fun | RCE：脚本引擎无沙箱 | 立即 |
| P0 | 4.2 | micro-dbview | SQL 注入：DDL 拼接 + 多语句检测绕过 | 立即 |
| P0 | 4.3 | micro-pay | JSON 注入：支付宝 bizContent 拼接 | 立即 |
| P0 | 4.4 | micro-pay | 支付回调金额未校验 | 立即 |
| P1 | 4.5 | micro-pay | openId 硬编码 | 1 周内 |
| P1 | 4.6 | micro-fileos | 文件类型校验全面放行 | 1 周内 |
| P1 | 4.7 | 多模块 | 敏感凭据明文内存缓存 | 1 周内 |
| P1 | 4.8 | micro-dbview | AES 密钥为空时明文存储 | 1 周内 |
| P1 | 3.1 | micro-mask | CLEAR_FLAG 竞态导致脱敏失效 | 1 周内 |
| P1 | 2.1 | micro-fun | 脚本缓存无失效机制 | 1 周内 |
| P2 | 4.9 | micro-dbview | 危险 SQL 检测绕过 | 2 周内 |
| P2 | 4.10 | micro-dbview | 多语句检测绕过 | 2 周内 |
| P2 | 4.11 | micro-dbview | JDBC URL 未校验 | 2 周内 |
| P2 | 2.2 | micro-pay | 支付缓存失效逻辑 | 2 周内 |
| P2 | 2.3 | micro-pay | Redis key 冲突 | 2 周内 |
| P2 | 1.1 | micro-dbview | SQL 无超时控制 | 2 周内 |
| P2 | 1.2 | micro-seq | size 无上限 DoS | 2 周内 |
| P3 | 其他 | 多模块 | 并发/性能/代码质量/合规 | 1 月内 |

---

**评估人**：AI 代码审计系统
**最后更新**：2026-06-02
