# 兜底模板（shrimp-gen 不可用时使用）

> 优先使用 shrimp-gen 生成器。本模板用于生成器不可用或需要手写的场景。
> 所有模板基于 micro-seq 模块抽象，使用占位符统一替换。

## 占位符约定

| 占位符 | 含义 | 示例 | 使用场景 |
|--------|------|------|----------|
| `XxxMod` | 模块缩写（PascalCase） | Seq | AutoConfig、Api 类名 |
| `XxxEnt` | 实体名（PascalCase） | Sequence | Entity、Mapper、Service、Rest、DTO 类名 |
| `xxxEnt` | 实体名（camelCase） | sequence | 方法名、URL 路径 |
| `XXX_ENT` | 实体名（UPPER_CASE） | SEQUENCE | Route 常量名 |
| `XxxCn` | 中文名 | 序列生成 | Swagger @Tag、@Operation 描述 |
| `xxx` | 模块名（camelCase） | seq | 包名 |
| `mdm_xxx` | 表名（蛇形） | mdm_sequence | 数据库表名 |
| `micro-xxx` | 模块目录名 | micro-seq | 模块目录、artifactId |

**注意：** 字段名（如 `xxxName`、`xxxCode`）中的 `xxx` 是业务字段前缀，用户根据实际业务替换，不使用上述占位符。

---

## 1. Entity 实体

**路径：** `src/main/java/com/wkclz/micro/xxx/bean/entity/MdmXxxEnt.java`
**参考样本：** `micro-seq/src/main/java/com/wkclz/micro/seq/bean/entity/MdmSequence.java`

```java
package com.wkclz.micro.xxx.bean.entity;

import com.wkclz.core.annotation.FieldDesc;
import com.wkclz.core.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_xxx (XxxCn 业务表) 重新生成代码会覆盖
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class MdmXxxEnt extends BaseEntity {

    /**
     * 名称
     */
    @FieldDesc("名称")
    private String xxxName;

    /**
     * 编码
     */
    @FieldDesc("编码")
    private String xxxCode;


    public static MdmXxxEnt copy(MdmXxxEnt source, MdmXxxEnt target) {
        if (target == null) { target = new MdmXxxEnt(); }
        if (source == null) { return target; }
        target.setId(source.getId());
        target.setXxxName(source.getXxxName());
        target.setXxxCode(source.getXxxCode());
        target.setSort(source.getSort());
        target.setCreateTime(source.getCreateTime());
        target.setCreateBy(source.getCreateBy());
        target.setUpdateTime(source.getUpdateTime());
        target.setUpdateBy(source.getUpdateBy());
        target.setRemark(source.getRemark());
        target.setVersion(source.getVersion());
        return target;
    }

    public static MdmXxxEnt copyIfNotNull(MdmXxxEnt source, MdmXxxEnt target) {
        if (target == null) { target = new MdmXxxEnt(); }
        if (source == null) { return target; }
        if (source.getId() != null) { target.setId(source.getId()); }
        if (source.getXxxName() != null) { target.setXxxName(source.getXxxName()); }
        if (source.getXxxCode() != null) { target.setXxxCode(source.getXxxCode()); }
        if (source.getSort() != null) { target.setSort(source.getSort()); }
        if (source.getCreateTime() != null) { target.setCreateTime(source.getCreateTime()); }
        if (source.getCreateBy() != null) { target.setCreateBy(source.getCreateBy()); }
        if (source.getUpdateTime() != null) { target.setUpdateTime(source.getUpdateTime()); }
        if (source.getUpdateBy() != null) { target.setUpdateBy(source.getUpdateBy()); }
        if (source.getRemark() != null) { target.setRemark(source.getRemark()); }
        if (source.getVersion() != null) { target.setVersion(source.getVersion()); }
        return target;
    }

}
```

**要点：**
- 继承 `BaseEntity`（自动获得 id/sort/createTime/createBy/updateTime/updateBy/remark/version/deleted）
- `@EqualsAndHashCode(callSuper = false)`——不调用父类 equals
- `copy()` 全量复制，`copyIfNotNull()` 增量复制——用于 update 场景
- 业务字段加 `@FieldDesc` 注解

---

## 2. Mapper 接口

