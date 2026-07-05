# sh-microapp 项目开发指南

本文档帮助开发者快速理解 `sh-microapp` 项目的整体架构、模块职责和开发规范。

## 📦 项目概述

`sh-microapp` 是微应用集合项目，每个模块承接相对独立的业务能力，被主应用依赖后使用。基于 Spring Boot 4.x + Java 25 构建，统一依赖 sh-framework 框架。

| 属性 | 值 |
|------|------|
| GroupId | `com.wkclz.microapp` |
| 版本 | `5.0.0-SNAPSHOT` |
| 父 POM | `com.wkclz.framework:sh-parent:5.0.0-SNAPSHOT` |
| Java | 25 |
| Spring Boot | 4.0.6 |
| ORM | MyBatis 4.0.1 + PageHelper |
| 缓存 | Redis (Lettuce) |

---

## 🏗️ 架构设计

```
┌─────────────────────────────────────────────────────────────┐
│                      主应用 (业务系统)                        │
│              引入所需 micro-* 模块依赖                         │
└─────────────────────────────────────────────────────────────┘
                              ↓ 依赖
┌─────────────────────────────────────────────────────────────┐
│                    sh-microapp (微应用集合)                    │
│                                                              │
│  micro-dict    micro-file    micro-form    micro-msg         │
│  micro-audit   micro-pdf     micro-mask    micro-pay         │
│  micro-seq     micro-fun     micro-k8s     micro-liteflow    │
│  micro-rmcheck micro-material micro-wxapp  micro-wxmp        │
│  micro-autotest micro-report                                 │
└─────────────────────────────────────────────────────────────┘
                              ↓ 依赖
┌─────────────────────────────────────────────────────────────┐
│                    sh-framework (基础框架)                     │
│                                                              │
│  sh-bom    sh-core    sh-tool    sh-mybatis    sh-web        │
│  sh-redis  sh-spring  sh-mqtt    sh-dynamicdb  sh-xxljob     │
└─────────────────────────────────────────────────────────────┘
```

---

## 📁 模块清单

### 数据管理类

| 模块 | 说明 | API 前缀 | 核心能力 |
|------|------|----------|----------|
| `micro-dict` | 数据字典 | `/micro-dict` | 字典类型/字典项 CRUD、跨环境 Copy/Paste、Redis Pub/Sub 缓存 |
| `micro-seq` | 序列号生成 | `/micro-seq` | 按 dictType 生成业务序列号 |
| `micro-material` | 物料管理 | `/micro-material` | 物料/物料组/物料版本/物料关联 CRUD、物料选择器 |
| `micro-form` | 表单规则 | `/micro-form` | 表单定义/规则/字段校验器管理、AOP 规则拦截 |

### 文件与文档类

| 模块 | 说明 | API 前缀 | 核心能力 |
|------|------|----------|----------|
| `micro-file` | 文件管理 | `/micro-file` | 多 OSS 提供商(阿里云/S3/MinIO)、签名链接、Magic Bytes 校验 |
| `micro-pdf` | PDF 生成 | `/micro-pdf` | PDF 模板管理、基于模板生成 PDF |

### 消息与通知类

| 模块 | 说明 | API 前缀 | 核心能力 |
|------|------|----------|----------|
| `micro-msg` | 消息通知 | `/micro-msg` | 消息模板/通知/用户记录/用户设置管理 |
| `micro-mask` | 数据脱敏 | `/micro-mask` | 脱敏规则管理、响应自动脱敏(ResponseAdvice) |

### 审计与校验类

| 模块 | 说明 | API 前缀 | 核心能力 |
|------|------|----------|----------|
| `micro-audit` | 变更审计 | `/micro-audit` | 数据变更日志记录、字段差异对比 |
| `micro-rmcheck` | 合规校验 | `/micro-rmcheck` | 校验规则/规则项管理、合规检查 API |

### 支付类

