package com.wkclz.micro.wxapp.mapper;

import com.wkclz.micro.wxapp.bean.entity.WxappLoginLog;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table wxapp_login_log (小程序用户登录日志) DAO 接口，代码重新生成不覆盖
 */

@Mapper
public interface WxappLoginLogMapper extends BaseMapper<WxappLoginLog> {

    // 示例查询,可删除
    Long example();

}