**路径：** `src/main/java/com/wkclz/micro/xxx/mapper/MdmXxxEntMapper.java`
**参考样本：** `micro-seq/src/main/java/com/wkclz/micro/seq/mapper/MdmSequenceMapper.java`

```java
package com.wkclz.micro.xxx.mapper;

import com.wkclz.micro.xxx.bean.entity.MdmXxxEnt;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_xxx (XxxCn) Mapper 接口，代码重新生成不覆盖
 */
@Mapper
public interface MdmXxxEntMapper extends BaseMapper<MdmXxxEnt> {

    List<MdmXxxEnt> getXxxEntList(MdmXxxEnt entity);

}
```

**要点：**
- 继承 `BaseMapper<MdmXxxEnt>`——自动获得 14 个通用方法（insert/insertBatch/deleteById/updateById/selectById 等）
- 自定义查询方法在 XML 中实现
- `@Mapper` 注解必须

---

## 3. Mapper.xml

**路径：** `src/main/resources/mapper/MdmXxxEntMapper.xml`
**参考样本：** `micro-seq/src/main/resources/mapper/MdmSequenceMapper.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.wkclz.micro.xxx.mapper.MdmXxxEntMapper">

    <select id="getXxxEntList" parameterType="com.wkclz.micro.xxx.bean.entity.MdmXxxEnt" resultType="com.wkclz.micro.xxx.bean.entity.MdmXxxEnt">
        SELECT
            id,
            xxx_name,
            xxx_code,
            sort,
            create_time,
            create_by,
            update_time,
            update_by,
            remark,
            version
        FROM
            mdm_xxx
        WHERE
            deleted = 0
            <if test="xxxName != null">AND xxx_name LIKE CONCAT('%', #{xxxName}, '%')</if>
            <if test="xxxCode != null">AND xxx_code LIKE CONCAT('%', #{xxxCode}, '%')</if>
        ORDER BY
            id DESC
    </select>

</mapper>
```

**要点：**
- `namespace` 必须与 Mapper 接口全限定名一致
- 所有查询必须 `WHERE deleted = 0`（逻辑删除过滤）
- 查询字段需与 Entity 字段对齐（蛇形 ↔ 驼峰自动转换）
- 模糊查询用 `LIKE CONCAT('%', #{xxx}, '%')`

---

## 4. Service

**路径：** `src/main/java/com/wkclz/micro/xxx/service/MdmXxxEntService.java`
**参考样本：** `micro-seq/src/main/java/com/wkclz/micro/seq/service/MdmSequenceService.java`

```java
package com.wkclz.micro.xxx.service;

import com.wkclz.core.base.PageData;
import com.wkclz.core.enums.ResultCode;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.xxx.bean.entity.MdmXxxEnt;
import com.wkclz.micro.xxx.mapper.MdmXxxEntMapper;
import com.wkclz.mybatis.helper.PageQuery;
import com.wkclz.mybatis.service.BaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_xxx (XxxCn) 单表服务类，代码重新生成不覆盖
 */
@Service
public class MdmXxxEntService extends BaseService<MdmXxxEnt, MdmXxxEntMapper> {

    public PageData<MdmXxxEnt> getXxxEntPage(MdmXxxEnt entity) {
        return PageQuery.page(entity, mapper::getXxxEntList);
    }

    @Transactional(rollbackFor = Exception.class)
    public MdmXxxEnt create(MdmXxxEnt entity) {
        duplicateCheck(entity);
        mapper.insert(entity);
        return entity;
    }

    @Transactional(rollbackFor = Exception.class)
    public MdmXxxEnt update(MdmXxxEnt entity) {
        duplicateCheck(entity);
        MdmXxxEnt oldEntity = selectById(entity.getId());
        if (oldEntity == null) {
            throw ValidationException.of(ResultCode.RECORD_NOT_EXIST);
        }
        MdmXxxEnt.copyIfNotNull(entity, oldEntity);
        updateByIdSelective(oldEntity);
        return oldEntity;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        MdmXxxEnt entity = selectById(id);
        if (entity == null) {
            throw ValidationException.of(ResultCode.RECORD_NOT_EXIST);
        }
        deleteById(entity);
    }

    /**
     * 唯一性校验——根据业务调整唯一条件
     */
    private void duplicateCheck(MdmXxxEnt entity) {
        MdmXxxEnt param = new MdmXxxEnt();
        // 唯一条件（示例：xxxCode 唯一）
        param.setXxxCode(entity.getXxxCode());
        param = selectOneByEntity(param);
        if (param == null) {
            return;
        }
        if (param.getId().equals(entity.getId())) {
            return;
        }
        throw ValidationException.of(ResultCode.RECORD_DUPLICATE);
    }

}
```

