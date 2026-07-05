package com.wkclz.micro.wxmp.mapper;

import com.wkclz.micro.wxmp.bean.resp.WxmpKfMsgPageResp;
import com.wkclz.micro.wxmp.bean.entity.WxmpKfMsg;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table wxmp_kf_msg (公众号-客服消息) Mapper 接口，代码重新生成不覆盖
 */

@Mapper
public interface WxmpKfMsgMapper extends BaseMapper<WxmpKfMsg> {

    List<WxmpKfMsgPageResp> getKfMsgList(WxmpKfMsg dto);

}

