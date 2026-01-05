package com.wkclz.micro.wxapp.bean.entity;

import com.wkclz.core.annotation.Desc;
import com.wkclz.core.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;




/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table wxapp_user (小程序用户) 重新生成代码会覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class WxappUser extends BaseEntity {

    /**
     * 用户编码
     */
    @Desc("用户编码")
    private String userCode;

    /**
     * 客户昵称
     */
    @Desc("客户昵称")
    private String nickname;

    /**
     * 微信appId
     */
    @Desc("微信appId")
    private String appId;

    /**
     * 微信openId
     */
    @Desc("微信openId")
    private String openId;

    /**
     * 微信公众平台unionId
     */
    @Desc("微信公众平台unionId")
    private String unionId;

    /**
     * 手机号
     */
    @Desc("手机号")
    private String mobile;

    /**
     * 邮箱
     */
    @Desc("邮箱")
    private String email;

    /**
     * 性别(0未知1男2女)
     */
    @Desc("性别(0未知1男2女)")
    private Integer gender;

    /**
     * 头像
     */
    @Desc("头像")
    private String avatar;

    /**
     * 登录成功次数
     */
    @Desc("登录成功次数")
    private Integer loginTimes;


    public static WxappUser copy(WxappUser source, WxappUser target) {
        if (target == null ) { target = new WxappUser();}
        if (source == null) { return target; }
        target.setId(source.getId());
        target.setUserCode(source.getUserCode());
        target.setNickname(source.getNickname());
        target.setAppId(source.getAppId());
        target.setOpenId(source.getOpenId());
        target.setUnionId(source.getUnionId());
        target.setMobile(source.getMobile());
        target.setEmail(source.getEmail());
        target.setGender(source.getGender());
        target.setAvatar(source.getAvatar());
        target.setLoginTimes(source.getLoginTimes());
        target.setSort(source.getSort());
        target.setCreateTime(source.getCreateTime());
        target.setCreateBy(source.getCreateBy());
        target.setUpdateTime(source.getUpdateTime());
        target.setUpdateBy(source.getUpdateBy());
        target.setRemark(source.getRemark());
        target.setVersion(source.getVersion());
        return target;
    }

    public static WxappUser copyIfNotNull(WxappUser source, WxappUser target) {
        if (target == null ) { target = new WxappUser();}
        if (source == null) { return target; }
        if (source.getId() != null) { target.setId(source.getId()); }
        if (source.getUserCode() != null) { target.setUserCode(source.getUserCode()); }
        if (source.getNickname() != null) { target.setNickname(source.getNickname()); }
        if (source.getAppId() != null) { target.setAppId(source.getAppId()); }
        if (source.getOpenId() != null) { target.setOpenId(source.getOpenId()); }
        if (source.getUnionId() != null) { target.setUnionId(source.getUnionId()); }
        if (source.getMobile() != null) { target.setMobile(source.getMobile()); }
        if (source.getEmail() != null) { target.setEmail(source.getEmail()); }
        if (source.getGender() != null) { target.setGender(source.getGender()); }
        if (source.getAvatar() != null) { target.setAvatar(source.getAvatar()); }
        if (source.getLoginTimes() != null) { target.setLoginTimes(source.getLoginTimes()); }
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

