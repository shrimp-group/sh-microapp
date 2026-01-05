package com.wkclz.micro.wxapp.bean.dto;

import com.wkclz.micro.wxapp.bean.entity.WxappConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table wxapp_config (小程序) 数据库实例扩展，代码重新生成不覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class WxappConfigDto extends WxappConfig {




    /**
     * entity 转 Dto
     * @param source
     * @return
     */
    public static WxappConfigDto copy(WxappConfig source) {
        WxappConfigDto target = new WxappConfigDto();
        WxappConfig.copy(source, target);
        return target;
    }
}

