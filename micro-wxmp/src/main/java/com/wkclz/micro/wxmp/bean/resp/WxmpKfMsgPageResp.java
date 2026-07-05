package com.wkclz.micro.wxmp.bean.resp;

import com.wkclz.web.bean.EntityResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "客服消息分页响应")
public class WxmpKfMsgPageResp extends EntityResp {

    @Schema(description = "租户编码")
    private String tenantCode;

    @Schema(description = "公众号appid")
    private String appId;

    @Schema(description = "消息类型")
    private String msgType;

    @Schema(description = "发送方")
    private String fromUser;

    @Schema(description = "发送方昵称")
    private String fromUserNickname;

    @Schema(description = "接收方")
    private String toUser;

    @Schema(description = "接收方昵称")
    private String toUserNickname;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "消息ID")
    private Long msgId;

    @Schema(description = "消息时间")
    private Date msgTime;
}
