package com.wkclz.micro.wxapp.bean.dto;

import com.wkclz.micro.wxapp.bean.entity.WxappLoginLog;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table wxapp_login_log (小程序用户登录日志) 数据库实例扩展，代码重新生成不覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class WxappLoginLogDto extends WxappLoginLog {




    /**
     * entity 转 Dto
     * @param source
     * @return
     */
    public static WxappLoginLogDto copy(WxappLoginLog source) {
        WxappLoginLogDto target = new WxappLoginLogDto();
        WxappLoginLog.copy(source, target);
        return target;
    }
}

