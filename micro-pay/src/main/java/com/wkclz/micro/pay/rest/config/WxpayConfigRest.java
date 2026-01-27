package com.wkclz.micro.pay.rest.config;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.core.enums.ResultCode;
import com.wkclz.iam.sdk.helper.SessionHelper;
import com.wkclz.micro.pay.cache.WxpayClientCache;
import com.wkclz.micro.pay.pojo.dto.PayWxpayConfigDto;
import com.wkclz.micro.pay.pojo.entity.PayWxpayConfig;
import com.wkclz.micro.pay.rest.Route;
import com.wkclz.micro.pay.service.PayWxpayConfigService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table pay_wxpay_config (支付-微信支付配置) 示例rest 接口，代码重新生成会覆盖
 */
@RestController
public class WxpayConfigRest {

    @Autowired
    private WxpayClientCache wxpayClientCache;
    @Autowired
    private PayWxpayConfigService payWxpayConfigService;

    /**
     * @api {get} /wxpay/config/page 1. 支付-微信支付配置-获取分页
     * @apiGroup WXPAY_CONFIG
     *
     * @apiVersion 0.0.1
     * @apiDescription 支付-微信支付配置-获取分页
     *
     * @apiParam {String} [appId] <code>param</code>AppId
     * @apiParam {String} [mchId] <code>param</code>支付商户号
     * @apiParam {String} [mchKey] <code>param</code>支付商户密钥
     * @apiParam {String} [notifyUrl] <code>param</code>服务器异步通知路径
     * @apiParam {String} [returnUrl] <code>param</code>页面跳转同步通知页面路径
     * @apiParam {String} [refundNotifyUrl] <code>param</code>退款回调地址
     * @apiParam {String} [privateKey] <code>param</code>证书Key
     * @apiParam {String} [privateCert] <code>param</code>证书Cert
     * @apiParam {Integer} [useSandboxEnv] <code>param</code>是否使用沙箱环境
     * @apiParam {String} [verifySign] <code>param</code>微信域名验证签名
     *
     * @apiParamExample {param} 请求样例:
     * ?id=1
     *
     * @apiSuccess {String} [tenantCode] 租户编码
     * @apiSuccess {String} [appId] AppId
     * @apiSuccess {String} [mchId] 支付商户号
     * @apiSuccess {String} [mchKey] 支付商户密钥
     * @apiSuccess {String} [notifyUrl] 服务器异步通知路径
     * @apiSuccess {String} [returnUrl] 页面跳转同步通知页面路径
     * @apiSuccess {String} [refundNotifyUrl] 退款回调地址
     * @apiSuccess {String} [privateKey] 证书Key
     * @apiSuccess {String} [privateCert] 证书Cert
     * @apiSuccess {Integer} [useSandboxEnv] 是否使用沙箱环境
     * @apiSuccess {String} [verifySign] 微信域名验证签名
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
     *                 "mchId": "mchId",
     *                 "mchKey": "mchKey",
     *                 "notifyUrl": "notifyUrl",
     *                 "returnUrl": "returnUrl",
     *                 "refundNotifyUrl": "refundNotifyUrl",
     *                 "privateKey": "privateKey",
     *                 "privateCert": "privateCert",
     *                 "useSandboxEnv": "useSandboxEnv",
     *                 "verifySign": "verifySign",
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
    @GetMapping(Route.WXPAY_CONFIG_PAGE)
    public R payWxpayConfigPage(PayWxpayConfigDto dto) {
        PageData<PayWxpayConfigDto> page = payWxpayConfigService.getWxpayConfigPage(dto);
        return R.ok(page);
    }

    /**
     * @api {get} /wxpay/config/info 2. 支付-微信支付配置-获取详情
     * @apiGroup WXPAY_CONFIG
     *
     * @apiVersion 0.0.1
     * @apiDescription 支付-微信支付配置-获取详情
     *
     * @apiParam {Long} id <code>param</code>数据id
     *
     * @apiParamExample {param} 请求样例:
     * ?id=1
     *
     * @apiSuccess {Long} [id] ID
     * @apiSuccess {String} [tenantCode] 租户编码
     * @apiSuccess {String} [appId] AppId
     * @apiSuccess {String} [mchId] 支付商户号
     * @apiSuccess {String} [mchKey] 支付商户密钥
     * @apiSuccess {String} [notifyUrl] 服务器异步通知路径
     * @apiSuccess {String} [returnUrl] 页面跳转同步通知页面路径
     * @apiSuccess {String} [refundNotifyUrl] 退款回调地址
     * @apiSuccess {String} [privateKey] 证书Key
     * @apiSuccess {String} [privateCert] 证书Cert
     * @apiSuccess {Integer} [useSandboxEnv] 是否使用沙箱环境
     * @apiSuccess {String} [verifySign] 微信域名验证签名
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
     *          "mchId": "mchId",
     *          "mchKey": "mchKey",
     *          "notifyUrl": "notifyUrl",
     *          "returnUrl": "returnUrl",
     *          "refundNotifyUrl": "refundNotifyUrl",
     *          "privateKey": "privateKey",
     *          "privateCert": "privateCert",
     *          "useSandboxEnv": "useSandboxEnv",
     *          "verifySign": "verifySign",
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
    @GetMapping(Route.WXPAY_CONFIG_INFO)
    public R payWxpayConfigInfo(PayWxpayConfig entity) {
        entity.setTenantCode(SessionHelper.getTenantCode());
        entity = payWxpayConfigService.getDetail(entity);
        return R.ok(entity);
    }

    /**
     * @api {post} /wxpay/config/create 3. 支付-微信支付配置-创建
     * @apiGroup WXPAY_CONFIG
     *
     * @apiVersion 0.0.1
     * @apiDescription 支付-微信支付配置-新增信息
     *
     * @apiParam {String} [tenantCode] <code>body</code>租户编码
     * @apiParam {String} [appId] <code>body</code>AppId
     * @apiParam {String} [mchId] <code>body</code>支付商户号
     * @apiParam {String} [mchKey] <code>body</code>支付商户密钥
     * @apiParam {String} [notifyUrl] <code>body</code>服务器异步通知路径
     * @apiParam {String} [returnUrl] <code>body</code>页面跳转同步通知页面路径
     * @apiParam {String} [refundNotifyUrl] <code>body</code>退款回调地址
     * @apiParam {String} [privateKey] <code>body</code>证书Key
     * @apiParam {String} [privateCert] <code>body</code>证书Cert
     * @apiParam {Integer} [useSandboxEnv] <code>body</code>是否使用沙箱环境
     * @apiParam {String} [verifySign] <code>body</code>微信域名验证签名
     * @apiParam {Integer} [sort] <code>body</code>排序
     * @apiParam {String} [remark] <code>body</code>备注
     *
     * @apiParamExample {json} 请求样例:
     * {
     *      "tenantCode": "tenantCode",
     *      "appId": "appId",
     *      "mchId": "mchId",
     *      "mchKey": "mchKey",
     *      "notifyUrl": "notifyUrl",
     *      "returnUrl": "returnUrl",
     *      "refundNotifyUrl": "refundNotifyUrl",
     *      "privateKey": "privateKey",
     *      "privateCert": "privateCert",
     *      "useSandboxEnv": "useSandboxEnv",
     *      "verifySign": "verifySign",
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
    @PostMapping(Route.WXPAY_CONFIG_CREATE)
    public R payWxpayConfigCreate(@RequestBody PayWxpayConfig entity) {
        paramCheck(entity);
        entity = payWxpayConfigService.create(entity);
        return R.ok(entity);
    }

    /**
     * @api {post} /wxpay/config/update 4. 支付-微信支付配置-更新
     * @apiGroup WXPAY_CONFIG
     *
     * @apiVersion 0.0.1
     * @apiDescription 支付-微信支付配置-更新信息
     *
     * @apiParam {Long} id <code>body</code>ID
     * @apiParam {String} [tenantCode] <code>body</code>租户编码
     * @apiParam {String} [appId] <code>body</code>AppId
     * @apiParam {String} [mchId] <code>body</code>支付商户号
     * @apiParam {String} [mchKey] <code>body</code>支付商户密钥
     * @apiParam {String} [notifyUrl] <code>body</code>服务器异步通知路径
     * @apiParam {String} [returnUrl] <code>body</code>页面跳转同步通知页面路径
     * @apiParam {String} [refundNotifyUrl] <code>body</code>退款回调地址
     * @apiParam {String} [privateKey] <code>body</code>证书Key
     * @apiParam {String} [privateCert] <code>body</code>证书Cert
     * @apiParam {Integer} [useSandboxEnv] <code>body</code>是否使用沙箱环境
     * @apiParam {String} [verifySign] <code>body</code>微信域名验证签名
     * @apiParam {Integer} [sort] <code>body</code>排序
     * @apiParam {String} [remark] <code>body</code>备注
     * @apiParam {Integer} version <code>body</code>版本号
     *
     * @apiParamExample {json} 请求样例:
     * {
     *      "id": "id",
     *      "tenantCode": "tenantCode",
     *      "appId": "appId",
     *      "mchId": "mchId",
     *      "mchKey": "mchKey",
     *      "notifyUrl": "notifyUrl",
     *      "returnUrl": "returnUrl",
     *      "refundNotifyUrl": "refundNotifyUrl",
     *      "privateKey": "privateKey",
     *      "privateCert": "privateCert",
     *      "useSandboxEnv": "useSandboxEnv",
     *      "verifySign": "verifySign",
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
    @PostMapping(Route.WXPAY_CONFIG_UPDATE)
    public R payWxpayConfigUpdate(@RequestBody PayWxpayConfig entity) {
        Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        Assert.notNull(entity.getVersion(), ResultCode.UPDATE_NO_VERSION.getMessage());
        paramCheck(entity);
        entity = payWxpayConfigService.update(entity);
        wxpayClientCache.clearCache();
        return R.ok(entity);
    }

    /**
     * @api {post} /wxpay/config/remove 6. 支付-微信支付配置-删除
     * @apiGroup WXPAY_CONFIG
     *
     * @apiVersion 0.0.1
     * @apiDescription 支付-微信支付配置-删除
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
    @PostMapping(Route.WXPAY_CONFIG_REMOVE)
    public R payWxpayConfigRemove(@RequestBody PayWxpayConfig entity) {
        Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        payWxpayConfigService.deleteById(entity);
        wxpayClientCache.clearCache();
        return R.ok(1);
    }


    /**
     * @api {get} /wxpay/config/verify/MP_verify_{verifySign}.txt 6. 微信支付配置域名安全验证
     * @apiGroup WXPAY_CONFIG
     *
     * @apiVersion 0.0.1
     * @apiDescription 支付-微信支付配置-服务器域名验证
     *
     * @apiParam {String} verifySign <code>PathVariable</code>签名
     *
     * @apiParamExample {param} 请求样例:
     * /wxpay/config/verify/MP_verify_xxxxxxxxxxx.txt
     *
     * @apiSuccess {String} [verifySign] 签名
     *
     * @apiSuccessExample {json} 返回样例:
     * xxxxxxxxxxx
     *
     */
    @GetMapping(Route.WXPAY_CONFIG_VERIFY)
    public String wxpayConfigVerify(HttpServletRequest req, @PathVariable("verifySign") String verifySign){
        if (StringUtils.isBlank(verifySign)){
            return "have no verify sign";
        }
        PayWxpayConfig config = new PayWxpayConfig();
        config.setVerifySign(verifySign);
        config = payWxpayConfigService.selectOneByEntity(config);

        if (config == null){
            return "error verify sign";
        }
        return verifySign;
    }



    private void paramCheck(PayWxpayConfig entity) {
        Assert.notNull(entity.getAppId(), "appId");
        Assert.notNull(entity.getMchId(), "mchId");
        Assert.notNull(entity.getMchV3Key(), "mchV3Key");
        Assert.notNull(entity.getNotifyUrl(), "notifyUrl");
        Assert.notNull(entity.getReturnUrl(), "returnUrl");
        Assert.notNull(entity.getRefundNotifyUrl(), "refundNotifyUrl");
        Assert.notNull(entity.getApiclientKey(), "privateKey");
        Assert.notNull(entity.getApiclientCert(), "privateCert");
        if (StringUtils.isBlank(entity.getTenantCode())) {
            entity.setTenantCode(SessionHelper.getTenantCode());
        }
    }

}

