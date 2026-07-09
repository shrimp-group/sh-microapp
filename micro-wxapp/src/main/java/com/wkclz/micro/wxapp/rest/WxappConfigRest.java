package com.wkclz.micro.wxapp.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.iam.contract.context.PrincipalContext;
import com.wkclz.micro.wxapp.Route;
import com.wkclz.micro.wxapp.bean.entity.WxappConfig;
import com.wkclz.micro.wxapp.bean.req.*;
import com.wkclz.micro.wxapp.bean.resp.WxappConfigPageResp;
import com.wkclz.micro.wxapp.bean.resp.WxappConfigResp;
import com.wkclz.micro.wxapp.service.WxappConfigService;
import jakarta.validation.Valid;
import com.wkclz.tool.utils.BeanUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table wxapp_config (小程序) 示例rest 接口，代码重新生成会覆盖
 */
@Tag(name = "小程序配置", description = "微信小程序配置管理接口")
@RestController
@RequestMapping(Route.PREFIX)
@Validated
public class WxappConfigRest {

    @Autowired
    private WxappConfigService wxappConfigService;

    @Operation(summary = "1. 小程序配置-分页查询")
    @GetMapping(Route.WXAPP_CONFIG_PAGE)
    public R<PageData<WxappConfigPageResp>> wxappConfigPage(@Valid WxappConfigPageReq req) {
        WxappConfig entity = BeanUtil.cp(req, WxappConfig.class);
        entity.setTenantCode(PrincipalContext.getTenantCode());
        PageData<WxappConfig> page = wxappConfigService.getConfigPage(entity);
        PageData<WxappConfigPageResp> newPage = page.convert(WxappConfigPageResp.class);
        return R.ok(newPage);
    }

    @Operation(summary = "2. 小程序配置-详情")
    @GetMapping(Route.WXAPP_CONFIG_INFO)
    public R<WxappConfigResp> wxappConfigInfo(@Valid WxappConfigInfoReq req) {
        WxappConfig entity = new WxappConfig();
        entity.setId(req.getId());
        entity.setTenantCode(PrincipalContext.getTenantCode());
        entity = wxappConfigService.getConfigInfo(entity);
        WxappConfigResp resp = BeanUtil.cp(entity, WxappConfigResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "3. 小程序配置-创建")
    @PostMapping(Route.WXAPP_CONFIG_CREATE)
    public R<WxappConfigResp> wxappConfigCreate(@Valid @RequestBody WxappConfigCreateReq req) {
        WxappConfig entity = BeanUtil.cp(req, WxappConfig.class);
        entity.setTenantCode(PrincipalContext.getTenantCode());
        entity = wxappConfigService.create(entity);
        WxappConfigResp resp = BeanUtil.cp(entity, WxappConfigResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "4. 小程序配置-更新")
    @PostMapping(Route.WXAPP_CONFIG_UPDATE)
    public R<WxappConfigResp> wxappConfigUpdate(@Valid @RequestBody WxappConfigUpdateReq req) {
        WxappConfig entity = BeanUtil.cp(req, WxappConfig.class);
        entity = wxappConfigService.update(entity);
        WxappConfigResp resp = BeanUtil.cp(entity, WxappConfigResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "5. 小程序配置-删除")
    @PostMapping(Route.WXAPP_CONFIG_REMOVE)
    public R<Integer> wxappConfigRemove(@Valid @RequestBody WxappConfigRemoveReq req) {
        WxappConfig entity = new WxappConfig();
        entity.setId(req.getId());
        entity.setIds(req.getIds());
        wxappConfigService.deleteById(entity);
        return R.ok(1);
    }

}
