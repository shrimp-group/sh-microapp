package com.wkclz.micro.wxapp.bean.dto;

import com.wkclz.micro.wxapp.bean.entity.WxappUser;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table wxapp_user (小程序用户) 数据库实例扩展，代码重新生成不覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class WxappUserDto extends WxappUser {




    /**
     * entity 转 Dto
     * @param source
     * @return
     */
    public static WxappUserDto copy(WxappUser source) {
        WxappUserDto target = new WxappUserDto();
        WxappUser.copy(source, target);
        return target;
    }
}

