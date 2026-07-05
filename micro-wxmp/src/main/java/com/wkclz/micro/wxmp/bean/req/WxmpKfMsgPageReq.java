package com.wkclz.micro.wxmp.bean.req;

import com.wkclz.web.bean.PageReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "客服消息分页查询请求")
public class WxmpKfMsgPageReq extends PageReq {

    @Schema(description = "公众号appid")
    private String appId;

    @Schema(description = "消息类型")
    private String msgType;

    @Schema(description = "发送方")
    private String fromUser;

    @Schema(description = "接收方")
    private String toUser;

    @Schema(description = "消息内容")
    private String content;
}
