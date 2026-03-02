package com.wkclz.micro.form.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.core.enums.ResultCode;
import com.wkclz.micro.form.cache.FormRuleCache;
import com.wkclz.micro.form.pojo.dto.MdmFormRuleDto;
import com.wkclz.micro.form.pojo.entity.MdmFormRule;
import com.wkclz.micro.form.service.MdmFormRuleService;
import jakarta.annotation.Resource;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_form_rule (表单校验规则) 示例rest 接口，代码重新生成会覆盖
 */
@RestController
@RequestMapping(Route.PREFIX)
public class FormRuleRest {

    @Resource
    private FormRuleCache formRuleCache;
    @Resource
    private MdmFormRuleService mdmFormRuleService;


    /**
     * @api {get} /micro-form/rule/page 1. 表单校验规则-分页
     * @apiGroup MDM_FORM_RULE
     *
     * @apiVersion 0.0.1
     * @apiDescription 表单校验规则-获取分页
     *
     * @apiParam {String} [formRuleCode] <code>param</code>表单校验规则编码
     * @apiParam {String} [formRuleName] <code>param</code>表单校验规则编码
     *
     * @apiParamExample {param} 请求样例:
     * ?id=1
     *
     * @apiSuccess {String} [formRuleCode] 表单校验规则编码
     * @apiSuccess {String} [formRuleName] 表单校验规则编码
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": {
     *         "rows": [
     *             {
     *                 "id": "id",
     *                 "formRuleCode": "formRuleCode",
     *                 "formRuleName": "formRuleName",
     *                 "sort": "sort",
     *                 "createTime": "createTime",
     *                 "createBy": "createBy",
     *                 "updateTime": "updateTime",
     *                 "updateBy": "updateBy",
     *                 "remark": "remark",
     *                 "version": "version",
     *             },
     *             ...
     *         ],
     *         "current": 1,
     *         "size": 10,
     *         "total": 1,
     *         "page": 1,
     *     }
     * }
     *
     */
    @GetMapping(Route.FORM_RULE_PAGE)
    public R mdmFormRulePage(MdmFormRuleDto dto) {
        PageData<MdmFormRuleDto> page = mdmFormRuleService.customPage(dto);
        return R.ok(page);
    }


    /**
     * @api {get} /micro-form/rule/info 2. 表单校验规则-详情
     * @apiGroup MDM_FORM_RULE
     *
     * @apiVersion 0.0.1
     * @apiDescription 表单校验规则-获取详情
     *
     * @apiParam {Long} id <code>param</code>数据id
     *
     * @apiParamExample {param} 请求样例:
     * ?id=1
     *
     * @apiSuccess {Long} [id] ID
     * @apiSuccess {String} [formRuleCode] 表单校验规则编码
     * @apiSuccess {String} [formRuleName] 表单校验规则编码
     * @apiSuccess {Integer} [sort] 排序
     * @apiSuccess {Date} [createTime] 创建时间
     * @apiSuccess {String} [createBy] 创建人
     * @apiSuccess {Date} [updateTime] 更新时间
     * @apiSuccess {String} [updateBy] 更新人
     * @apiSuccess {String} [remark] 备注
     * @apiSuccess {Integer} [version] 版本号
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": {
     *          "id": "id",
     *          "formRuleCode": "formRuleCode",
     *          "formRuleName": "formRuleName",
     *          "sort": "sort",
     *          "createTime": "createTime",
     *          "createBy": "createBy",
     *          "updateTime": "updateTime",
     *          "updateBy": "updateBy",
     *          "remark": "remark",
     *          "version": "version",
     *          "status": "status",
     *     }
     * }
     *
     */
    @GetMapping(Route.FORM_RULE_INFO)
    public R mdmFormRuleInfo(MdmFormRule entity) {
        entity = mdmFormRuleService.selectById(entity.getId());
        return R.ok(entity);
    }


    /**
     * @api {post} /micro-form/rule/create 3. 表单校验规则-新增
     * @apiGroup MDM_FORM_RULE
     *
     * @apiVersion 0.0.1
     * @apiDescription 表单校验规则-新增信息
     *
     * @apiParam {String} [formRuleCode] <code>body</code>表单校验规则编码
     * @apiParam {String} [formRuleName] <code>body</code>表单校验规则编码
     * @apiParam {Integer} [sort] <code>body</code>排序
     * @apiParam {String} [remark] <code>body</code>备注
     *
     * @apiParamExample {json} 请求样例:
     * {
     *      "formRuleCode": "formRuleCode",
     *      "formRuleName": "formRuleName",
     *      "sort": "sort",
     *      "remark": "remark",
     * }
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": ObjectEntity
     * }
     *
     */
    @PostMapping(Route.FORM_RULE_CREATE)
    public R mdmFormRuleCreate(@RequestBody MdmFormRule entity) {
        paramCheck(entity);
        entity = mdmFormRuleService.create(entity);
        formRuleCache.clearCache();
        return R.ok(entity);
    }


    /**
     * @api {post} /micro-form/rule/update 4. 表单校验规则-更新
     * @apiGroup MDM_FORM_RULE
     *
     * @apiVersion 0.0.1
     * @apiDescription 表单校验规则-更新信息
     *
     * @apiParam {Long} id <code>body</code>ID
     * @apiParam {String} [formRuleCode] <code>body</code>表单校验规则编码
     * @apiParam {String} [formRuleName] <code>body</code>表单校验规则编码
     * @apiParam {Integer} [sort] <code>body</code>排序
     * @apiParam {String} [remark] <code>body</code>备注
     * @apiParam {Integer} version <code>body</code>版本号
     *
     * @apiParamExample {json} 请求样例:
     * {
     *      "id": "id",
     *      "formRuleCode": "formRuleCode",
     *      "formRuleName": "formRuleName",
     *      "sort": "sort",
     *      "remark": "remark",
     *      "version": "version",
     * }
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": ObjectEntity
     * }
     *
     */
    @PostMapping(Route.FORM_RULE_UPDATE)
    public R mdmFormRuleUpdate(@RequestBody MdmFormRule entity) {
        Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        Assert.notNull(entity.getVersion(), ResultCode.UPDATE_NO_VERSION.getMessage());
        paramCheck(entity);
        entity = mdmFormRuleService.update(entity);
        formRuleCache.clearCache();
        return R.ok(entity);
    }


    /**
     * @api {post} /micro-form/rule/remove 5. 表单校验规则-移除
     * @apiGroup MDM_FORM_RULE
     *
     * @apiVersion 0.0.1
     * @apiDescription 表单校验规则-删除
     *
     * @apiParam {Long} [id] <code>body</code>数据id
     *
     * @apiParamExample {json} 请求样例:
     * {
     *     "id": 1
     * }
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": 1
     * }
     *
     */
    @PostMapping(Route.FORM_RULE_REMOVE)
    public R mdmFormRuleRemove(@RequestBody MdmFormRule entity) {
        Integer i = mdmFormRuleService.customRemove(entity);
        formRuleCache.clearCache();
        return R.ok(i);
    }




    private void paramCheck(MdmFormRule entity) {
        if (entity == null) {
            return;
        }
        Assert.notNull(entity.getFormRuleName(), "formRuleName 不能为空!");
        Assert.notNull(entity.getApiMethod(), "apiMethod 不能为空!");
        Assert.notNull(entity.getApiUri(), "apiUri 不能为空!");
    }

}

