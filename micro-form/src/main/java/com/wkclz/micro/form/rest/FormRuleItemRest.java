package com.wkclz.micro.form.rest;

import com.wkclz.core.base.R;
import com.wkclz.core.enums.ResultCode;
import com.wkclz.micro.form.cache.FormRuleCache;
import com.wkclz.micro.form.pojo.entity.MdmFormRuleItem;
import com.wkclz.micro.form.service.MdmFormRuleItemService;
import io.jsonwebtoken.lang.Assert;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_form_rule_item (表单校验规则-校验项) 示例rest 接口，代码重新生成会覆盖
 */
@RestController
public class FormRuleItemRest {

    @Resource
    private FormRuleCache formRuleCache;
    @Resource
    private MdmFormRuleItemService mdmFormRuleItemService;

    /**
     * @api {get} /form/rule/item/list 1. 表单校验规则-检查项-列表
     * @apiGroup MDM_FORM_RULE_ITEM
     *
     * @apiVersion 0.0.1
     * @apiDescription 表单校验规则-校验项-获取分页
     *
     * @apiParam {String} [formRuleCode] <code>param</code>表单校验规则编码
     * @apiParam {String} [fieldName] <code>param</code>字段名称
     * @apiParam {Integer} [required] <code>param</code>必填
     * @apiParam {String} [type] <code>param</code>字段类型
     * @apiParam {Integer} [min] <code>param</code>最小值
     * @apiParam {Integer} [max] <code>param</code>最大值
     * @apiParam {String} [pattern] <code>param</code>表单校验正则
     * @apiParam {String} [trigger] <code>param</code>触发方式
     * @apiParam {String} [message] <code>param</code>验证失败提示
     *
     * @apiParamExample {param} 请求样例:
     * ?id=1
     *
     * @apiSuccess {String} [formRuleCode] 表单校验规则编码
     * @apiSuccess {String} [fieldName] 字段名称
     * @apiSuccess {Integer} [required] 必填
     * @apiSuccess {String} [type] 字段类型
     * @apiSuccess {Integer} [min] 最小值
     * @apiSuccess {Integer} [max] 最大值
     * @apiSuccess {String} [pattern] 表单校验正则
     * @apiSuccess {String} [trigger] 触发方式
     * @apiSuccess {String} [message] 验证失败提示
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": [
     *         {
     *             "id": "id",
     *             "formRuleCode": "formRuleCode",
     *             "fieldName": "fieldName",
     *             "required": "required",
     *             "type": "type",
     *             "min": "min",
     *             "max": "max",
     *             "pattern": "pattern",
     *             "trigger": "trigger",
     *             "message": "message",
     *             "sort": "sort",
     *             "createTime": "createTime",
     *             "createBy": "createBy",
     *             "updateTime": "updateTime",
     *             "updateBy": "updateBy",
     *             "remark": "remark",
     *             "version": "version",
     *         },
     *         ...
     *     ],
     * }
     *
     */
    @GetMapping(Route.FORM_RULE_ITEM_LIST)
    public R mdmFormRuleItemList(MdmFormRuleItem entity) {
        Assert.notNull(entity.getFormRuleCode(), "formRuleCode 不能为空");
        entity.setOrderBy("sort ASC, id ASC");
        List<MdmFormRuleItem> list = mdmFormRuleItemService.selectByEntity(entity);
        return R.ok(list);
    }

