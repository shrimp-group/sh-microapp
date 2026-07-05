package com.wkclz.micro.wxmp.mapper;

import com.wkclz.micro.wxmp.bean.entity.WxmpUser;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table wxmp_user (微信用户) Mapper 接口，代码重新生成不覆盖
 */

@Mapper
public interface WxmpUserMapper extends BaseMapper<WxmpUser> {

    WxmpUser getWxmpUserByOpenId(@Param("openId") String openId);

    WxmpUser getWxmpUserByUserCode(@Param("userCode") String userCode);

}