**要点：**
- 继承 `BaseService<MdmXxxEnt, MdmXxxEntMapper>`——自动获得通用 CRUD
- `@Transactional` 放在 Service 层，`rollbackFor = Exception.class`
- `duplicateCheck` 中的唯一条件需根据业务调整
- `update` 使用 `copyIfNotNull` + `updateByIdSelective` 实现增量更新
- 业务异常用 `ValidationException.of()`

---

## 5. Rest 控制器

**路径：** `src/main/java/com/wkclz/micro/xxx/rest/XxxEntRest.java`
**参考样本：** `micro-seq/src/main/java/com/wkclz/micro/seq/rest/SequenceRest.java`

```java
package com.wkclz.micro.xxx.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.micro.xxx.bean.entity.MdmXxxEnt;
import com.wkclz.micro.xxx.bean.req.XxxEntCreateReq;
import com.wkclz.micro.xxx.bean.req.XxxEntInfoReq;
import com.wkclz.micro.xxx.bean.req.XxxEntPageReq;
import com.wkclz.micro.xxx.bean.req.XxxEntUpdateReq;
import com.wkclz.micro.xxx.bean.resp.XxxEntPageResp;
import com.wkclz.micro.xxx.bean.resp.XxxEntResp;
import com.wkclz.micro.xxx.service.MdmXxxEntService;
import com.wkclz.tool.utils.BeanUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_xxx (XxxCn) 示例rest 接口，代码重新生成会覆盖
 */
@Tag(name = "1.XxxCn", description = "XxxCn 管理接口")
@RestController
@RequestMapping(Route.PREFIX)
@Validated
public class XxxEntRest {

    @Autowired
    private MdmXxxEntService mdmXxxEntService;

    @Operation(summary = "1.XxxCn-分页查询", description = "根据条件分页查询 XxxCn 列表")
    @GetMapping(Route.XXX_ENT_PAGE)
    public R<PageData<XxxEntPageResp>> xxxEntPage(@Valid XxxEntPageReq req) {
        MdmXxxEnt entity = BeanUtil.cp(req, MdmXxxEnt.class);
        PageData<MdmXxxEnt> page = mdmXxxEntService.getXxxEntPage(entity);
        PageData<XxxEntPageResp> newPage = page.convert(XxxEntPageResp.class);
        return R.ok(newPage);
    }

    @Operation(summary = "2.XxxCn-详情", description = "根据ID查询 XxxCn 详情")
    @GetMapping(Route.XXX_ENT_INFO)
    public R<XxxEntResp> xxxEntInfo(@Valid XxxEntInfoReq req) {
        MdmXxxEnt entity = mdmXxxEntService.selectById(req.getId());
        if (entity == null) {
            return R.error("id is error");
        }
        XxxEntResp resp = BeanUtil.cp(entity, XxxEntResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "3.XxxCn-创建", description = "创建 XxxCn 记录")
    @PostMapping(Route.XXX_ENT_CREATE)
    public R<XxxEntResp> xxxEntCreate(@Valid @RequestBody XxxEntCreateReq req) {
        MdmXxxEnt entity = BeanUtil.cp(req, MdmXxxEnt.class);
        entity = mdmXxxEntService.create(entity);
        XxxEntResp resp = BeanUtil.cp(entity, XxxEntResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "4.XxxCn-修改", description = "修改 XxxCn 信息")
    @PostMapping(Route.XXX_ENT_UPDATE)
    public R<XxxEntResp> xxxEntUpdate(@Valid @RequestBody XxxEntUpdateReq req) {
        MdmXxxEnt entity = BeanUtil.cp(req, MdmXxxEnt.class);
        entity = mdmXxxEntService.update(entity);
        XxxEntResp resp = BeanUtil.cp(entity, XxxEntResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "5.XxxCn-删除", description = "根据ID删除 XxxCn 记录")
    @PostMapping(Route.XXX_ENT_REMOVE)
    public R<String> xxxEntRemove(@Valid XxxEntInfoReq req) {
        mdmXxxEntService.deleteById(req.getId());
        return R.ok("删除成功");
    }

}
```