    /**
     * @api {get} /form/rule/item/info 2. 表单校验规则-检查项-详情
     * @apiGroup MDM_FORM_RULE_ITEM
     *
     * @apiVersion 0.0.1
     * @apiDescription 表单校验规则-校验项-获取详情
     *
     * @apiParam {Long} id <code>param</code>数据id
     *
     * @apiParamExample {param} 请求样例:
     * ?id=1
     *
     * @apiSuccess {Long} [id] ID
     * @apiSuccess {String} [formRuleCode] 表单校验规则编码
     * @apiSuccess {String} [fieldName] 字段名称
     * @apiSuccess {Integer} [required] 必填
     * @apiSuccess {String} [type] 字段类型
     * @apiSuccess {Integer} [min] 最小值
     * @apiSuccess {Integer} [max] 最大值
     * @apiSuccess {String} [pattern] 表单校验正则
     * @apiSuccess {String} [trigger] 触发方式
     * @apiSuccess {String} [message] 验证失败提示
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
     *          "fieldName": "fieldName",
     *          "required": "required",
     *          "type": "type",
     *          "min": "min",
     *          "max": "max",
     *          "pattern": "pattern",
     *          "trigger": "trigger",
     *          "message": "message",
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
    @GetMapping(Route.FORM_RULE_ITEM_INFO)
    public R mdmFormRuleItemInfo(MdmFormRuleItem entity) {
        entity = mdmFormRuleItemService.selectById(entity.getId());
        return R.ok(entity);
    }

    /**
     * @api {post} /form/rule/item/create 3. 表单校验规则-检查项-新增
     * @apiGroup MDM_FORM_RULE_ITEM
     *
     * @apiVersion 0.0.1
     * @apiDescription 表单校验规则-校验项-新增信息
     *
     * @apiParam {String} [formRuleCode] <code>body</code>表单校验规则编码
     * @apiParam {String} [fieldName] <code>body</code>字段名称
     * @apiParam {Integer} [required] <code>body</code>必填
     * @apiParam {String} [type] <code>body</code>字段类型
     * @apiParam {Integer} [min] <code>body</code>最小值
     * @apiParam {Integer} [max] <code>body</code>最大值
     * @apiParam {String} [pattern] <code>body</code>表单校验正则
     * @apiParam {String} [trigger] <code>body</code>触发方式
     * @apiParam {String} [message] <code>body</code>验证失败提示
     * @apiParam {Integer} [sort] <code>body</code>排序
     * @apiParam {String} [remark] <code>body</code>备注
     *
     * @apiParamExample {json} 请求样例:
     * {
     *      "formRuleCode": "formRuleCode",
     *      "fieldName": "fieldName",
     *      "required": "required",
     *      "type": "type",
     *      "min": "min",
     *      "max": "max",
     *      "pattern": "pattern",
     *      "trigger": "trigger",
     *      "message": "message",
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
    @PostMapping(Route.FORM_RULE_ITEM_CREATE)
    public R mdmFormRuleItemCreate(@RequestBody MdmFormRuleItem entity) {
        paramCheck(entity);
        entity = mdmFormRuleItemService.create(entity);
        formRuleCache.clearCache();
        return R.ok(entity);
    }

    /**
     * @api {post} /form/rule/item/update 4. 表单校验规则-检查项-更新
     * @apiGroup MDM_FORM_RULE_ITEM
     *
     * @apiVersion 0.0.1
     * @apiDescription 表单校验规则-校验项-更新信息
     *
     * @apiParam {Long} id <code>body</code>ID
     * @apiParam {String} [formRuleCode] <code>body</code>表单校验规则编码
     * @apiParam {String} [fieldName] <code>body</code>字段名称
     * @apiParam {Integer} [required] <code>body</code>必填
     * @apiParam {String} [type] <code>body</code>字段类型
     * @apiParam {Integer} [min] <code>body</code>最小值
     * @apiParam {Integer} [max] <code>body</code>最大值
     * @apiParam {String} [pattern] <code>body</code>表单校验正则
     * @apiParam {String} [trigger] <code>body</code>触发方式
     * @apiParam {String} [message] <code>body</code>验证失败提示
     * @apiParam {Integer} [sort] <code>body</code>排序
     * @apiParam {String} [remark] <code>body</code>备注
     * @apiParam {Integer} version <code>body</code>版本号
     *
     * @apiParamExample {json} 请求样例:
     * {
     *      "id": "id",
     *      "formRuleCode": "formRuleCode",
     *      "fieldName": "fieldName",
     *      "required": "required",
     *      "type": "type",
     *      "min": "min",
     *      "max": "max",
     *      "pattern": "pattern",
     *      "trigger": "trigger",
     *      "message": "message",
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
    @PostMapping(Route.FORM_RULE_ITEM_UPDATE)
    public R mdmFormRuleItemUpdate(@RequestBody MdmFormRuleItem entity) {
        Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        Assert.notNull(entity.getVersion(), ResultCode.UPDATE_NO_VERSION.getMessage());
        paramCheck(entity);
        entity = mdmFormRuleItemService.update(entity);
        formRuleCache.clearCache();
        return R.ok(entity);
    }

    /**
     * @api {post} /form/rule/item/remove 5. 表单校验规则-检查项-移除
     * @apiGroup MDM_FORM_RULE_ITEM
     *
     * @apiVersion 0.0.1
     * @apiDescription 表单校验规则-校验项-删除
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
    @PostMapping(Route.FORM_RULE_ITEM_REMOVE)
    public R mdmFormRuleItemRemove(@RequestBody MdmFormRuleItem entity) {
        org.springframework.util.Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        mdmFormRuleItemService.deleteById(entity);
        formRuleCache.clearCache();
        return R.ok(1);
    }



    private void paramCheck(MdmFormRuleItem entity) {
        if (entity == null) {
            return;
        }
        Assert.notNull(entity.getFormRuleCode(), "formRuleCode 不能为空!");
        Assert.notNull(entity.getFieldName(), "fieldName 不能为空!");
        if (entity.getRequired() == null) {
            entity.setRequired(1);
        }
        if (entity.getFieldType() == null) {
            entity.setFieldType("string");
        }
        if (entity.getSort() == null) {
            entity.setSort(99);
        }
        if (StringUtils.isBlank(entity.getMessage())) {
            entity.setMessage("数据不符合要求，请修正!");
        }
    }

}

