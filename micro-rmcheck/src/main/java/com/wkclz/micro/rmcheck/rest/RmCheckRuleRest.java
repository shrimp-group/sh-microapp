package com.wkclz.micro.rmcheck.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.core.enums.ResultCode;
import com.wkclz.micro.rmcheck.pojo.dto.RmCheckRuleDto;
import com.wkclz.micro.rmcheck.pojo.entity.RmCheckRule;
import com.wkclz.micro.rmcheck.service.RmCheckRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table rm_check_rule (删除检查规则) 示例rest 接口，代码重新生成会覆盖
 */
@RestController
public class RmCheckRuleRest {

    @Autowired
    private RmCheckRuleService rmCheckRuleService;

    /**
     * @api {get} /rm/check/rule/page 1. 删除检查规则-分页
     * @apiGroup RM_CHECK_RULE
     *
     * @apiVersion 0.0.1
     * @apiDescription 删除检查规则-获取分页
     *
     * @apiParam {String} [tableName] <code>param</code>表名
     *
     * @apiParamExample {param} 请求样例:
     * ?id=1
     *
     * @apiSuccess {String} [tableName] 表名
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": {
     *         "rows": [
     *             {
     *                 "id": "id",
     *                 "tableName": "tableName",
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
    @GetMapping(Route.RM_CHECK_RULE_PAGE)
    public R rmCheckRulePage(RmCheckRuleDto dto) {
        PageData<RmCheckRuleDto> page = rmCheckRuleService.getRmCheckRulePage(dto);
        return R.ok(page);
    }


    /**
     * @api {get} /rm/check/rule/info 2. 删除检查规则-详情
     * @apiGroup RM_CHECK_RULE
     *
     * @apiVersion 0.0.1
     * @apiDescription 删除检查规则-获取详情
     *
     * @apiParam {Long} id <code>param</code>数据id
     *
     * @apiParamExample {param} 请求样例:
     * ?id=1
     *
     * @apiSuccess {Long} [id] ID
     * @apiSuccess {String} [tableName] 表名
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
    @GetMapping(Route.RM_CHECK_RULE_INFO)
    public R rmCheckRuleInfo(RmCheckRule entity) {
        entity = rmCheckRuleService.selectById(entity.getId());
        return R.ok(entity);
    }



    /**
     * @api {post} /rm/check/rule/create 3. 删除检查规则-新增
     * @apiGroup RM_CHECK_RULE
     *
     * @apiVersion 0.0.1
     * @apiDescription 删除检查规则-新增信息
     *
     * @apiParam {String} [tableName] <code>body</code>表名
     * @apiParam {Integer} [sort] <code>body</code>排序
     * @apiParam {String} [remark] <code>body</code>备注
     *
     * @apiParamExample {json} 请求样例:
     * {
     *      "tableName": "tableName",
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
    @PostMapping(Route.RM_CHECK_RULE_CREATE)
    public R rmCheckRuleCreate(@RequestBody RmCheckRule entity) {
        paramCheck(entity);
        entity = rmCheckRuleService.create(entity);
        return R.ok(entity);
    }



    /**
     * @api {post} /rm/check/rule/update 4. 删除检查规则-更新
     * @apiGroup RM_CHECK_RULE
     *
     * @apiVersion 0.0.1
     * @apiDescription 删除检查规则-更新信息
     *
     * @apiParam {Long} id <code>body</code>ID
     * @apiParam {String} [tableName] <code>body</code>表名
     * @apiParam {Integer} [sort] <code>body</code>排序
     * @apiParam {String} [remark] <code>body</code>备注
     * @apiParam {Integer} version <code>body</code>版本号
     *
     * @apiParamExample {json} 请求样例:
     * {
     *      "id": "id",
     *      "tableName": "tableName",
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
    @PostMapping(Route.RM_CHECK_RULE_UPDATE)
    public R rmCheckRuleUpdate(@RequestBody RmCheckRule entity) {
        Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        Assert.notNull(entity.getVersion(), ResultCode.UPDATE_NO_VERSION.getMessage());
        paramCheck(entity);
        entity = rmCheckRuleService.update(entity);
        return R.ok(entity);
    }



    /**
     * @api {post} /rm/check/rule/remove 5. 删除检查规则-移除
     * @apiGroup RM_CHECK_RULE
     *
     * @apiVersion 0.0.1
     * @apiDescription 删除检查规则-删除
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
    @PostMapping(Route.RM_CHECK_RULE_REMOVE)
    public R rmCheckRuleRemove(@RequestBody RmCheckRule entity) {
        Integer i = rmCheckRuleService.customRemove(entity);
        return R.ok(i);
    }


    private static void paramCheck(RmCheckRule entity) {
        Assert.notNull(entity.getTableName(), "tableName 不能为空!");
        Assert.notNull(entity.getColumnName(), "columnName 不能为空!");
        if (entity.getEnableFlag() == null) {
            entity.setEnableFlag(1);
        }
    }

}

