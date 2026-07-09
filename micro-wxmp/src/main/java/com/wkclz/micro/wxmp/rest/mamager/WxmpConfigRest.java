package com.wkclz.micro.wxmp.rest.mamager;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.iam.contract.context.PrincipalContext;
import com.wkclz.micro.wxmp.bean.entity.WxmpConfig;
import com.wkclz.micro.wxmp.bean.req.WxmpConfigCreateReq;
import com.wkclz.micro.wxmp.bean.req.WxmpConfigInfoReq;
import com.wkclz.micro.wxmp.bean.req.WxmpConfigPageReq;
import com.wkclz.micro.wxmp.bean.req.WxmpConfigUpdateReq;
import com.wkclz.micro.wxmp.bean.resp.WxmpConfigPageResp;
import com.wkclz.micro.wxmp.bean.resp.WxmpConfigResp;
import com.wkclz.micro.wxmp.rest.Route;
import com.wkclz.micro.wxmp.service.WxmpConfigService;
import com.wkclz.tool.utils.BeanUtil;
import com.wkclz.web.bean.RemoveReq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table wxmp_config (公众号) 示例rest 接口，代码重新生成会覆盖
 */
@Tag(name = "1.公众号配置", description = "微信公众号配置管理")
@RestController
@RequestMapping(Route.PREFIX)
@Validated
public class WxmpConfigRest {

    @Autowired
    private WxmpConfigService wxmpConfigService;

    @Operation(summary = "1.公众号配置-分页查询", description = "分页查询公众号配置列表")
    @GetMapping(Route.WXMP_CONFIG_PAGE)
    public R<PageData<WxmpConfigPageResp>> wxmpConfigPage(@Valid WxmpConfigPageReq req) {
        WxmpConfig entity = BeanUtil.cp(req, WxmpConfig.class);
        entity.setTenantCode(PrincipalContext.getTenantCode());
        PageData<WxmpConfig> page = wxmpConfigService.getConfigPage(entity);
        return R.ok(page.convert(WxmpConfigPageResp.class));
    }

    @Operation(summary = "2.公众号配置-详情", description = "根据ID查询公众号配置详情")
    @GetMapping(Route.WXMP_CONFIG_INFO)
    public R<WxmpConfigResp> wxmpConfigInfo(@Valid WxmpConfigInfoReq req) {
        WxmpConfig entity = new WxmpConfig();
        entity.setId(req.getId());
        entity.setTenantCode(PrincipalContext.getTenantCode());
        entity = wxmpConfigService.getConfigInfo(entity);
        WxmpConfigResp resp = BeanUtil.cp(entity, WxmpConfigResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "3.公众号配置-创建", description = "创建公众号配置")
    @PostMapping(Route.WXMP_CONFIG_CREATE)
    public R<WxmpConfigResp> wxmpConfigCreate(@Valid @RequestBody WxmpConfigCreateReq req) {
        WxmpConfig entity = BeanUtil.cp(req, WxmpConfig.class);
        entity.setTenantCode(PrincipalContext.getTenantCode());
        entity = wxmpConfigService.create(entity);
        WxmpConfigResp resp = BeanUtil.cp(entity, WxmpConfigResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "4.公众号配置-更新", description = "更新公众号配置")
    @PostMapping(Route.WXMP_CONFIG_UPDATE)
    public R<WxmpConfigResp> wxmpConfigUpdate(@Valid @RequestBody WxmpConfigUpdateReq req) {
        WxmpConfig entity = BeanUtil.cp(req, WxmpConfig.class);
        entity = wxmpConfigService.update(entity);
        WxmpConfigResp resp = BeanUtil.cp(entity, WxmpConfigResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "5.公众号配置-删除", description = "删除公众号配置")
    @PostMapping(Route.WXMP_CONFIG_REMOVE)
    public R<Integer> wxmpConfigRemove(@Valid @RequestBody RemoveReq req) {
        WxmpConfig entity = new WxmpConfig();
        entity.setId(req.getId());
        wxmpConfigService.deleteById(entity);
        return R.ok(1);
    }
}
