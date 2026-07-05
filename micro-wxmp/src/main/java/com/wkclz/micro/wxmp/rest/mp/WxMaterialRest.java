package com.wkclz.micro.wxmp.rest.mp;

import com.wkclz.core.base.R;
import com.wkclz.micro.wxmp.bean.req.WxMaterialPageReq;
import com.wkclz.micro.wxmp.rest.Route;
import com.wkclz.micro.wxmp.service.WxMaterialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.bean.material.WxMpMaterialFileBatchGetResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "5.素材与菜单", description = "微信公众号素材与菜单管理")
@RestController
@RequestMapping(Route.PREFIX)
@Validated
public class WxMaterialRest {

    @Autowired
    private WxMaterialService wxMaterialService;

    @Operation(summary = "1.素材-获取永久素材列表", description = "获取微信公众号永久素材列表")
    @GetMapping(Route.WXMP_MATERIAL_BATCHGET_MATERIAL)
    public R<WxMpMaterialFileBatchGetResult> wxmpMaterialBatchgetMaterial(
            @PathVariable("appid") String appid, @Valid WxMaterialPageReq req) throws WxErrorException {
        req.init();
        int offset = req.getOffset().intValue();
        int count = req.getSize().intValue();
        WxMpMaterialFileBatchGetResult result = wxMaterialService.batchGetMaterial(appid, offset, count);
        return R.ok(result);
    }

    @Operation(summary = "2.菜单-设置", description = "设置微信公众号菜单")
    @PostMapping(Route.WXMP_MENU_UPDATE)
    public R<String> wxmpMenuUpdate(@PathVariable("appid") String appid) throws WxErrorException {
        String result = wxMaterialService.updateMenu(appid);
        return R.ok(result);
    }

    @Operation(summary = "3.菜单-删除", description = "删除微信公众号菜单")
    @PostMapping(Route.WXMP_MENU_DELETE)
    public R<Void> wxmpMenuDelete(@PathVariable("appid") String appid) throws WxErrorException {
        wxMaterialService.deleteMenu(appid);
        return R.ok();
    }
}
