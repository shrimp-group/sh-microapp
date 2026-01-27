package com.wkclz.micro.wxmp.pojo.dto;

import com.wkclz.micro.wxmp.pojo.entity.WxmpUser;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table wxmp_user (微信用户) 数据库实例扩展，代码重新生成不覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class WxmpUserDto extends WxmpUser {




    /**
     * entity 转 Dto
     * @param source
     * @return
     */
    public static WxmpUserDto copy(WxmpUser source) {
        WxmpUserDto target = new WxmpUserDto();
        WxmpUser.copy(source, target);
        return target;
    }
}

