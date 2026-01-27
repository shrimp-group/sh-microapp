package com.wkclz.micro.wxapp.bean.entity;

import com.wkclz.core.annotation.Desc;
import com.wkclz.core.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;




/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table wxapp_login_log (小程序用户登录日志) 重新生成代码会覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class WxappLoginLog extends BaseEntity {

    /**
     * 用户编码
     */
    @Desc("用户编码")
    private String userCode;

    /**
     * 小程序openId
     */
    @Desc("小程序openId")
    private String openId;

    /**
     * 登录IP
     */
    @Desc("登录IP")
    private String loginIp;


    public static WxappLoginLog copy(WxappLoginLog source, WxappLoginLog target) {
        if (target == null ) { target = new WxappLoginLog();}
        if (source == null) { return target; }
        target.setId(source.getId());
        target.setUserCode(source.getUserCode());
        target.setOpenId(source.getOpenId());
        target.setLoginIp(source.getLoginIp());
        target.setSort(source.getSort());
        target.setCreateTime(source.getCreateTime());
        target.setCreateBy(source.getCreateBy());
        target.setUpdateTime(source.getUpdateTime());
        target.setUpdateBy(source.getUpdateBy());
        target.setRemark(source.getRemark());
        target.setVersion(source.getVersion());
        return target;
    }

    public static WxappLoginLog copyIfNotNull(WxappLoginLog source, WxappLoginLog target) {
        if (target == null ) { target = new WxappLoginLog();}
        if (source == null) { return target; }
        if (source.getId() != null) { target.setId(source.getId()); }
        if (source.getUserCode() != null) { target.setUserCode(source.getUserCode()); }
        if (source.getOpenId() != null) { target.setOpenId(source.getOpenId()); }
        if (source.getLoginIp() != null) { target.setLoginIp(source.getLoginIp()); }
        if (source.getSort() != null) { target.setSort(source.getSort()); }
        if (source.getCreateTime() != null) { target.setCreateTime(source.getCreateTime()); }
        if (source.getCreateBy() != null) { target.setCreateBy(source.getCreateBy()); }
        if (source.getUpdateTime() != null) { target.setUpdateTime(source.getUpdateTime()); }
        if (source.getUpdateBy() != null) { target.setUpdateBy(source.getUpdateBy()); }
        if (source.getRemark() != null) { target.setRemark(source.getRemark()); }
        if (source.getVersion() != null) { target.setVersion(source.getVersion()); }
        return target;
    }

}

