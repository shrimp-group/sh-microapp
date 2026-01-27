package com.wkclz.micro.wxmp.dao;

import com.wkclz.micro.wxmp.pojo.entity.WxmpLoginLog;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table wxmp_login_log (微信用户登录日志) DAO 接口，代码重新生成不覆盖
 */

@Mapper
public interface WxmpLoginLogMapper extends BaseMapper<WxmpLoginLog> {

    // 示例查询,可删除
    Long example();

}

