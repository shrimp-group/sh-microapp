package com.wkclz.micro.wxapp.dao;

import com.wkclz.micro.wxapp.bean.entity.WxappConfig;
import com.wkclz.micro.wxapp.bean.vo.WxMaAppInfo;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table wxapp_config (小程序) DAO 接口，代码重新生成不覆盖
 */

@Mapper
public interface WxappConfigMapper extends BaseMapper<WxappConfig> {

    WxMaAppInfo getWxMaAppInfo(WxMaAppInfo param);

    List<WxappConfig> getConfigList(WxappConfig config);

}

