package com.wkclz.micro.form.rest;

import com.wkclz.core.base.R;
import com.wkclz.micro.form.bean.dto.MdmFormRuleFieldValidatorDto;
import com.wkclz.micro.form.bean.req.MdmFormRuleValidatorCreateReq;
import com.wkclz.micro.form.bean.resp.MdmFormRuleValidatorResp;
import com.wkclz.micro.form.service.MdmFormRuleFieldValidatorService;
import com.wkclz.tool.utils.BeanUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_form_rule (表单校验规则) 示例rest 接口，代码重新生成会覆盖
 */
@Tag(name = "3.表单校验规则验证器", description = "表单校验规则验证器管理接口")
@Validated
@RestController
@RequestMapping(Route.PREFIX)
public class FormRuleValidatorRest {

    @Resource
    private MdmFormRuleFieldValidatorService mdmFormRuleFieldValidatorService;

    @Operation(summary = "6.表单校验规则-字段及验证器", description = "根据规则编码获取字段及验证器列表")
    @GetMapping(Route.FORM_RULE_FIELD_AND_VALIDATOR)
    public R<List<MdmFormRuleValidatorResp>> formRuleFieldAndValidator(@RequestParam String formRuleCode) {
        List<MdmFormRuleFieldValidatorDto> validatorList = mdmFormRuleFieldValidatorService.getFormRuleFieldValidatorList(formRuleCode);
        List<MdmFormRuleValidatorResp> respList = BeanUtil.cp(validatorList, MdmFormRuleValidatorResp.class);
        return R.ok(respList);
    }

    @Operation(summary = "7.表单校验规则-字段及验证器保存", description = "批量保存字段及验证器")
    @PostMapping(Route.FORM_RULE_FIELD_AND_VALIDATOR_SAVE)
    public R<Integer> formRuleFieldAndValidatorSave(@RequestBody List<MdmFormRuleValidatorCreateReq> reqList) {
        if (CollectionUtils.isEmpty(reqList)) {
            return R.error("表单校验规则列表不能为空");
        }
        List<MdmFormRuleFieldValidatorDto> validators = BeanUtil.cp(reqList, MdmFormRuleFieldValidatorDto.class);
        Integer save = mdmFormRuleFieldValidatorService.saveBatch(validators);
        return R.ok(save);
    }

}
