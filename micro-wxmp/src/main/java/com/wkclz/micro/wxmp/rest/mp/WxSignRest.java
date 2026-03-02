package com.wkclz.micro.wxmp.rest.mp;

import com.wkclz.core.base.R;
import com.wkclz.micro.wxmp.config.WxMpConfiguration;
import com.wkclz.micro.wxmp.rest.Route;
import me.chanjar.weixin.common.bean.WxJsapiSignature;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author shrimp
 */
@RestController
@RequestMapping(Route.PREFIX)
public class WxSignRest {

    @Autowired
    private WxMpConfiguration wxMpConfiguration;

    @GetMapping(Route.H5_WX_SIGN)
    public R h5WxSign(@RequestParam("appid") String appid, @RequestParam("url") String url) throws WxErrorException {
        if (StringUtils.isBlank(appid)) {
            return R.error("appid 不能为空");
        }
        if (StringUtils.isBlank(url)) {
            return R.error("url 不能为空");
        }
        WxMpService mpService = wxMpConfiguration.getMpService(appid);
        WxJsapiSignature ws = mpService.createJsapiSignature(url);
        return R.ok(ws);
    }

}