| 模块 | 说明 | API 前缀 | 核心能力 |
|------|------|----------|----------|
| `micro-pay` | 支付集成 | `/micro-pay` | 微信支付/支付宝配置与订单管理、PayOrderSpi（支付-订单交互 SPI，供订单模块实现，提供订单信息查询与状态更新）、积分支付集成（下单时积分消费 consume、支付失败补偿 releaseConsume）、退款编排（总单/子单两个退款入口，总单退款经 releaseConsume 释放冻结与回退已扣积分，子单退款通过 SPI 获取子单 points 并调 pointsRefundService.refund 退还；退款积分是否退还受 `pay.points-refund-on-refund.enable` 开关控制，默认启用） |

### 积分类

| 模块 | 说明 | API 前缀 | 核心能力 |
|------|------|----------|----------|
| `micro-points` | 积分账户 | `/micro-points` | 积分钱包/发放/试算/消费(冻结→异步扣减)/回退/过期/对账 |

### 微信类

| 模块 | 说明 | API 前缀 | 核心能力 |
|------|------|----------|----------|
| `micro-wxapp` | 微信小程序 | `/micro-wxapp` | 小程序登录/用户管理/配置/媒体上传 |
| `micro-wxmp` | 微信公众号 | `/micro-wxmp` | 公众号消息处理/事件处理/客服消息/用户管理 |

### 基础设施类

| 模块 | 说明 | API 前缀 | 核心能力 |
|------|------|----------|----------|
| `micro-liteflow` | 规则引擎 | `/micro-liteflow` | LiteFlow 链/脚本管理、规则编排 |
| `micro-fun` | 函数管理 | `/micro-fun` | 函数分类/函数定义、JS 脚本引擎执行 |
| `micro-k8s` | K8s 管理 | `/micro-k8s` | Kubernetes 集群配置/资源查询/自定义 API |
| `micro-autotest` | 自动化测试 | `/micro-autotest` | REST 接口扫描/自动 Mock/测试执行/报告生成(MD+HTML) |
| `micro-report` | 报表管理 | `/micro-report` | SQL 报表定义/参数与结果字段管理/动态查询执行/Excel 导出 |

---

## 🔧 模块开发规范

### 标准目录结构

每个 micro-* 模块遵循统一的目录结构：

```
micro-xxx/
├── pom.xml
├── README.md                          # 可选
├── AGENTS.md                          # 模块开发指南
└── src/main/
    ├── java/com/wkclz/micro/xxx/
    │   ├── XxxAutoConfig.java         # 自动配置（@ComponentScan + @MapperScan）
    │   ├── api/                       # 对外 API 接口（供其他模块调用）
    │   ├── bean/
    │   │   ├── entity/                # 数据库实体（extends BaseEntity）
    │   │   ├── dto/                   # 数据传输对象
    │   │   └── enums/                 # 枚举
    │   ├── cache/                     # 缓存（Redis Pub/Sub）
    │   ├── config/                    # 配置类
    │   ├── mapper/            # MyBatis Mapper 接口
    │   ├── helper/                    # 辅助工具类
    │   ├── rest/                      # REST 控制器
    │   │   ├── Route.java             # 路由常量（@Router 注解）
    │   │   └── *Rest.java             # REST 控制器
    │   ├── service/                   # 业务服务（extends BaseService）
    │   └── utils/                     # 工具类
    └── resources/
        ├── META-INF/spring/
        │   └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
        └── mapper/                    # MyBatis XML 映射文件
```

### 自动配置类命名

```java
@Configuration
@ComponentScan(basePackages = {"com.wkclz.micro.xxx"})
@MapperScan(basePackages = {"com.wkclz.micro.xxx.mapper"})
public class XxxAutoConfig {
}
```

注册文件 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`：
```
com.wkclz.micro.xxx.XxxAutoConfig
```

### Route 路由常量

```java
@Router(module = "micro-xxx", prefix = Route.PREFIX)
public interface Route {
    String PREFIX = "/micro-xxx";

