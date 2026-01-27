package com.wkclz.micro.wxmp.pojo.dto;

import com.wkclz.micro.wxmp.pojo.entity.WxmpConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table wxmp_config (公众号) 数据库实例扩展，代码重新生成不覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class WxmpConfigDto extends WxmpConfig {




    /**
     * entity 转 Dto
     * @param source
     * @return
     */
    public static WxmpConfigDto copy(WxmpConfig source) {
        WxmpConfigDto target = new WxmpConfigDto();
        WxmpConfig.copy(source, target);
        return target;
    }
}

