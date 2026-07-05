package com.wkclz.micro.pay.rest.config;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.iam.sdk.helper.SessionHelper;
import com.wkclz.micro.pay.bean.req.*;
import com.wkclz.micro.pay.cache.AlipayClientCache;
import com.wkclz.micro.pay.bean.dto.PayAlipayConfigDto;
import com.wkclz.micro.pay.bean.entity.PayAlipayConfig;
import com.wkclz.micro.pay.bean.resp.AlipayConfigCreateResp;
import com.wkclz.micro.pay.bean.resp.AlipayConfigInfoResp;
import com.wkclz.micro.pay.bean.resp.AlipayConfigPageResp;
import com.wkclz.micro.pay.bean.resp.AlipayConfigUpdateResp;
import com.wkclz.micro.pay.rest.Route;
import com.wkclz.micro.pay.service.PayAlipayConfigService;
import com.wkclz.tool.utils.BeanUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table pay_alipay_config (支付-支付宝配置) 示例rest 接口，代码重新生成会覆盖
 */
@Tag(name = "2.支付宝配置", description = "支付宝支付配置管理接口")
@RestController
@RequestMapping(Route.PREFIX)
@Validated
public class AlipayConfigRest {

    @Autowired
    private AlipayClientCache alipayClientCache;
    @Autowired
    private PayAlipayConfigService payAlipayConfigService;

    @Operation(summary = "1.支付宝配置-分页查询", description = "分页查询支付宝支付配置列表")
    @GetMapping(Route.ALIPAY_CONFIG_PAGE)
    public R<PageData<AlipayConfigPageResp>> payAlipayConfigPage(@Valid AlipayConfigPageReq req) {
        PayAlipayConfigDto dto = BeanUtil.cp(req, PayAlipayConfigDto.class);
        dto.setTenantCode(SessionHelper.getTenantCode());
        PageData<PayAlipayConfigDto> page = payAlipayConfigService.getAlipayConfigPage(dto);
        PageData<AlipayConfigPageResp> newPage = page.convert(AlipayConfigPageResp.class);
        return R.ok(newPage);
    }

    @Operation(summary = "2.支付宝配置-详情", description = "根据ID查询支付宝支付配置详情")
    @GetMapping(Route.ALIPAY_CONFIG_INFO)
    public R<AlipayConfigInfoResp> payAlipayConfigInfo(@Valid AlipayConfigInfoReq req) {
        PayAlipayConfig entity = new PayAlipayConfig();
        entity.setId(req.getId());
        entity.setTenantCode(SessionHelper.getTenantCode());
        entity = payAlipayConfigService.getDetail(entity);
        AlipayConfigInfoResp resp = BeanUtil.cp(entity, AlipayConfigInfoResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "3.支付宝配置-创建", description = "新增支付宝支付配置")
    @PostMapping(Route.ALIPAY_CONFIG_CREATE)
    public R<AlipayConfigCreateResp> payAlipayConfigCreate(@Valid @RequestBody AlipayConfigCreateReq req) {
        if (req.getIsProd() == null) {
            req.setIsProd(1);
        }
        PayAlipayConfig entity = BeanUtil.cp(req, PayAlipayConfig.class);
        if (StringUtils.isBlank(entity.getTenantCode())) {
            entity.setTenantCode(SessionHelper.getTenantCode());
        }
        entity = payAlipayConfigService.create(entity);
        AlipayConfigCreateResp resp = BeanUtil.cp(entity, AlipayConfigCreateResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "4.支付宝配置-更新", description = "更新支付宝支付配置")
    @PostMapping(Route.ALIPAY_CONFIG_UPDATE)
    public R<AlipayConfigUpdateResp> payAlipayConfigUpdate(@Valid @RequestBody AlipayConfigUpdateReq req) {
        if (req.getIsProd() == null) {
            req.setIsProd(1);
        }
        PayAlipayConfig entity = BeanUtil.cp(req, PayAlipayConfig.class);
        if (StringUtils.isBlank(entity.getTenantCode())) {
            entity.setTenantCode(SessionHelper.getTenantCode());
        }
        entity = payAlipayConfigService.update(entity);
        alipayClientCache.clearCache();
        AlipayConfigUpdateResp resp = BeanUtil.cp(entity, AlipayConfigUpdateResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "5.支付宝配置-删除", description = "删除支付宝支付配置")
    @PostMapping(Route.ALIPAY_CONFIG_REMOVE)
    public R<Integer> payAlipayConfigRemove(@Valid @RequestBody AlipayConfigRemoveReq req) {
        payAlipayConfigService.deleteById(req.getId());
        alipayClientCache.clearCache();
        return R.ok(1);
    }

}
