package com.wkclz.micro.wxapp.dao;

import com.wkclz.micro.wxapp.bean.entity.WxappUser;
import com.wkclz.mybatis.base.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table wxapp_user (小程序用户) DAO 接口，代码重新生成不覆盖
 */

@Mapper
public interface WxappUserMapper extends BaseMapper<WxappUser> {

    WxappUser getWxappUserByOpenId(@Param("openId") String openId);

    WxappUser getWxappUserByUserCode(@Param("userCode") String userCode);

}

