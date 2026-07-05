package com.wkclz.micro.wxmp.mapper;

import com.wkclz.micro.wxmp.bean.entity.WxmpConfig;
import com.wkclz.micro.wxmp.bean.dto.WxMpAppInfo;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table wxmp_config (公众号) Mapper 接口，代码重新生成不覆盖
 */

@Mapper
public interface WxmpConfigMapper extends BaseMapper<WxmpConfig> {

    WxMpAppInfo getWxMpAppInfo(WxMpAppInfo config);

    List<WxmpConfig> getConfigList(WxmpConfig config);


}

