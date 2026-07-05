package com.wkclz.micro.form.rest;

import com.wkclz.core.base.R;
import com.wkclz.micro.form.bean.dto.MdmFormRuleFieldValidatorDto;
import com.wkclz.micro.form.bean.req.CommonFormRuleReq;
import com.wkclz.micro.form.bean.resp.CommonFormRuleResp;
import com.wkclz.micro.form.service.MdmFormRuleFieldValidatorService;
import com.wkclz.tool.utils.BeanUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_form (表单) 示例rest 接口，代码重新生成会覆盖
 */
@Tag(name = "6.通用表单验证规则", description = "通用表单验证规则查询接口")
@Validated
@RestController
@RequestMapping(Route.PREFIX)
public class CommonFormRuleRest {

    @Resource
    private MdmFormRuleFieldValidatorService mdmFormRuleFieldValidatorService;

    @Operation(summary = "1.通用表单验证规则-规则清单", description = "根据API方法和路径获取表单验证规则清单")
    @GetMapping(Route.COMMON_FORM_RULE)
    public R<List<CommonFormRuleResp>> commonFormRule(@Valid CommonFormRuleReq req) {
        List<MdmFormRuleFieldValidatorDto> validators = mdmFormRuleFieldValidatorService.getFormRuleFieldValidatorList4Check(req.getApiMethod(), req.getApiUri());
        List<CommonFormRuleResp> respList = BeanUtil.cp(validators, CommonFormRuleResp.class);
        return R.ok(respList);
    }

}
