package com.wkclz.micro.fun.rest;

import com.wkclz.core.base.R;
import com.wkclz.micro.fun.bean.dto.FunCategoryDto;
import com.wkclz.micro.fun.bean.entity.FunCategory;
import com.wkclz.micro.fun.bean.req.FunCategoryCreateReq;
import com.wkclz.micro.fun.bean.req.FunCategoryInfoReq;
import com.wkclz.micro.fun.bean.req.FunCategoryListReq;
import com.wkclz.micro.fun.bean.req.FunCategoryUpdateReq;
import com.wkclz.micro.fun.bean.resp.FunCategoryResp;
import com.wkclz.micro.fun.bean.resp.FunCategoryTreeResp;
import com.wkclz.micro.fun.service.FunCategoryService;
import com.wkclz.tool.utils.BeanUtil;
import com.wkclz.web.bean.RemoveReq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table fun_category (函数-分类) 示例rest 接口，代码重新生成会覆盖
 */
@Slf4j
@Tag(name = "1.函数分类", description = "函数分类管理接口")
@Validated
@RestController
@RequestMapping(Route.PREFIX)
public class FunCategoryRest {

    @Autowired
    private FunCategoryService funCategoryService;

    @Operation(summary = "1.函数分类-列表", description = "查询函数分类列表")
    @GetMapping(Route.FUN_CATEGORY_LIST)
    public R<List<FunCategoryResp>> funCategoryList(@Valid FunCategoryListReq req) {
        log.info("函数分类-列表查询, req: {}", req);
        FunCategory entity = BeanUtil.cp(req, FunCategory.class);
        List<FunCategory> list = funCategoryService.getFunCategoryList(entity);
        List<FunCategoryResp> respList = BeanUtil.cp(list, FunCategoryResp.class);
        return R.ok(respList);
    }

    @Operation(summary = "2.函数分类-树", description = "查询函数分类树形结构")
    @GetMapping(Route.FUN_CATEGORY_TREE)
    public R<List<FunCategoryTreeResp>> funCategoryTree(@Valid FunCategoryListReq req) {
        log.info("函数分类-树查询, req: {}", req);
        FunCategory entity = BeanUtil.cp(req, FunCategory.class);
        List<FunCategoryDto> list = funCategoryService.getFunCategoryTree(entity);
        List<FunCategoryTreeResp> respList = BeanUtil.cp(list, FunCategoryTreeResp.class);
        return R.ok(respList);
    }

    @Operation(summary = "3.函数分类-详情", description = "根据ID查询函数分类详情")
    @GetMapping(Route.FUN_CATEGORY_INFO)
    public R<FunCategoryResp> funCategoryInfo(@Valid FunCategoryInfoReq req) {
        log.info("函数分类-详情查询, id: {}", req.getId());
        FunCategory entity = funCategoryService.selectById(req.getId());
        if (entity == null) {
            return R.error("id is error");
        }
        FunCategoryResp resp = BeanUtil.cp(entity, FunCategoryResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "4.函数分类-创建", description = "新增函数分类")
    @PostMapping(Route.FUN_CATEGORY_CREATE)
    public R<FunCategoryResp> funCategoryCreate(@Valid @RequestBody FunCategoryCreateReq req) {
        log.info("函数分类-创建, req: {}", req);
        FunCategory entity = BeanUtil.cp(req, FunCategory.class);
        if (entity.getSort() == null) {
            entity.setSort(99);
        }
        if (entity.getVisible() == null) {
            entity.setVisible(1);
        }
        entity = funCategoryService.create(entity);
        FunCategoryResp resp = BeanUtil.cp(entity, FunCategoryResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "5.函数分类-修改", description = "修改函数分类")
    @PostMapping(Route.FUN_CATEGORY_UPDATE)
    public R<FunCategoryResp> funCategoryUpdate(@Valid @RequestBody FunCategoryUpdateReq req) {
        log.info("函数分类-修改, req: {}", req);
        FunCategory entity = BeanUtil.cp(req, FunCategory.class);
        if (entity.getSort() == null) {
            entity.setSort(99);
        }
        if (entity.getVisible() == null) {
            entity.setVisible(1);
        }
        entity = funCategoryService.update(entity);
        FunCategoryResp resp = BeanUtil.cp(entity, FunCategoryResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "6.函数分类-删除", description = "删除函数分类")
    @PostMapping(Route.FUN_CATEGORY_REMOVE)
    public R<Integer> funCategoryRemove(@Valid @RequestBody RemoveReq req) {
        log.info("函数分类-删除, id: {}", req.getId());
        FunCategory entity = new FunCategory();
        entity.setId(req.getId());
        Integer rt = funCategoryService.customDelete(entity);
        return R.ok(rt);
    }

    @Operation(summary = "7.函数分类-选项", description = "获取函数分类下拉选项")
    @GetMapping(Route.FUN_CATEGORY_OPTIONS)
    public R<List<FunCategoryTreeResp>> funCategoryOptions() {
        log.info("函数分类-选项查询");
        FunCategory funCategory = new FunCategory();
        funCategory.setVisible(1);
        List<FunCategoryDto> tree = funCategoryService.getFunCategoryOptions(funCategory);
        List<FunCategoryTreeResp> respList = BeanUtil.cp(tree, FunCategoryTreeResp.class);
        return R.ok(respList);
    }

}
