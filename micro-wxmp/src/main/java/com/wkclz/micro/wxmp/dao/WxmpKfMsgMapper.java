package com.wkclz.micro.wxmp.dao;

import com.wkclz.micro.wxmp.pojo.dto.WxmpKfMsgDto;
import com.wkclz.micro.wxmp.pojo.entity.WxmpKfMsg;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table wxmp_kf_msg (公众号-客服消息) DAO 接口，代码重新生成不覆盖
 */

@Mapper
public interface WxmpKfMsgMapper extends BaseMapper<WxmpKfMsg> {

    List<WxmpKfMsgDto> getKfMsgList(WxmpKfMsgDto dto);

}

