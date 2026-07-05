package com.wkclz.micro.wxmp.bean.entity;

import com.wkclz.core.annotation.FieldDesc;
import com.wkclz.core.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;




/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table wxmp_user (微信用户) 重新生成代码会覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class WxmpUser extends BaseEntity {

    /**
     * 用户编码
     */
    @FieldDesc("用户编码")
    private String userCode;

    /**
     * 客户昵称
     */
    @FieldDesc("客户昵称")
    private String nickname;

    /**
     * 微信appId
     */
    @FieldDesc("微信appId")
    private String appId;

    /**
     * 微信openId
     */
    @FieldDesc("微信openId")
    private String openId;

    /**
     * 微信公众平台unionId
     */
    @FieldDesc("微信公众平台unionId")
    private String unionId;

    /**
     * 关注状态
     */
    @FieldDesc("关注状态")
    private Integer subscribeStatus;

    /**
     * 手机号
     */
    @FieldDesc("手机号")
    private String mobile;

    /**
     * 邮箱
     */
    @FieldDesc("邮箱")
    private String email;

    /**
     * 性别(0未知1男2女)
     */
    @FieldDesc("性别(0未知1男2女)")
    private Integer gender;

    /**
     * 头像
     */
    @FieldDesc("头像")
    private String avatar;

    /**
     * 国家
     */
    @FieldDesc("国家")
    private String country;

    /**
     * 省
     */
    @FieldDesc("省")
    private String province;

    /**
     * 市
     */
    @FieldDesc("市")
    private String city;

    /**
     * 乡镇
     */
    @FieldDesc("乡镇")
    private String privilegeList;

    /**
     * 登录成功次数
     */
    @FieldDesc("登录成功次数")
    private Integer loginTimes;


    public static WxmpUser copy(WxmpUser source, WxmpUser target) {
        if (target == null ) { target = new WxmpUser();}
        if (source == null) { return target; }
        target.setId(source.getId());
        target.setUserCode(source.getUserCode());
        target.setNickname(source.getNickname());
        target.setAppId(source.getAppId());
        target.setOpenId(source.getOpenId());
        target.setUnionId(source.getUnionId());
        target.setSubscribeStatus(source.getSubscribeStatus());
        target.setMobile(source.getMobile());
        target.setEmail(source.getEmail());
        target.setGender(source.getGender());
        target.setAvatar(source.getAvatar());
        target.setCountry(source.getCountry());
        target.setProvince(source.getProvince());
        target.setCity(source.getCity());
        target.setPrivilegeList(source.getPrivilegeList());
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

    public static WxmpUser copyIfNotNull(WxmpUser source, WxmpUser target) {
        if (target == null ) { target = new WxmpUser();}
        if (source == null) { return target; }
        if (source.getId() != null) { target.setId(source.getId()); }
        if (source.getUserCode() != null) { target.setUserCode(source.getUserCode()); }
        if (source.getNickname() != null) { target.setNickname(source.getNickname()); }
        if (source.getAppId() != null) { target.setAppId(source.getAppId()); }
        if (source.getOpenId() != null) { target.setOpenId(source.getOpenId()); }
        if (source.getUnionId() != null) { target.setUnionId(source.getUnionId()); }
        if (source.getSubscribeStatus() != null) { target.setSubscribeStatus(source.getSubscribeStatus()); }
        if (source.getMobile() != null) { target.setMobile(source.getMobile()); }
        if (source.getEmail() != null) { target.setEmail(source.getEmail()); }
        if (source.getGender() != null) { target.setGender(source.getGender()); }
        if (source.getAvatar() != null) { target.setAvatar(source.getAvatar()); }
        if (source.getCountry() != null) { target.setCountry(source.getCountry()); }
        if (source.getProvince() != null) { target.setProvince(source.getProvince()); }
        if (source.getCity() != null) { target.setCity(source.getCity()); }
        if (source.getPrivilegeList() != null) { target.setPrivilegeList(source.getPrivilegeList()); }
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

