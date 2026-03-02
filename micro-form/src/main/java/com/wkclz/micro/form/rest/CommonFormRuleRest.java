package com.wkclz.micro.form.rest;

import com.wkclz.core.base.R;
import com.wkclz.micro.form.pojo.entity.MdmFormRule;
import com.wkclz.micro.form.pojo.vo.FormRuleItemVo;
import com.wkclz.micro.form.service.MdmFormRuleService;
import jakarta.annotation.Resource;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_form (表单) 示例rest 接口，代码重新生成会覆盖
 */
@RestController
@RequestMapping(Route.PREFIX)
public class CommonFormRuleRest {

    @Resource
    private MdmFormRuleService mdmFormRuleService;


    /**
     * @api {get} /common/form/rule 1. common-表单验证规则-规则清单
     * @apiGroup FORM
     *
     * @apiVersion 0.0.1
     * @apiDescription 表单-获取分页
     *
     * @apiParam {String} apiMethod <code>param</code>API方法
     * @apiParam {String} apiUri <code>param</code>API接口
     *
     * @apiParamExample {param} 请求样例:
     * ?id=1
     *
     * @apiSuccess {String} [Object] 表单校验规则
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": {
     *     }
     * }
     *
     */
    @GetMapping(Route.COMMON_FORM_RULE)
    public R commonFormRule(MdmFormRule entity) {
        Assert.notNull(entity.getApiMethod(), "apiMethod 不能为空!");
        Assert.notNull(entity.getApiUri(), "apiUri 不能为空!");
        Map<String, List<FormRuleItemVo>> rules = mdmFormRuleService.getFormRuleInfo(entity.getApiMethod(), entity.getApiUri());
        return R.ok(rules);
    }

}

