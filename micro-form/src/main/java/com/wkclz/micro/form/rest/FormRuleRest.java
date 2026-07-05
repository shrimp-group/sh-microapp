package com.wkclz.micro.form.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.micro.form.cache.FormRuleCache;
import com.wkclz.micro.form.bean.dto.MdmFormRuleDto;
import com.wkclz.micro.form.bean.entity.MdmFormRule;
import com.wkclz.micro.form.bean.req.MdmFormRuleCreateReq;
import com.wkclz.micro.form.bean.req.MdmFormRuleInfoReq;
import com.wkclz.micro.form.bean.req.MdmFormRulePageReq;
import com.wkclz.micro.form.bean.req.MdmFormRuleUpdateReq;
import com.wkclz.micro.form.bean.resp.MdmFormRuleResp;
import com.wkclz.micro.form.service.MdmFormRuleService;
import com.wkclz.tool.utils.BeanUtil;
import com.wkclz.web.bean.RemoveReq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_form_rule (表单校验规则) 示例rest 接口，代码重新生成会覆盖
 */
@Tag(name = "2.表单校验规则", description = "表单校验规则管理接口")
@Validated
@RestController
@RequestMapping(Route.PREFIX)
public class FormRuleRest {

    @Resource
    private FormRuleCache formRuleCache;
    @Resource
    private MdmFormRuleService mdmFormRuleService;

    @Operation(summary = "1.表单校验规则-分页查询", description = "根据条件分页查询表单校验规则列表")
    @GetMapping(Route.FORM_RULE_PAGE)
    public R<PageData<MdmFormRuleResp>> mdmFormRulePage(@Valid MdmFormRulePageReq req) {
        MdmFormRuleDto dto = BeanUtil.cp(req, MdmFormRuleDto.class);
        PageData<MdmFormRuleDto> page = mdmFormRuleService.customPage(dto);
        PageData<MdmFormRuleResp> newPage = page.convert(MdmFormRuleResp.class);
        return R.ok(newPage);
    }

    @Operation(summary = "2.表单校验规则-详情", description = "根据ID查询表单校验规则详情")
    @GetMapping(Route.FORM_RULE_INFO)
    public R<MdmFormRuleResp> mdmFormRuleInfo(@Valid MdmFormRuleInfoReq req) {
        MdmFormRule entity = mdmFormRuleService.selectById(req.getId());
        if (entity == null) {
            return R.error("id is error");
        }
        MdmFormRuleResp resp = BeanUtil.cp(entity, MdmFormRuleResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "3.表单校验规则-创建", description = "新增表单校验规则")
    @PostMapping(Route.FORM_RULE_CREATE)
    public R<MdmFormRuleResp> mdmFormRuleCreate(@RequestBody MdmFormRuleCreateReq req) {
        MdmFormRule entity = BeanUtil.cp(req, MdmFormRule.class);
        entity = mdmFormRuleService.create(entity);
        formRuleCache.clearCache();
        MdmFormRuleResp resp = BeanUtil.cp(entity, MdmFormRuleResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "4.表单校验规则-修改", description = "修改表单校验规则")
    @PostMapping(Route.FORM_RULE_UPDATE)
    public R<MdmFormRuleResp> mdmFormRuleUpdate(@RequestBody MdmFormRuleUpdateReq req) {
        MdmFormRule entity = BeanUtil.cp(req, MdmFormRule.class);
        entity = mdmFormRuleService.update(entity);
        formRuleCache.clearCache();
        MdmFormRuleResp resp = BeanUtil.cp(entity, MdmFormRuleResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "5.表单校验规则-删除", description = "删除表单校验规则")
    @PostMapping(Route.FORM_RULE_REMOVE)
    public R<Integer> mdmFormRuleRemove(@RequestBody RemoveReq req) {
        MdmFormRule entity = new MdmFormRule();
        entity.setId(req.getId());
        Integer i = mdmFormRuleService.customRemove(entity);
        formRuleCache.clearCache();
        return R.ok(i);
    }

}