    @ApiDesc("1. 分页查询")
    String PAGE = "/page";
    // ...
}
```

### 实体定义

```java
@Data
@EqualsAndHashCode(callSuper = true)
public class MdmXxx extends BaseEntity {
    private String xxxCode;
    private String xxxName;
}
```

### Mapper 定义

```java
@Mapper
public interface MdmXxxMapper extends BaseMapper<MdmXxx> {
    // 自定义方法在 XML 中实现
}
```

### Service 定义

```java
@Service
public class MdmXxxService extends BaseService<MdmXxx, MdmXxxMapper> {
    // 继承通用 CRUD: insert, insertBatch, deleteById, updateById,
    // selectById, selectAll, selectByEntity, selectPage 等
}
```

### REST 控制器

```java
@RestController
@RequestMapping(Route.PREFIX)
public class XxxRest {

    @Autowired
    private MdmXxxService service;

    @GetMapping(Route.PAGE)
    public R<PageData<MdmXxx>> page(MdmXxx entity) {
        PageData<MdmXxx> page = service.selectPage(entity);
        return R.ok(page);
    }
}
```

---

## 🔗 依赖关系

### 框架依赖

| 框架模块 | 说明 | 典型使用 |
|----------|------|----------|
| `sh-bom` | BOM 版本管理 | 统一第三方依赖版本 |
| `sh-core` | 核心基础 | BaseEntity、R、CommonException、UserContext |
| `sh-tool` | 工具集 | 加密、字符串、日期、Bean 操作 |
| `sh-mybatis` | ORM | BaseMapper(14 方法)、BaseService、PageQuery |
| `sh-web` | Web 扩展 | ErrorHandler、RestHelper、IpHelper |
| `sh-redis` | Redis | RedisHelper、RedisLock、RedisMessageQueue |
| `sh-spring` | Spring 扩展 | SpringContextHolder、SnowflakeHelper、MailUtil |
| `sh-mqtt` | MQTT | @MqttController、MqttProducer |
| `sh-dynamicdb` | 动态数据源 | 运行时切换数据源 |
| `sh-xxljob` | 定时任务 | @XxlJob 注解开发 |

### 模块间依赖

```
micro-dict ← micro-form (表单字典选项)
micro-dict ← micro-mask (脱敏字典)
micro-dict ← micro-seq  (序列号字典类型)
micro-dict ← micro-report (报表参数字典选项，仅字段引用，无硬依赖)
micro-file ← micro-pdf  (PDF 文件存储)
micro-file ← micro-wxapp(小程序媒体上传)
micro-points ← micro-pay (积分支付/退款积分回退)
```

---

## 🛠️ 开发流程

### 新建模块

1. 创建 `micro-xxx` 目录，编写 `pom.xml`（parent 指向 sh-microapp）
2. 创建 `XxxAutoConfig.java` 自动配置类
3. 创建 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
4. 在父 `pom.xml` 的 `<modules>` 中添加 `<module>micro-xxx</module>`
5. 按标准目录结构创建 entity / mapper / service / rest
6. 创建 `Route.java` 定义路由常量
7. 编写 `AGENTS.md` 模块开发指南
8. 在 `.agents/skills/` 下创建 SKILL.md

### 引用模块

```xml
<dependency>
    <groupId>com.wkclz.microapp</groupId>
    <artifactId>micro-xxx</artifactId>
