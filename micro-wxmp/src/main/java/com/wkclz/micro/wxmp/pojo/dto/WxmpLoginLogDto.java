package com.wkclz.micro.wxmp.pojo.dto;

import com.wkclz.micro.wxmp.pojo.entity.WxmpLoginLog;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table wxmp_login_log (微信用户登录日志) 数据库实例扩展，代码重新生成不覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class WxmpLoginLogDto extends WxmpLoginLog {




    /**
     * entity 转 Dto
     * @param source
     * @return
     */
    public static WxmpLoginLogDto copy(WxmpLoginLog source) {
        WxmpLoginLogDto target = new WxmpLoginLogDto();
        WxmpLoginLog.copy(source, target);
        return target;
    }
}

