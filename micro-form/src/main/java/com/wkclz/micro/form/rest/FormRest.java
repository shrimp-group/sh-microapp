package com.wkclz.micro.form.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.micro.form.cache.FormCache;
import com.wkclz.micro.form.bean.dto.MdmFormDto;
import com.wkclz.micro.form.bean.entity.MdmForm;
import com.wkclz.micro.form.bean.entity.MdmFormItem;
import com.wkclz.micro.form.bean.req.MdmFormCreateReq;
import com.wkclz.micro.form.bean.req.MdmFormInfoReq;
import com.wkclz.micro.form.bean.req.MdmFormPageReq;
import com.wkclz.micro.form.bean.req.MdmFormUpdateReq;
import com.wkclz.micro.form.bean.resp.MdmFormPageResp;
import com.wkclz.micro.form.bean.resp.MdmFormResp;
import com.wkclz.micro.form.service.FormTableInfoService;
import com.wkclz.micro.form.service.MdmFormService;
import com.wkclz.mybatis.bean.ColumnQuery;
import com.wkclz.tool.utils.BeanUtil;
import com.wkclz.web.bean.RemoveReq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "1.表单管理", description = "表单管理接口")
@Validated
@RestController
@RequestMapping(Route.PREFIX)
public class FormRest {

    @Autowired
    private FormCache formCache;
    @Autowired
    private MdmFormService mdmFormService;
    @Autowired
    private FormTableInfoService formTableInfoService;

    @Operation(summary = "1.表单-分页查询", description = "根据条件分页查询表单列表")
    @GetMapping(Route.FORM_PAGE)
    public R<PageData<MdmFormPageResp>> mdmFormPage(@Valid MdmFormPageReq req) {
        MdmFormDto dto = BeanUtil.cp(req, MdmFormDto.class);
        PageData<MdmFormDto> page = mdmFormService.getFormPage(dto);
        PageData<MdmFormPageResp> newPage = page.convert(MdmFormPageResp.class);
        return R.ok(newPage);
    }

    @Operation(summary = "2.表单-详情", description = "根据ID查询表单详情")
    @GetMapping(Route.FORM_INFO)
    public R<MdmFormResp> mdmFormInfo(@Valid MdmFormInfoReq req) {
        MdmForm entity = new MdmForm();
        entity.setId(req.getId());
        MdmFormDto dto = mdmFormService.getFormInfo(entity);
        MdmFormResp resp = BeanUtil.cp(dto, MdmFormResp.class);
        if (dto.getItems() != null) {
            List<MdmFormResp.MdmFormItemResp> itemResps = BeanUtil.cp(dto.getItems(), MdmFormResp.MdmFormItemResp.class);
            resp.setItems(itemResps);
        }
        return R.ok(resp);
    }

    @Operation(summary = "3.表单-创建", description = "新增表单")
    @PostMapping(Route.FORM_CREATE)
    public R<MdmFormResp> mdmFormCreate(@RequestBody MdmFormCreateReq req) {
        MdmFormDto dto = BeanUtil.cp(req, MdmFormDto.class);
        parmCheck(dto);
        MdmForm entity = mdmFormService.create(dto);
        formCache.clearCache();
        MdmFormResp resp = BeanUtil.cp(entity, MdmFormResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "4.表单-修改", description = "修改表单")
    @PostMapping(Route.FORM_UPDATE)
    public R<MdmFormResp> mdmFormUpdate(@RequestBody MdmFormUpdateReq req) {
        MdmFormDto dto = BeanUtil.cp(req, MdmFormDto.class);
        dto.setFormCode(null);
        parmCheck(dto);
        MdmForm entity = mdmFormService.update(dto);
        formCache.clearCache();
        MdmFormResp resp = BeanUtil.cp(entity, MdmFormResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "5.表单-删除", description = "删除表单")
    @PostMapping(Route.FORM_REMOVE)
    public R<Integer> mdmFormRemove(@Valid @RequestBody RemoveReq req) {
        MdmForm entity = new MdmForm();
        entity.setId(req.getId());
        mdmFormService.customRemove(entity);
        formCache.clearCache();
        return R.ok(1);
    }

    @Operation(summary = "7.表单-数据库字段", description = "获取表单输入项数据库字段信息")
    @GetMapping(Route.FORM_DB_COLUMNS)
    public R<List<ColumnQuery>> formDbColumns() {
        List<ColumnQuery> infos = formTableInfoService.getColumnInfos();
        return R.ok(infos);
    }

    private static void parmCheck(MdmFormDto dto) {
        if (dto == null) {
            return;
        }
        List<MdmFormItem> items = dto.getItems();
        if (CollectionUtils.isEmpty(items)) {
            return;
        }
        for (MdmFormItem item : items) {
            item.setFormCode(dto.getFormCode());
            if (item.getClearable() == null) {
                item.setClearable(1);
            }
            if (item.getSort() == null) {
                item.setSort(99);
            }
        }
    }
}
