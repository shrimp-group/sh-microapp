package com.wkclz.micro.pay.rest.config;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.iam.contract.context.PrincipalContext;
import com.wkclz.micro.pay.cache.WxpayClientCache;
import com.wkclz.micro.pay.bean.dto.PayWxpayConfigDto;
import com.wkclz.micro.pay.bean.entity.PayWxpayConfig;
import com.wkclz.micro.pay.bean.req.WxpayConfigCreateReq;
import com.wkclz.micro.pay.bean.req.WxpayConfigInfoReq;
import com.wkclz.micro.pay.bean.req.WxpayConfigPageReq;
import com.wkclz.micro.pay.bean.req.WxpayConfigRemoveReq;
import com.wkclz.micro.pay.bean.req.WxpayConfigUpdateReq;
import com.wkclz.micro.pay.bean.resp.WxpayConfigCreateResp;
import com.wkclz.micro.pay.bean.resp.WxpayConfigInfoResp;
import com.wkclz.micro.pay.bean.resp.WxpayConfigPageResp;
import com.wkclz.micro.pay.bean.resp.WxpayConfigUpdateResp;
import com.wkclz.micro.pay.rest.Route;
import com.wkclz.micro.pay.service.PayWxpayConfigService;
import com.wkclz.tool.utils.BeanUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table pay_wxpay_config (支付-微信支付配置) 示例rest 接口，代码重新生成会覆盖
 */

@Tag(name = "1.微信支付配置", description = "微信支付配置管理接口")
@RestController
@RequestMapping(Route.PREFIX)
@Validated
public class WxpayConfigRest {

    @Autowired
    private WxpayClientCache wxpayClientCache;
    @Autowired
    private PayWxpayConfigService payWxpayConfigService;

    @Operation(summary = "1.微信支付配置-分页查询", description = "分页查询微信支付配置列表")
    @GetMapping(Route.WXPAY_CONFIG_PAGE)
    public R<PageData<WxpayConfigPageResp>> payWxpayConfigPage(@Valid WxpayConfigPageReq req) {
        PayWxpayConfigDto dto = BeanUtil.cp(req, PayWxpayConfigDto.class);
        PageData<PayWxpayConfigDto> page = payWxpayConfigService.getWxpayConfigPage(dto);
        PageData<WxpayConfigPageResp> newPage = page.convert(WxpayConfigPageResp.class);
        return R.ok(newPage);
    }

    @Operation(summary = "2.微信支付配置-详情", description = "根据ID查询微信支付配置详情")
    @GetMapping(Route.WXPAY_CONFIG_INFO)
    public R<WxpayConfigInfoResp> payWxpayConfigInfo(@Valid WxpayConfigInfoReq req) {
        PayWxpayConfig entity = new PayWxpayConfig();
        entity.setId(req.getId());
        entity.setTenantCode(PrincipalContext.getTenantCode());
        entity = payWxpayConfigService.getDetail(entity);
        WxpayConfigInfoResp resp = BeanUtil.cp(entity, WxpayConfigInfoResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "3.微信支付配置-创建", description = "新增微信支付配置")
    @PostMapping(Route.WXPAY_CONFIG_CREATE)
    public R<WxpayConfigCreateResp> payWxpayConfigCreate(@Valid @RequestBody WxpayConfigCreateReq req) {
        PayWxpayConfig entity = BeanUtil.cp(req, PayWxpayConfig.class);
        if (StringUtils.isBlank(entity.getTenantCode())) {
            entity.setTenantCode(PrincipalContext.getTenantCode());
        }
        entity = payWxpayConfigService.create(entity);
        WxpayConfigCreateResp resp = BeanUtil.cp(entity, WxpayConfigCreateResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "4.微信支付配置-更新", description = "更新微信支付配置")
    @PostMapping(Route.WXPAY_CONFIG_UPDATE)
    public R<WxpayConfigUpdateResp> payWxpayConfigUpdate(@Valid @RequestBody WxpayConfigUpdateReq req) {
        PayWxpayConfig entity = BeanUtil.cp(req, PayWxpayConfig.class);
        if (StringUtils.isBlank(entity.getTenantCode())) {
            entity.setTenantCode(PrincipalContext.getTenantCode());
        }
        entity = payWxpayConfigService.update(entity);
        wxpayClientCache.clearCache();
        WxpayConfigUpdateResp resp = BeanUtil.cp(entity, WxpayConfigUpdateResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "5.微信支付配置-删除", description = "删除微信支付配置")
    @PostMapping(Route.WXPAY_CONFIG_REMOVE)
    public R<Integer> payWxpayConfigRemove(@Valid @RequestBody WxpayConfigRemoveReq req) {
        PayWxpayConfig entity = new PayWxpayConfig();
        entity.setId(req.getId());
        payWxpayConfigService.deleteById(entity);
        wxpayClientCache.clearCache();
        return R.ok(1);
    }

    @Operation(summary = "6.微信支付配置-域名验证", description = "微信支付服务器域名验证")
    @GetMapping(Route.WXPAY_CONFIG_VERIFY)
    public String wxpayConfigVerify(HttpServletRequest req, @PathVariable("verifySign") String verifySign) {
        if (StringUtils.isBlank(verifySign)) {
            return "have no verify sign";
        }
        PayWxpayConfig config = new PayWxpayConfig();
        config.setVerifySign(verifySign);
        config = payWxpayConfigService.selectOneByEntity(config);
        if (config == null) {
            return "error verify sign";
        }
        return verifySign;
    }
}
