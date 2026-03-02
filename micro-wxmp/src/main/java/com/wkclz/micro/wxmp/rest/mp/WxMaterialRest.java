package com.wkclz.micro.wxmp.rest.mp;

import com.wkclz.core.base.BaseEntity;
import com.wkclz.core.base.R;
import com.wkclz.micro.wxmp.config.WxMpConfiguration;
import com.wkclz.micro.wxmp.pojo.entity.WxmpConfig;
import com.wkclz.micro.wxmp.rest.Route;
import com.wkclz.micro.wxmp.service.WxmpConfigService;
import jakarta.annotation.Resource;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpMaterialService;
import me.chanjar.weixin.mp.api.WxMpMenuService;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.material.WxMpMaterialFileBatchGetResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;


/**
 * @author shrimp
 */
@RestController
@RequestMapping(Route.PREFIX)
public class WxMaterialRest {

    @Resource
    private WxMpConfiguration wxMpConfiguration;
    @Resource
    private WxmpConfigService wxmpConfigService;


    @GetMapping(Route.WXMP_MATERIAL_BATCHGET_MATERIAL)
    public R wxmpMaterialBatchgetMaterial(@PathVariable("appid") String appid, BaseEntity entity) throws WxErrorException {
        entity.init();
        int offset = entity.getOffset().intValue();
        int count = entity.getSize().intValue();

        WxMpService mpService = wxMpConfiguration.getMpService(appid);
        WxMpMaterialService materialService = mpService.getMaterialService();
        WxMpMaterialFileBatchGetResult result = materialService.materialFileBatchGet(WxConsts.MediaFileType.IMAGE, offset, count);
        return R.ok(result);
    }


    @PostMapping(Route.WXMP_MENU_UPDATE)
    public R wxmpMenuUpdate(@PathVariable("appid") String appid) throws WxErrorException {
        WxMpService mpService = wxMpConfiguration.getMpService(appid);
        WxmpConfig wxmpConfig = wxmpConfigService.getConfigByAppId(appid);

        String mpMenuJson = wxmpConfig.getMpMenuJson();
        if (StringUtils.isBlank(mpMenuJson)) {
            return R.error("未配置预设菜单!");
        }
        WxMpMenuService menuService = mpService.getMenuService();
        String s = menuService.menuCreate(mpMenuJson);
        return R.ok(s);
    }


    @PostMapping(Route.WXMP_MENU_DELETE)
    public R wxmpMenuDelete(@PathVariable("appid") String appid) throws WxErrorException {
        WxMpService mpService = wxMpConfiguration.getMpService(appid);
        WxMpMenuService menuService = mpService.getMenuService();
        menuService.menuDelete();
        return R.ok();
    }


}
