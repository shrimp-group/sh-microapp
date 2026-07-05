package com.wkclz.micro.wxmp.service;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.wxmp.config.WxMpConfiguration;
import com.wkclz.micro.wxmp.bean.entity.WxmpConfig;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpMaterialService;
import me.chanjar.weixin.mp.api.WxMpMenuService;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.material.WxMpMaterialFileBatchGetResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WxMaterialService {

    @Autowired
    private WxMpConfiguration wxMpConfiguration;
    @Autowired
    private WxmpConfigService wxmpConfigService;

    /**
     * 获取永久素材列表
     */
    public WxMpMaterialFileBatchGetResult batchGetMaterial(String appId, int offset, int count) throws WxErrorException {
        log.info("获取微信素材列表, appId: {}, offset: {}, count: {}", appId, offset, count);
        WxMpService mpService = wxMpConfiguration.getMpService(appId);
        WxMpMaterialService materialService = mpService.getMaterialService();
        return materialService.materialFileBatchGet(WxConsts.MediaFileType.IMAGE, offset, count);
    }

    /**
     * 设置公众号菜单
     */
    public String updateMenu(String appId) throws WxErrorException {
        log.info("设置公众号菜单, appId: {}", appId);
        WxMpService mpService = wxMpConfiguration.getMpService(appId);
        WxmpConfig wxmpConfig = wxmpConfigService.getConfigByAppId(appId);

        String mpMenuJson = wxmpConfig.getMpMenuJson();
        if (StringUtils.isBlank(mpMenuJson)) {
            throw ValidationException.of("未配置预设菜单!");
        }

        WxMpMenuService menuService = mpService.getMenuService();
        String result = menuService.menuCreate(mpMenuJson);
        log.info("设置公众号菜单成功, appId: {}, result: {}", appId, result);
        return result;
    }

    /**
     * 删除公众号菜单
     */
    public void deleteMenu(String appId) throws WxErrorException {
        log.info("删除公众号菜单, appId: {}", appId);
        WxMpService mpService = wxMpConfiguration.getMpService(appId);
        WxMpMenuService menuService = mpService.getMenuService();
        menuService.menuDelete();
        log.info("删除公众号菜单成功, appId: {}", appId);
    }
}
