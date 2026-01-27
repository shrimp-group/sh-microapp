package com.wkclz.micro.wxmp.pojo.dto;

import com.wkclz.micro.wxmp.pojo.entity.WxmpKfMsg;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table wxmp_kf_msg (公众号-客服消息) 数据库实例扩展，代码重新生成不覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class WxmpKfMsgDto extends WxmpKfMsg {


    private String fromUserNickname;
    private String toUserNickname;


    /**
     * entity 转 Dto
     * @param source
     * @return
     */
    public static WxmpKfMsgDto copy(WxmpKfMsg source) {
        WxmpKfMsgDto target = new WxmpKfMsgDto();
        WxmpKfMsg.copy(source, target);
        return target;
    }
}

