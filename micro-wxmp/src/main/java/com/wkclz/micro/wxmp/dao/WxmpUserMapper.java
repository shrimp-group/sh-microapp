package com.wkclz.micro.wxmp.dao;

import com.wkclz.micro.wxmp.pojo.entity.WxmpUser;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table wxmp_user (微信用户) DAO 接口，代码重新生成不覆盖
 */

@Mapper
public interface WxmpUserMapper extends BaseMapper<WxmpUser> {

    WxmpUser getWxmpUserByOpenId(@Param("openId") String openId);

    WxmpUser getWxmpUserByUserCode(@Param("userCode") String userCode);

}

