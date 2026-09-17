package com.wkclz.micro.flowable.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.flowable.bean.entity.FlowableCategory;
import com.wkclz.micro.flowable.bean.entity.FlowableProcessDesign;
import com.wkclz.micro.flowable.bean.req.CategoryCreateReq;
import com.wkclz.micro.flowable.bean.req.CategoryPageReq;
import com.wkclz.micro.flowable.bean.req.CategoryUpdateReq;
import com.wkclz.micro.flowable.bean.resp.CategoryResp;
import com.wkclz.micro.flowable.service.FlowableCategoryService;
import com.wkclz.micro.flowable.service.FlowableProcessDesignService;
import com.wkclz.tool.utils.BeanUtil;
import com.wkclz.web.bean.RemoveReq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "8.管理端-流程分类", description = "流程分类管理接口")
@RestController
@RequestMapping(Route.PREFIX)
@Validated
public class FlowableCategoryRest {

    private static final Logger log = LoggerFactory.getLogger(FlowableCategoryRest.class);

    @Autowired
    private FlowableCategoryService categoryService;
    @Autowired
    private FlowableProcessDesignService designService;

    @Operation(summary = "1.分类-分页查询")
    @GetMapping(Route.ADMIN_CATEGORY_PAGE)
    public R<PageData<CategoryResp>> page(@Valid CategoryPageReq req) {
        log.info("分页查询流程分类: categoryCode={}, categoryName={}, status={}", req.getCategoryCode(), req.getCategoryName(), req.getStatus());
        FlowableCategory entity = BeanUtil.cp(req, FlowableCategory.class);
        PageData<FlowableCategory> page = categoryService.getCategoryPage(entity);
        return R.ok(page.convert(CategoryResp.class));
    }

    @Operation(summary = "2.分类-下拉列表", description = "仅返回启用状态分类，按 sort 升序")
    @GetMapping(Route.ADMIN_CATEGORY_LIST)
    public R<List<CategoryResp>> list() {
        log.info("查询流程分类下拉列表");
        FlowableCategory param = new FlowableCategory();
        param.setStatus(1);
        List<FlowableCategory> list = categoryService.getCategoryList(param);
        return R.ok(BeanUtil.cp(list, CategoryResp.class));
    }

    @Operation(summary = "3.分类-创建")
    @PostMapping(Route.ADMIN_CATEGORY_CREATE)
    public R<CategoryResp> create(@Valid @RequestBody CategoryCreateReq req) {
        log.info("创建流程分类: categoryCode={}, categoryName={}", req.getCategoryCode(), req.getCategoryName());
        FlowableCategory query = new FlowableCategory();
        query.setCategoryCode(req.getCategoryCode());
        if (!categoryService.selectByEntity(query).isEmpty()) {
            throw ValidationException.of("分类编码已存在: " + req.getCategoryCode());
        }
        FlowableCategory category = BeanUtil.cp(req, FlowableCategory.class);
        category.setStatus(1);
        categoryService.insert(category);
        log.info("创建流程分类成功: id={}", category.getId());
        return R.ok(BeanUtil.cp(category, CategoryResp.class));
    }

    @Operation(summary = "4.分类-修改", description = "分类编码不可修改")
    @PostMapping(Route.ADMIN_CATEGORY_UPDATE)
    public R<Integer> update(@Valid @RequestBody CategoryUpdateReq req) {
        log.info("修改流程分类: id={}", req.getId());
        FlowableCategory category = categoryService.selectById(req.getId());
        if (category == null) {
            throw ValidationException.of("分类不存在");
        }
        FlowableCategory update = new FlowableCategory();
        update.setId(req.getId());
        update.setCategoryName(req.getCategoryName());
        update.setStatus(req.getStatus());
        update.setSort(req.getSort());
        update.setRemark(req.getRemark());
        update.setVersion(req.getVersion());
        return R.ok(categoryService.updateByIdSelective(update));
    }

    @Operation(summary = "5.分类-删除", description = "被流程设计引用的分类不允许删除")
    @PostMapping(Route.ADMIN_CATEGORY_REMOVE)
    public R<Integer> remove(@Valid @RequestBody RemoveReq req) {
        log.info("删除流程分类: id={}", req.getId());
        FlowableCategory category = categoryService.selectById(req.getId());
        if (category == null) {
            throw ValidationException.of("分类不存在");
        }
        FlowableProcessDesign ref = new FlowableProcessDesign();
        ref.setCategory(category.getCategoryCode());
        if (!designService.selectByEntity(ref).isEmpty()) {
            throw ValidationException.of("分类已被流程设计引用，无法删除");
        }
        FlowableCategory del = new FlowableCategory();
        del.setId(req.getId());
        return R.ok(categoryService.deleteById(del));
    }
}
