---
name: creating-micro-module
description: Use when 创建新的 micro-* 微模块——需要新建模块目录、注册父 pom、创建 AutoConfig/Route/imports 骨架文件、套用命名规范、验证 Spring 扫描与 Mapper 注入时触发。不适用于查询已有模块（用 micro-apps）。
---

# creating-micro-module 新建微模块脚手架

## 概述

新建 micro-* 微模块的标准流程与骨架模板。聚焦"shrimp-gen 生成器不产出的部分"——模块拼装、注册、命名约束、验证。

## 何时使用

- 新建 micro-xxx 模块
- 修改模块注册（pom.xml/AutoConfig/imports）异常排查
- 命名规范校验

不适用：
- 查询已有模块内部知识 → 用 micro-apps
- 生成 Entity/Mapper/Service/Rest → 优先 shrimp-gen

## 新建模块 7 步流程

### 步骤 1：父 pom 注册

在 `pom.xml` 的 `<modules>` 中添加新模块：

```xml
<modules>
    <!-- 已有模块 -->
    <module>micro-seq</module>
    <!-- 新增 -->
    <module>micro-xxx</module>
</modules>
```

### 步骤 2：模块 pom.xml

创建 `micro-xxx/pom.xml`，parent 指向 sh-microapp：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.wkclz.microapp</groupId>
        <artifactId>sh-microapp</artifactId>
        <version>${revision}</version>
        <relativePath>../pom.xml</relativePath>
    </parent>

    <artifactId>micro-xxx</artifactId>

    <properties>
        <maven.compiler.source>25</maven.compiler.source>
        <maven.compiler.target>25</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
    </dependencies>

</project>
```

如需启用 shrimp-gen 代码生成器，追加 `<build>` 插件：

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.wkclz.generator</groupId>
            <artifactId>generator-client</artifactId>
            <version>5.0.0-SNAPSHOT</version>
            <configuration>
                <options>
                    <option><!-- 生成器选项 ID --></option>
                </options>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### 步骤 3：自动配置 XxxModAutoConfig.java

创建 `src/main/java/com/wkclz/micro/xxx/XxxModAutoConfig.java`：

```java
package com.wkclz.micro.xxx;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan({"com.wkclz.micro.xxx.mapper"})
@ComponentScan(basePackages = {"com.wkclz.micro.xxx"})
public class XxxModAutoConfig {
}
```

**命名注意：** AutoConfig 类名是 `XxxModAutoConfig`（如 `SeqAutoConfig`），**不带** Micro 前缀。

### 步骤 4：AutoConfiguration.imports 注册

创建 `src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`：

```
com.wkclz.micro.xxx.XxxModAutoConfig
```

**路径必须精确**——`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`，文件无扩展名。

### 步骤 5：Route 路由常量

创建 `src/main/java/com/wkclz/micro/xxx/rest/Route.java`：

```java
package com.wkclz.micro.xxx.rest;

import com.wkclz.core.annotation.ApiDesc;
import com.wkclz.core.annotation.Router;

@Router(module = "micro-xxx", prefix = Route.PREFIX)
public interface Route {

    String PREFIX = "/micro-xxx";

    @ApiDesc("1. XxxCn-分页")
    String XXX_ENT_PAGE = "/xxxEnt/page";
    @ApiDesc("2. XxxCn-详情")
    String XXX_ENT_INFO = "/xxxEnt/info";
    @ApiDesc("3. XxxCn-创建")
    String XXX_ENT_CREATE = "/xxxEnt/create";
    @ApiDesc("4. XxxCn-修改")
    String XXX_ENT_UPDATE = "/xxxEnt/update";
    @ApiDesc("5. XxxCn-删除")
    String XXX_ENT_REMOVE = "/xxxEnt/remove";
}
```

### 步骤 6：CRUD 骨架（优先 shrimp-gen）

优先调用 shrimp-gen 生成器产出 Entity / Mapper / Service / Rest / Mapper.xml / Req / Resp。

生成器不可用时，读取 `references/templates.md` 套用兜底模板，使用占位符 `XxxMod`/`XxxEnt`/`xxxEnt`/`XXX_ENT`/`XxxCn`/`xxx`/`mdm_xxx`/`micro-xxx` 替换。

### 步骤 7：验证

执行下方"验证清单"，确认模块能被 Spring 扫描、Mapper 能注入、Route 可访问。

## 命名规范速查

| 维度 | 规则 | 示例 |
|------|------|------|
| 模块目录名 | `micro-xxx` | micro-seq |
| 包名根 | `com.wkclz.micro.xxx` | com.wkclz.micro.seq |
| 表名 | `mdm_xxx` | mdm_sequence |
| Entity | `MdmXxxEnt`（Mdm 前缀） | MdmSequence |
| AutoConfig | `XxxModAutoConfig`（不带 Micro） | SeqAutoConfig |
| Rest | `XxxEntRest`（不带 Mdm） | SequenceRest |
| API 前缀 | `/micro-xxx` | /micro-seq |

详细规则见 `references/naming-conventions.md`。

## 验证清单

- [ ] 父 pom.xml 已添加 `<module>micro-xxx</module>`
- [ ] `AutoConfiguration.imports` 文件已创建且路径精确
- [ ] `@ComponentScan` 包路径为 `com.wkclz.micro.xxx`
- [ ] `@MapperScan` 包路径为 `com.wkclz.micro.xxx.mapper`
- [ ] `Route.PREFIX` 与模块目录名一致（`/micro-xxx`）
- [ ] 启动日志无 "No MyBatis mapper was found" 警告
- [ ] Swagger 文档可见新模块端点

## 常见错误

| 症状 | 原因 | 修复 |
|------|------|------|
| 模块未被 Spring 扫描 | imports 文件缺失或 @ComponentScan 路径错 | 检查 imports 文件路径与包名 |
| Mapper 无法注入 | @MapperScan 路径未包含 Mapper 包 | 校正 @MapperScan basePackages 为 `com.wkclz.micro.xxx.mapper` |
| Route 404 | PREFIX 与 @RequestMapping 不一致 | 校正 Route.PREFIX 与 @RequestMapping 一致 |
| 依赖版本冲突 | 子 pom 指定了版本号 | 移除版本号，由 sh-bom 管理 |
| AutoConfig 类名错误 | 加了 Micro 前缀 | 改为 `XxxModAutoConfig`（如 `SeqAutoConfig`） |
| Entity 与 Rest 命名混淆 | Rest 加了 Mdm 前缀 | Rest 不带 Mdm 前缀（`SequenceRest` 而非 `MdmSequenceRest`） |