</dependency>
```

---

## 📊 数据库规范

### 命名

- 表名：蛇形命名法，如 `mdm_dict`、`mdm_dict_item`
- 字段名：蛇形命名法，Java 实体自动转驼峰
- 主键：`id` (bigint AUTO_INCREMENT)
- 编码字段：`xxx_code` (唯一索引)

### 基础字段（DbColumnEntity）

| 数据库字段 | Java 字段 | 类型 | 说明 |
|-----------|----------|------|------|
| `id` | `id` | bigint | 主键 |
| `sort` | `sort` | int | 排序 |
| `create_time` | `createTime` | datetime | 创建时间 |
| `create_by` | `createBy` | varchar(31) | 创建人 |
| `update_time` | `updateTime` | datetime | 修改时间 |
| `update_by` | `updateBy` | varchar(31) | 修改人 |
| `remark` | `remark` | varchar(255) | 备注 |
| `version` | `version` | int | 乐观锁 |
| `deleted` | — | varchar(24) | 逻辑删除(0=未删除) |

### 扩展字段（BaseEntity）

| 数据库字段 | Java 字段 | 说明 |
|-----------|----------|------|
| `xxx_code` | `xxxCode` | 业务编码 |
| `tenant_code` | `tenantCode` | 租户编码 |

---

## ⚠️ 注意事项

1. **版本统一管理**：所有第三方依赖版本由 `sh-bom` 管理，子模块 pom.xml 不指定版本号
2. **逻辑删除**：所有表使用 `deleted` 字段，删除时写入时间戳，BaseMapper 自动过滤
3. **乐观锁**：更新时必须传 `version`，BaseMapper 自动 `AND version=#{version}`
4. **自动填充**：`MyBatisUpdateInterceptor` 自动填充 `createBy`/`updateBy`，清空时间字段由数据库默认值填充
5. **缓存一致性**：使用 Redis Pub/Sub 广播缓存刷新，3 秒防抖
6. **异常处理**：统一使用 `ValidationException.of("消息")` 或其他 CommonException 子类
7. **响应封装**：所有 REST 接口返回 `R<T>` 统一响应对象
8. **事务注解**：`@Transactional` 放在 Service 层

---

## 🆘 常见问题

| 问题 | 解决 |
|------|------|
| 模块未被 Spring 扫描到 | 检查 AutoConfiguration.imports 文件和 @ComponentScan 包路径 |
| Mapper 无法注入 | 检查 @MapperScan 包路径是否包含 Mapper 接口所在包 |
| 依赖版本冲突 | 检查 sh-bom 是否统一管理，不要在子模块指定版本号 |
| 缓存不一致 | 确认 Redis Pub/Sub 频道正常通信 |
| 乐观锁更新失败 | 前端必须回传 version 字段 |

---

## 编码规则

> 以下规则为 harness 工程强制规范，AI 编码时必须遵循：

1. **禁止调用系统资源**：仅能使用当前目录下的代码资源，不得调用系统级命令或外部系统资源
2. **保留已有注释**：不要移除已添加的注释，除非相关代码块已变动
3. **关键位置加日志**：实现业务逻辑时，在关键位置添加 log 日志打印（方法入口、分支判断、异常捕获、外部调用）
4. **更新文档**：任务完成后，必须更新本文件（AGENTS.md）以及相关的故事文件
5. **Req/Resp 封装**：所有请求参数封装 Req 对象（除非参数只有一个值），所有返回内容封装 Resp 对象（除非返回只有一个值）

## 质量门禁
- lint: `mvn checkstyle:check`
- test: `mvn test`
- build: `mvn package -DskipTests`
- typecheck: `mvn compile`

## 代码规范
详见 [docs/coding-standards/java.md](docs/coding-standards/java.md)

## 研发规范
- [研发过程规范](docs/dev-process.md)
- [需求拆解模板](docs/requirement-template.md)
- [技术活文档](docs/living-docs-technical/)
- [业务活文档](docs/living-docs-business/)
- [开发规范](docs/standards/)
- [Harness 规范总纲](docs/harness-spec.md)

## Stories
见 [docs/stories/](docs/stories/) 目录，按业务域分组

## 变更记录
见 [changes/](changes/) 目录

### 2026-07-05 Harness 升级
- 新增 `changes/` 变更目录及 README
- 新增 `.editorconfig` 统一编辑器格式
- 新增 `docs/tech-debts/` 下 5 个分类子目录（性能风险、内存隐患、线程与并发风险、安全风险、其他潜在风险）
- 同步 `docs/harness-spec.md`、`docs/dev-process.md`、`docs/standards/harness.md` 与 sh-harness 最新版
- 增强 `.gitignore` 合并 harness 标准 Java 忽略规则

## 项目上下文
见 [CONTEXT.md](CONTEXT.md)

---

**最后更新时间**: 2026-06-28
