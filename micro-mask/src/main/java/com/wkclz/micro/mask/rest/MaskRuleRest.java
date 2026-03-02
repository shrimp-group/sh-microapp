package com.wkclz.micro.mask.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.core.enums.ResultCode;
import com.wkclz.micro.mask.cache.MaskCache;
import com.wkclz.micro.mask.config.MaskResponseAdvice;
import com.wkclz.micro.mask.pojo.entity.MdmMaskRule;
import com.wkclz.micro.mask.service.MdmMaskRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_mask_rule (脱敏规则) 示例rest 接口，代码重新生成会覆盖
 */
@RestController
@RequestMapping(Route.PREFIX)
public class MaskRuleRest {

    @Autowired
    private MaskCache maskCache;
    @Autowired
    private MdmMaskRuleService mdmMaskRuleService;

    /**
     * @api {get} /micro-mask/rule/page 1. 脱敏规则-分页
     * @apiGroup MASK
     *
     * @apiVersion 0.0.1
     * @apiDescription 脱敏规则-获取分页
     *
     * @apiParam {String} [maskRuleCode] <code>param</code>脱敏规则编码(模糊搜索)
     * @apiParam {String} [maskRuleName] <code>param</code>脱敏规则名称(模糊搜索)
     *
     * @apiParamExample {param} 请求样例:
     * ?id=1
     *
     * @apiSuccess {String} [maskRuleCode] 脱敏规则编码
     * @apiSuccess {String} [maskRuleName] 脱敏规则名称
     * @apiSuccess {String} [maskRuleRegular] 脱敏正则
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": {
     *         "rows": [
     *             {
     *                 "id": "id",
     *                 "maskRuleCode": "maskRuleCode",
     *                 "maskRuleName": "maskRuleName",
     *                 "maskRuleRegular": "maskRuleRegular",
     *                 "sort": "sort",
     *                 "createTime": "createTime",
     *                 "createBy": "createBy",
     *                 "updateTime": "updateTime",
     *                 "updateBy": "updateBy",
     *                 "remark": "remark",
     *                 "version": "version"
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
    @GetMapping(Route.RULE_PAGE)
    public R mdmMaskRulePage(MdmMaskRule entity) {
        PageData<MdmMaskRule> page = mdmMaskRuleService.getMaskRulePage(entity);
        return R.ok(page);
    }


    /**
     * @api {get} /micro-mask/rule/info 2. 脱敏规则-详情
     * @apiGroup MASK
     *
     * @apiVersion 0.0.1
     * @apiDescription 脱敏规则-获取详情
     *
     * @apiParam {Long} id <code>param</code>数据id
     *
     * @apiParamExample {param} 请求样例:
     * ?id=1
     *
     * @apiSuccess {Long} [id] ID
     * @apiSuccess {String} [maskRuleCode] 脱敏规则编码
     * @apiSuccess {String} [maskRuleName] 脱敏规则名称
     * @apiSuccess {String} [maskRuleRegular] 脱敏正则
     * @apiSuccess {String} [maskRuleScript] 脱敏函数
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
     *          "maskRuleCode": "maskRuleCode",
     *          "maskRuleName": "maskRuleName",
     *          "maskRuleRegular": "maskRuleRegular",
     *          "maskRuleScript": "maskRuleScript",
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
    @GetMapping(Route.RULE_INFO)
    public R mdmMaskRuleInfo(MdmMaskRule entity) {
        Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        Assert.notNull(entity.getId(), ResultCode.UPDATE_NO_VERSION.getMessage());
        entity = mdmMaskRuleService.selectById(entity.getId());
        return R.ok(entity);
    }


    /**
     * @api {post} /micro-mask/rule/create 3. 脱敏规则-新增
     * @apiGroup MASK
     *
     * @apiVersion 0.0.1
     * @apiDescription 脱敏规则-新增信息
     *
     * @apiParam {String} [maskRuleCode] <code>body</code>脱敏规则编码
     * @apiParam {String} [maskRuleName] <code>body</code>脱敏规则名称
     * @apiParam {String} [maskRuleRegular] <code>body</code>脱敏正则
     * @apiParam {String} [maskRuleScript] <code>body</code>脱敏函数
     * @apiParam {Integer} [sort] <code>body</code>排序
     * @apiParam {String} [remark] <code>body</code>备注
     *
     * @apiParamExample {json} 请求样例:
     * {
     *      "maskRuleCode": "maskRuleCode",
     *      "maskRuleName": "maskRuleName",
     *      "maskRuleRegular": "maskRuleRegular",
     *      "maskRuleScript": "maskRuleScript",
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
    @PostMapping(Route.RULE_CREATE)
    public R mdmMaskRuleCreate(@RequestBody MdmMaskRule entity) {
        Assert.notNull(entity.getMaskRuleName(), "maskRuleName 不能为空");
        Assert.notNull(entity.getRequestMethod(), "requestMethod 不能为空");
        Assert.notNull(entity.getRequestUri(), "requestUri 不能为空");
        Assert.notNull(entity.getMaskJsonPath(), "maskJsonPath 不能为空");
        if (entity.getEnableFlag() == null) {
            entity.setEnableFlag(1);
        }
        entity = mdmMaskRuleService.create(entity);
        maskCache.clearCache();
        MaskResponseAdvice.clearCache();
        return R.ok(entity);
    }


    /**
     * @api {post} /micro-mask/rule/update 4. 脱敏规则-修改
     * @apiGroup MASK
     *
     * @apiVersion 0.0.1
     * @apiDescription 脱敏规则-更新信息
     *
     * @apiParam {Long} id <code>body</code>ID
     * @apiParam {String} [maskRuleCode] <code>body</code>脱敏规则编码
     * @apiParam {String} [maskRuleName] <code>body</code>脱敏规则名称
     * @apiParam {String} [maskRuleRegular] <code>body</code>脱敏正则
     * @apiParam {String} [maskRuleScript] <code>body</code>脱敏函数
     * @apiParam {Integer} [sort] <code>body</code>排序
     * @apiParam {String} [remark] <code>body</code>备注
     * @apiParam {Integer} version <code>body</code>版本号
     *
     * @apiParamExample {json} 请求样例:
     * {
     *      "id": "id",
     *      "maskRuleCode": "maskRuleCode",
     *      "maskRuleName": "maskRuleName",
     *      "maskRuleRegular": "maskRuleRegular",
     *      "maskRuleScript": "maskRuleScript",
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
    @PostMapping(Route.RULE_UPDATE)
    public R mdmMaskRuleUpdate(@RequestBody MdmMaskRule entity) {
        Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        Assert.notNull(entity.getVersion(), ResultCode.UPDATE_NO_VERSION.getMessage());
        Assert.notNull(entity.getMaskRuleName(), "maskRuleName 不能为空");
        Assert.notNull(entity.getRequestMethod(), "requestMethod 不能为空");
        Assert.notNull(entity.getRequestUri(), "requestUri 不能为空");
        Assert.notNull(entity.getMaskJsonPath(), "maskJsonPath 不能为空");
        if (entity.getEnableFlag() == null) {
            entity.setEnableFlag(1);
        }
        entity = mdmMaskRuleService.update(entity);
        maskCache.clearCache();
        MaskResponseAdvice.clearCache();
        return R.ok(entity);
    }


    /**
     * @api {post} /micro-mask/rule/remove 5. 脱敏规则-删除
     * @apiGroup MASK
     *
     * @apiVersion 0.0.1
     * @apiDescription 脱敏规则-删除
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
    @PostMapping(Route.RULE_REMOVE)
    public R mdmMaskRuleRemove(@RequestBody MdmMaskRule entity) {
        Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        mdmMaskRuleService.deleteById(entity);
        maskCache.clearCache();
        MaskResponseAdvice.clearCache();
        return R.ok(1);
    }


}

