package com.wkclz.micro.pay.rest.config;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.core.enums.ResultCode;
import com.wkclz.iam.sdk.helper.SessionHelper;
import com.wkclz.micro.pay.cache.AlipayClientCache;
import com.wkclz.micro.pay.pojo.dto.PayAlipayConfigDto;
import com.wkclz.micro.pay.pojo.entity.PayAlipayConfig;
import com.wkclz.micro.pay.rest.Route;
import com.wkclz.micro.pay.service.PayAlipayConfigService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table pay_alipay_config (支付-支付宝配置) 示例rest 接口，代码重新生成会覆盖
 */
@RestController
public class AlipayConfigRest {

    @Autowired
    private AlipayClientCache alipayClientCache;
    @Autowired
    private PayAlipayConfigService payAlipayConfigService;


    /**
     * @api {get} /alipay/config/page 1. 支付宝支付-分页
     * @apiGroup ALIPAY_CONFIG
     *
     * @apiVersion 0.0.1
     * @apiDescription 支付-支付宝配置-获取分页
     *
     * @apiParam {String} [appId] <code>param</code>应用ID
     *
     * @apiParamExample {param} 请求样例:
     * ?id=1
     *
     * @apiSuccess {String} [tenantCode] 租户编码
     * @apiSuccess {String} [appId] 应用ID
     * @apiSuccess {String} [merchantPrivateKey] 商户私钥
     * @apiSuccess {String} [alipayPublicKey] 支付宝公钥
     * @apiSuccess {String} [appPublicKey] 应用公钥
     * @apiSuccess {String} [notifyUrl] 服务器异步通知路径
     * @apiSuccess {String} [returnUrl] 页面跳转同步通知页面路径
     * @apiSuccess {String} [signType] 签名方式
     * @apiSuccess {String} [charset] 字符编码格式
     * @apiSuccess {Integer} [isProd] 是否生产环境
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
     *                 "merchantPrivateKey": "merchantPrivateKey",
     *                 "alipayPublicKey": "alipayPublicKey",
     *                 "appPublicKey": "appPublicKey",
     *                 "notifyUrl": "notifyUrl",
     *                 "returnUrl": "returnUrl",
     *                 "signType": "signType",
     *                 "charset": "charset",
     *                 "gatewayUrl": "gatewayUrl",
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
    @GetMapping(Route.ALIPAY_CONFIG_PAGE)
    public R payAlipayConfigPage(PayAlipayConfigDto entity) {
        entity.setTenantCode(SessionHelper.getTenantCode());
        PageData<PayAlipayConfigDto> page = payAlipayConfigService.getAlipayConfigPage(entity);
        return R.ok(page);
    }

    /**
     * @api {get} /alipay/config/info 2. 支付宝支付-详情
     * @apiGroup ALIPAY_CONFIG
     *
     * @apiVersion 0.0.1
     * @apiDescription 支付-支付宝配置-获取详情
     *
     * @apiParam {Long} id <code>param</code>数据id
     *
     * @apiParamExample {param} 请求样例:
     * ?id=1
     *
     * @apiSuccess {Long} [id] ID
     * @apiSuccess {String} [tenantCode] 租户编码
     * @apiSuccess {String} [appId] 应用ID
     * @apiSuccess {String} [merchantPrivateKey] 商户私钥
     * @apiSuccess {String} [alipayPublicKey] 支付宝公钥
     * @apiSuccess {String} [appPublicKey] 应用公钥
     * @apiSuccess {String} [notifyUrl] 服务器异步通知路径
     * @apiSuccess {String} [returnUrl] 页面跳转同步通知页面路径
     * @apiSuccess {String} [signType] 签名方式
     * @apiSuccess {String} [charset] 字符编码格式
     * @apiSuccess {Integer} [isProd] 是否生产环境
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
     *          "merchantPrivateKey": "merchantPrivateKey",
     *          "alipayPublicKey": "alipayPublicKey",
     *          "appPublicKey": "appPublicKey",
     *          "notifyUrl": "notifyUrl",
     *          "returnUrl": "returnUrl",
     *          "signType": "signType",
     *          "charset": "charset",
     *          "gatewayUrl": "gatewayUrl",
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
    @GetMapping(Route.ALIPAY_CONFIG_INFO)
    public R payAlipayConfigInfo(PayAlipayConfig entity) {
        entity.setTenantCode(SessionHelper.getTenantCode());
        entity = payAlipayConfigService.getDetail(entity);
        return R.ok(entity);
    }

    /**
     * @api {post} /alipay/config/create 3. 支付宝支付-创建
     * @apiGroup ALIPAY_CONFIG
     *
     * @apiVersion 0.0.1
     * @apiDescription 支付-支付宝配置-新增信息
     *
     * @apiParam {String} [tenantCode] <code>body</code>租户编码
     * @apiParam {String} [appId] <code>body</code>应用ID
     * @apiParam {String} [merchantPrivateKey] <code>body</code>商户私钥
     * @apiParam {String} [alipayPublicKey] <code>body</code>支付宝公钥
     * @apiParam {String} [appPublicKey] <code>body</code>应用公钥
     * @apiParam {String} [notifyUrl] <code>body</code>服务器异步通知路径
     * @apiParam {String} [returnUrl] <code>body</code>页面跳转同步通知页面路径
     * @apiParam {String} [signType] <code>body</code>签名方式
     * @apiParam {String} [charset] <code>body</code>字符编码格式
     * @apiParam {Integer} [isProd] <code>body</code>是否生产环境
     * @apiParam {Integer} [sort] <code>body</code>排序
     * @apiParam {String} [remark] <code>body</code>备注
     *
     * @apiParamExample {json} 请求样例:
     * {
     *      "tenantCode": "tenantCode",
     *      "appId": "appId",
     *      "merchantPrivateKey": "merchantPrivateKey",
     *      "alipayPublicKey": "alipayPublicKey",
     *      "appPublicKey": "appPublicKey",
     *      "notifyUrl": "notifyUrl",
     *      "returnUrl": "returnUrl",
     *      "signType": "signType",
     *      "charset": "charset",
     *      "gatewayUrl": "gatewayUrl",
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
    @PostMapping(Route.ALIPAY_CONFIG_CREATE)
    public R payAlipayConfigCreate(@RequestBody PayAlipayConfig entity) {
        paramCheck(entity);
        entity = payAlipayConfigService.create(entity);
        return R.ok(entity);
    }

    /**
     * @api {post} /alipay/config/update 4. 支付宝支付-更新
     * @apiGroup ALIPAY_CONFIG
     *
     * @apiVersion 0.0.1
     * @apiDescription 支付-支付宝配置-更新信息
     *
     * @apiParam {Long} id <code>body</code>ID
     * @apiParam {String} [tenantCode] <code>body</code>租户编码
     * @apiParam {String} [appId] <code>body</code>应用ID
     * @apiParam {String} [merchantPrivateKey] <code>body</code>商户私钥
     * @apiParam {String} [alipayPublicKey] <code>body</code>支付宝公钥
     * @apiParam {String} [appPublicKey] <code>body</code>应用公钥
     * @apiParam {String} [notifyUrl] <code>body</code>服务器异步通知路径
     * @apiParam {String} [returnUrl] <code>body</code>页面跳转同步通知页面路径
     * @apiParam {String} [signType] <code>body</code>签名方式
     * @apiParam {String} [charset] <code>body</code>字符编码格式
     * @apiParam {Integer} [isProd] <code>body</code>是否生产环境
     * @apiParam {Integer} [sort] <code>body</code>排序
     * @apiParam {String} [remark] <code>body</code>备注
     * @apiParam {Integer} version <code>body</code>版本号
     *
     * @apiParamExample {json} 请求样例:
     * {
     *      "id": "id",
     *      "tenantCode": "tenantCode",
     *      "appId": "appId",
     *      "merchantPrivateKey": "merchantPrivateKey",
     *      "alipayPublicKey": "alipayPublicKey",
     *      "appPublicKey": "appPublicKey",
     *      "notifyUrl": "notifyUrl",
     *      "returnUrl": "returnUrl",
     *      "signType": "signType",
     *      "charset": "charset",
     *      "gatewayUrl": "gatewayUrl",
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
    @PostMapping(Route.ALIPAY_CONFIG_UPDATE)
    public R payAlipayConfigUpdate(@RequestBody PayAlipayConfig entity) {
        Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        Assert.notNull(entity.getVersion(), ResultCode.UPDATE_NO_VERSION.getMessage());
        paramCheck(entity);
        entity = payAlipayConfigService.update(entity);
        alipayClientCache.clearCache();
        return R.ok(entity);
    }


    /**
     * @api {post} /alipay/config/remove 5. 支付宝支付-移除
     * @apiGroup ALIPAY_CONFIG
     *
     * @apiVersion 0.0.1
     * @apiDescription 支付-支付宝配置-删除
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
    @PostMapping(Route.ALIPAY_CONFIG_REMOVE)
    public R payAlipayConfigRemove(@RequestBody PayAlipayConfig entity) {
        Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        payAlipayConfigService.deleteById(entity);
        alipayClientCache.clearCache();
        return R.ok(1);
    }



    private void paramCheck(PayAlipayConfig entity) {
        Assert.notNull(entity.getAppId(), "appId 不能为空！");
        Assert.notNull(entity.getAlipayPublicKey(), "alipayPublicKey 不能为空");
        Assert.notNull(entity.getAppPublicKey(), "appPublicKey 不能为空");
        Assert.notNull(entity.getNotifyUrl(), "notifyUrl 不能为空");
        Assert.notNull(entity.getReturnUrl(), "returnUrl 不能为空");
        Assert.notNull(entity.getSignType(), "signType 不能为空");
        Assert.notNull(entity.getCharset(), "charset 不能为空");
        if (entity.getIsProd() == null) {
            entity.setIsProd(1);
        }
        if (StringUtils.isBlank(entity.getTenantCode())) {
            entity.setTenantCode(SessionHelper.getTenantCode());
        }
    }
}

