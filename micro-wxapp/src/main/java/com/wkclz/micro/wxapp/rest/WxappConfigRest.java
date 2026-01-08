package com.wkclz.micro.wxapp.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.core.enums.ResultCode;
import com.wkclz.iam.sdk.helper.SessionHelper;
import com.wkclz.micro.wxapp.Route;
import com.wkclz.micro.wxapp.bean.entity.WxappConfig;
import com.wkclz.micro.wxapp.service.WxappConfigService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table wxapp_config (小程序) 示例rest 接口，代码重新生成会覆盖
 */
@RestController
@RequestMapping(Route.PREFIX)
public class WxappConfigRest {

    @Autowired
    private WxappConfigService wxappConfigService;

    /**
     * @api {get} /wxmp/config/page 1. 微信小程序-配置-分页
     * @apiGroup WXMP_CONFIG
     *
     * @apiVersion 0.0.1
     * @apiDescription 小程序-获取分页
     *
     * @apiParam {String} [tenantCode] <code>param</code>租户编码
     * @apiParam {String} [appId] <code>param</code>小程序appid
     * @apiParam {String} [appSecret] <code>param</code>小程序Secret
     * @apiParam {String} [appToken] <code>param</code>小程序appToken
     * @apiParam {String} [aesKey] <code>param</code>小程序AESKey
     *
     * @apiParamExample {param} 请求样例:
     * ?id=1
     *
     * @apiSuccess {String} [tenantCode] 租户编码
     * @apiSuccess {String} [appId] 小程序appid
     * @apiSuccess {String} [appSecret] 小程序Secret
     * @apiSuccess {String} [appToken] 小程序appToken
     * @apiSuccess {String} [aesKey] 小程序AESKey
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": {
     *         "rows": [
     *             {
     *                 "id": "id",
     *                 "tenantCode": "tenantCode",
     *                 "appId": "appId",
     *                 "appSecret": "appSecret",
     *                 "appToken": "appToken",
     *                 "aesKey": "aesKey",
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
    @GetMapping(Route.WXAPP_CONFIG_PAGE)
    public R wxappConfigPage(WxappConfig entity) {
        entity.setTenantCode(SessionHelper.getTenantCode());
        PageData<WxappConfig> page = wxappConfigService.getConfigPage(entity);
        return R.ok(page);
    }



    /**
     * @api {get} /wxmp/config/info 2. 微信小程序-配置-详情
     * @apiGroup WXMP_CONFIG
     *
     * @apiVersion 0.0.1
     * @apiDescription 小程序-获取详情
     *
     * @apiParam {Long} id <code>param</code>数据id
     *
     * @apiParamExample {param} 请求样例:
     * ?id=1
     *
     * @apiSuccess {Long} [id] ID
     * @apiSuccess {String} [tenantCode] 租户编码
     * @apiSuccess {String} [appId] 小程序appid
     * @apiSuccess {String} [appSecret] 小程序Secret
     * @apiSuccess {String} [appToken] 小程序appToken
     * @apiSuccess {String} [aesKey] 小程序AESKey
     * @apiSuccess {String} [welcomeMsg] 欢迎信息
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
     *          "tenantCode": "tenantCode",
     *          "appId": "appId",
     *          "appSecret": "appSecret",
     *          "appToken": "appToken",
     *          "aesKey": "aesKey",
     *          "welcomeMsg": "welcomeMsg",
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
    @GetMapping(Route.WXAPP_CONFIG_INFO)
    public R wxappConfigInfo(WxappConfig entity) {
        Assert.notNull(entity.getId(), "请求错误！参数[id]不能为空");
        entity.setTenantCode(SessionHelper.getTenantCode());
        entity = wxappConfigService.getConfigInfo(entity);
        return R.ok(entity);
    }



    /**
     * @api {post} /wxmp/config/create 3. 微信小程序-配置-创建
     * @apiGroup WXMP_CONFIG
     *
     * @apiVersion 0.0.1
     * @apiDescription 小程序-新增信息
     *
     * @apiParam {String} [tenantCode] <code>body</code>租户编码
     * @apiParam {String} [appId] <code>body</code>小程序appid
     * @apiParam {String} [appSecret] <code>body</code>小程序Secret
     * @apiParam {String} [appToken] <code>body</code>小程序appToken
     * @apiParam {String} [aesKey] <code>body</code>小程序AESKey
     * @apiParam {String} [welcomeMsg] <code>body</code>欢迎信息
     * @apiParam {Integer} [sort] <code>body</code>排序
     * @apiParam {String} [remark] <code>body</code>备注
     *
     * @apiParamExample {json} 请求样例:
     * {
     *      "tenantCode": "tenantCode",
     *      "appId": "appId",
     *      "appSecret": "appSecret",
     *      "appToken": "appToken",
     *      "aesKey": "aesKey",
     *      "welcomeMsg": "welcomeMsg",
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
    @PostMapping(Route.WXAPP_CONFIG_CREATE)
    public R wxappConfigCreate(@RequestBody WxappConfig entity) {
        paramCheck(entity);
        entity.setTenantCode(SessionHelper.getTenantCode());
        entity = wxappConfigService.create(entity);
        return R.ok(entity);
    }




    /**
     * @api {post} /wxmp/config/update 4. 微信小程序-配置-更新
     * @apiGroup WXMP_CONFIG
     *
     * @apiVersion 0.0.1
     * @apiDescription 小程序-更新信息
     *
     * @apiParam {Long} id <code>body</code>ID
     * @apiParam {String} [tenantCode] <code>body</code>租户编码
     * @apiParam {String} [appId] <code>body</code>小程序appid
     * @apiParam {String} [appSecret] <code>body</code>小程序Secret
     * @apiParam {String} [appToken] <code>body</code>小程序appToken
     * @apiParam {String} [aesKey] <code>body</code>小程序AESKey
     * @apiParam {String} [welcomeMsg] <code>body</code>欢迎信息
     * @apiParam {Integer} [sort] <code>body</code>排序
     * @apiParam {String} [remark] <code>body</code>备注
     * @apiParam {Integer} version <code>body</code>版本号
     *
     * @apiParamExample {json} 请求样例:
     * {
     *      "id": "id",
     *      "tenantCode": "tenantCode",
     *      "appId": "appId",
     *      "appSecret": "appSecret",
     *      "appToken": "appToken",
     *      "aesKey": "aesKey",
     *      "welcomeMsg": "welcomeMsg",
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
    @PostMapping(Route.WXAPP_CONFIG_UPDATE)
    public R wxappConfigUpdate(@RequestBody WxappConfig entity) {
        Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        Assert.notNull(entity.getVersion(), ResultCode.UPDATE_NO_VERSION.getMessage());
        paramCheck(entity);
        entity = wxappConfigService.update(entity);
        return R.ok(entity);
    }



    /**
     * @api {post} /wxmp/config/remove 5. 微信小程序-配置-删除
     * @apiGroup WXMP_CONFIG
     *
     * @apiVersion 0.0.1
     * @apiDescription 小程序-删除
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
    @PostMapping(Route.WXAPP_CONFIG_REMOVE)
    public R wxappConfigRemove(@RequestBody WxappConfig entity) {
        wxappConfigService.deleteById(entity);
        return R.ok(1);
    }



    private void paramCheck(WxappConfig entity) {
        Assert.notNull(entity.getAppId(), "appId 不能为空");
        Assert.notNull(entity.getAppSecret(), "appSecret 不能为空");
        if (StringUtils.isBlank(entity.getTenantCode())) {
            entity.setTenantCode(SessionHelper.getTenantCode());
        }
    }


}