**要点：**
- `@Tag` + `@Operation` 用于 Swagger 文档
- `@RequestMapping(Route.PREFIX)`——与 Route 常量一致
- 所有接口返回 `R<T>` 统一响应对象
- 分页返回 `PageData<T>`，使用 `page.convert(XxxEntPageResp.class)` 转换
- `BeanUtil.cp()` 用于 Req→Entity、Entity→Resp 转换
- `@Valid` + `@Validated` 启用参数校验

---

## 6. Req / Resp DTO

**路径：** `src/main/java/com/wkclz/micro/xxx/bean/req/` 与 `src/main/java/com/wkclz/micro/xxx/bean/resp/`
**参考样本：** `micro-seq/src/main/java/com/wkclz/micro/seq/bean/req/` 与 `resp/`

### XxxEntPageReq（分页查询入参）

```java
package com.wkclz.micro.xxx.bean.req;

import com.wkclz.web.bean.PageReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "XxxCn 分页查询请求")
public class XxxEntPageReq extends PageReq {

    @Schema(description = "名称【支持模糊查询】")
    private String xxxName;

    @Schema(description = "编码【支持模糊查询】")
    private String xxxCode;
}
```

### XxxEntInfoReq（详情入参）

```java
package com.wkclz.micro.xxx.bean.req;

import com.wkclz.web.bean.IdReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "XxxCn 详情查询请求")
public class XxxEntInfoReq extends IdReq {
}
```

### XxxEntCreateReq（创建入参）

```java
package com.wkclz.micro.xxx.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "XxxCn 创建请求")
public class XxxEntCreateReq implements Serializable {

    @NotBlank(message = "名称不能为空")
    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String xxxName;

    @NotBlank(message = "编码不能为空")
    @Schema(description = "编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String xxxCode;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "备注")
    private String remark;
}
```

### XxxEntUpdateReq（修改入参）

```java
package com.wkclz.micro.xxx.bean.req;

import com.wkclz.web.bean.UpdateReq;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "XxxCn 修改请求")
public class XxxEntUpdateReq extends UpdateReq {

    @NotBlank(message = "名称不能为空")
    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String xxxName;

    @NotBlank(message = "编码不能为空")
    @Schema(description = "编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String xxxCode;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "备注")
    private String remark;
}
```

### XxxEntResp（详情返回）

```java
package com.wkclz.micro.xxx.bean.resp;

import com.wkclz.web.bean.EntityResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "XxxCn 响应")
public class XxxEntResp extends EntityResp {

    @Schema(description = "名称")
    private String xxxName;

    @Schema(description = "编码")
    private String xxxCode;
}
```

### XxxEntPageResp（分页返回）

```java
package com.wkclz.micro.xxx.bean.resp;

import com.wkclz.web.bean.EntityResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "XxxCn 分页响应")
public class XxxEntPageResp extends EntityResp {

    @Schema(description = "名称")
    private String xxxName;

    @Schema(description = "编码")
    private String xxxCode;
}
```

**DTO 要点：**
- 分页入参继承 `PageReq`，详情入参继承 `IdReq`，修改入参继承 `UpdateReq`（含 id + version）
- 创建入参 `implements Serializable`（框架未提供 `CreateReq` 基类）
- 响应继承 `EntityResp`（自动获得 id/sort/createTime 等）
- 使用 `@Schema` 描述字段，`@NotBlank`/`@NotNull` 校验必填
- Req 和 Resp 分开，避免 Entity 直接暴露给前端

---

## 使用建议

1. 替换所有占位符（`XxxMod`/`XxxEnt`/`xxxEnt`/`XXX_ENT`/`XxxCn`/`xxx`/`mdm_xxx`/`micro-xxx`）后即可使用
2. `duplicateCheck` 中的唯一条件需根据业务调整（示例为 xxxCode 唯一）
3. Mapper.xml 的查询字段需与 Entity 字段对齐
4. 事务注解 `@Transactional` 放在 Service 层
5. 如需对外 API（供其他模块调用），在 `api/` 包下创建 `XxxModApi` 接口（参考 SeqApi、PdfApi）
