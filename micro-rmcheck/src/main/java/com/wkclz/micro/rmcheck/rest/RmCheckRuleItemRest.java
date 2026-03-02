package com.wkclz.micro.rmcheck.rest;

import com.wkclz.core.base.R;
import com.wkclz.core.enums.ResultCode;
import com.wkclz.micro.rmcheck.pojo.dto.RmCheckRuleItemDto;
import com.wkclz.micro.rmcheck.pojo.entity.RmCheckRuleItem;
import com.wkclz.micro.rmcheck.service.RmCheckRuleItemService;
import io.jsonwebtoken.lang.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table rm_check_rule_item (删除检查规则-检查项) 示例rest 接口，代码重新生成会覆盖
 */
@RestController
@RequestMapping(Route.PREFIX)
public class RmCheckRuleItemRest {

    @Autowired
    private RmCheckRuleItemService rmCheckRuleItemService;

    /**
     * @api {get} /micro-rmcheck/rule/item/page 1. 删除检查规则-检查项-列表
     * @apiGroup RM_CHECK_RULE_ITEM
     *
     * @apiVersion 0.0.1
     * @apiDescription 删除检查规则-检查项-获取分页
     *
     * @apiParam {String} [tableName] <code>param</code>表名
     * @apiParam {String} [checkTableName] <code>param</code>被检查表名
     * @apiParam {String} [checkColumnName] <code>param</code>被检查字段名
     *
     * @apiParamExample {param} 请求样例:
     * ?id=1
     *
     * @apiSuccess {String} [tableName] 表名
     * @apiSuccess {String} [checkTableName] 被检查表名
     * @apiSuccess {String} [checkColumnName] 被检查字段名
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": [
     *         {
     *             "id": "id",
     *             "tableName": "tableName",
     *             "checkTableName": "checkTableName",
     *             "checkColumnName": "checkColumnName",
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
    @GetMapping(Route.RM_CHECK_RULE_ITEM_LIST)
    public R rmCheckRuleItemPage(RmCheckRuleItemDto dto) {
        Assert.notNull(dto.getRuleCode(), "ruleCode 不能为空！");
        List<RmCheckRuleItemDto> list = rmCheckRuleItemService.getRmCheckRuleItemList(dto);
        return R.ok(list);
    }


    /**
     * @api {get} /micro-rmcheck/rule/item/info 2. 删除检查规则-检查项-详情
     * @apiGroup RM_CHECK_RULE_ITEM
     *
     * @apiVersion 0.0.1
     * @apiDescription 删除检查规则-检查项-获取详情
     *
     * @apiParam {Long} id <code>param</code>数据id
     *
     * @apiParamExample {param} 请求样例:
     * ?id=1
     *
     * @apiSuccess {Long} [id] ID
     * @apiSuccess {String} [tableName] 表名
     * @apiSuccess {String} [checkTableName] 被检查表名
     * @apiSuccess {String} [checkColumnName] 被检查字段名
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
     *          "tableName": "tableName",
     *          "checkTableName": "checkTableName",
     *          "checkColumnName": "checkColumnName",
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
    @GetMapping(Route.RM_CHECK_RULE_ITEM_INFO)
    public R rmCheckRuleItemInfo(RmCheckRuleItem entity) {
        entity = rmCheckRuleItemService.selectById(entity.getId());
        return R.ok(entity);
    }



    /**
     * @api {post} /micro-rmcheck/rule/item/create 3. 删除检查规则-检查项-新增
     * @apiGroup RM_CHECK_RULE_ITEM
     *
     * @apiVersion 0.0.1
     * @apiDescription 删除检查规则-检查项-新增信息
     *
     * @apiParam {String} [tableName] <code>body</code>表名
     * @apiParam {String} [checkTableName] <code>body</code>被检查表名
     * @apiParam {String} [checkColumnName] <code>body</code>被检查字段名
     * @apiParam {Integer} [sort] <code>body</code>排序
     * @apiParam {String} [remark] <code>body</code>备注
     *
     * @apiParamExample {json} 请求样例:
     * {
     *      "tableName": "tableName",
     *      "checkTableName": "checkTableName",
     *      "checkColumnName": "checkColumnName",
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
    @PostMapping(Route.RM_CHECK_RULE_ITEM_CREATE)
    public R rmCheckRuleItemCreate(@RequestBody RmCheckRuleItem entity) {
        paramCheck(entity);
        entity = rmCheckRuleItemService.create(entity);
        return R.ok(entity);
    }


    /**
     * @api {post} /micro-rmcheck/rule/item/update 4. 删除检查规则-检查项-更新
     * @apiGroup RM_CHECK_RULE_ITEM
     *
     * @apiVersion 0.0.1
     * @apiDescription 删除检查规则-检查项-更新信息
     *
     * @apiParam {Long} id <code>body</code>ID
     * @apiParam {String} [tableName] <code>body</code>表名
     * @apiParam {String} [checkTableName] <code>body</code>被检查表名
     * @apiParam {String} [checkColumnName] <code>body</code>被检查字段名
     * @apiParam {Integer} [sort] <code>body</code>排序
     * @apiParam {String} [remark] <code>body</code>备注
     * @apiParam {Integer} version <code>body</code>版本号
     *
     * @apiParamExample {json} 请求样例:
     * {
     *      "id": "id",
     *      "tableName": "tableName",
     *      "checkTableName": "checkTableName",
     *      "checkColumnName": "checkColumnName",
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
    @PostMapping(Route.RM_CHECK_RULE_ITEM_UPDATE)
    public R rmCheckRuleItemUpdate(@RequestBody RmCheckRuleItem entity) {
        Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        Assert.notNull(entity.getVersion(), ResultCode.UPDATE_NO_VERSION.getMessage());
        paramCheck(entity);
        entity = rmCheckRuleItemService.update(entity);
        return R.ok(entity);
    }


    /**
     * @api {post} /micro-rmcheck/rule/item/remove 5. 删除检查规则-检查项-移除
     * @apiGroup RM_CHECK_RULE_ITEM
     *
     * @apiVersion 0.0.1
     * @apiDescription 删除检查规则-检查项-删除
     *
     * @apiParam {Long} [id] <code>body</code>数据id
     * @apiParam {Long[]} [ids] <code>body</code>数据ids(当支持批量删除时)
     *
     * @apiParamExample {json} 请求样例:
     * {
     *     "id": 1,
     *     "ids": [1]
     * }
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": 1
     * }
     *
     */
    @PostMapping(Route.RM_CHECK_RULE_ITEM_REMOVE)
    public R rmCheckRuleItemRemove(@RequestBody RmCheckRuleItem entity) {
        org.springframework.util.Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        rmCheckRuleItemService.deleteById(entity);
        return R.ok(1);
    }


    private static void paramCheck(RmCheckRuleItem entity) {
        Assert.notNull(entity.getRuleCode(), "ruleCode 不能为空!");
        Assert.notNull(entity.getCheckTableName(), "checkTableName 不能为空!");
        Assert.notNull(entity.getCheckColumnName(), "checkColumnName 不能为空!");
        if (entity.getEnableFlag() == null) {
            entity.setEnableFlag(1);
        }
    }

}

