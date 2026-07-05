package com.wkclz.micro.form.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.micro.form.bean.entity.MdmFormRuleValidatorTemplate;
import com.wkclz.micro.form.bean.req.MdmFormRuleValidatorTemplateCreateReq;
import com.wkclz.micro.form.bean.req.MdmFormRuleValidatorTemplateInfoReq;
import com.wkclz.micro.form.bean.req.MdmFormRuleValidatorTemplatePageReq;
import com.wkclz.micro.form.bean.req.MdmFormRuleValidatorTemplateUpdateReq;
import com.wkclz.micro.form.bean.resp.MdmFormRuleValidatorTemplateResp;
import com.wkclz.micro.form.service.MdmFormRuleValidatorTemplateService;
import com.wkclz.tool.utils.BeanUtil;
import com.wkclz.web.bean.RemoveReq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Description Create by sh-generator
 * @author shrimp
 * @table mdm_form_rule_validator_template (表单校验规则-模板) 示例rest 接口，代码重新生成会覆盖
 */
@Tag(name = "4.表单校验规则模板", description = "表单校验规则模板管理接口")
@Validated
@RestController
@RequestMapping(Route.PREFIX)
public class FormRuleValidatorTemplateRest {

    @Resource
    private MdmFormRuleValidatorTemplateService mdmFormRuleValidatorTemplateService;

    @Operation(summary = "1.表单校验规则模板-分页查询", description = "根据条件分页查询表单校验规则模板列表")
    @GetMapping(Route.FORM_RULE_VALIDATOR_TEMPLATE_PAGE)
    public R<PageData<MdmFormRuleValidatorTemplateResp>> mdmFormRuleValidatorTemplatePage(@Valid MdmFormRuleValidatorTemplatePageReq req) {
        MdmFormRuleValidatorTemplate entity = BeanUtil.cp(req, MdmFormRuleValidatorTemplate.class);
        PageData<MdmFormRuleValidatorTemplate> page = mdmFormRuleValidatorTemplateService.getValidatorTemplatePage(entity);
        PageData<MdmFormRuleValidatorTemplateResp> newPage = page.convert(MdmFormRuleValidatorTemplateResp.class);
        return R.ok(newPage);
    }

    @Operation(summary = "2.表单校验规则模板-详情", description = "根据ID查询表单校验规则模板详情")
    @GetMapping(Route.FORM_RULE_VALIDATOR_TEMPLATE_INFO)
    public R<MdmFormRuleValidatorTemplateResp> mdmFormRuleValidatorTemplateInfo(MdmFormRuleValidatorTemplateInfoReq req) {
        MdmFormRuleValidatorTemplate entity = mdmFormRuleValidatorTemplateService.selectById(req.getId());
        if (entity == null) {
            return R.error("id is error");
        }
        MdmFormRuleValidatorTemplateResp resp = BeanUtil.cp(entity, MdmFormRuleValidatorTemplateResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "3.表单校验规则模板-创建", description = "新增表单校验规则模板")
    @PostMapping(Route.FORM_RULE_VALIDATOR_TEMPLATE_CREATE)
    public R<MdmFormRuleValidatorTemplateResp> mdmFormRuleValidatorTemplateCreate(@RequestBody MdmFormRuleValidatorTemplateCreateReq req) {
        MdmFormRuleValidatorTemplate entity = BeanUtil.cp(req, MdmFormRuleValidatorTemplate.class);
        entity = mdmFormRuleValidatorTemplateService.create(entity);
        MdmFormRuleValidatorTemplateResp resp = BeanUtil.cp(entity, MdmFormRuleValidatorTemplateResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "4.表单校验规则模板-修改", description = "修改表单校验规则模板")
    @PostMapping(Route.FORM_RULE_VALIDATOR_TEMPLATE_UPDATE)
    public R<MdmFormRuleValidatorTemplateResp> mdmFormRuleValidatorTemplateUpdate(@RequestBody MdmFormRuleValidatorTemplateUpdateReq req) {
        MdmFormRuleValidatorTemplate entity = BeanUtil.cp(req, MdmFormRuleValidatorTemplate.class);
        entity = mdmFormRuleValidatorTemplateService.update(entity);
        MdmFormRuleValidatorTemplateResp resp = BeanUtil.cp(entity, MdmFormRuleValidatorTemplateResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "5.表单校验规则模板-删除", description = "删除表单校验规则模板")
    @PostMapping(Route.FORM_RULE_VALIDATOR_TEMPLATE_REMOVE)
    public R<Integer> mdmFormRuleValidatorTemplateRemove(@RequestBody RemoveReq req) {
        mdmFormRuleValidatorTemplateService.deleteById(req.getId());
        return R.ok(1);
    }

}
