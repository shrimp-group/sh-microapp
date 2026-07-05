package com.wkclz.micro.msg.bean.resp;

import com.wkclz.web.bean.EntityResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "消息通知响应")
public class MsgNotificationResp extends EntityResp {

    @Schema(description = "消息编码")
    private String noticeNo;

    @Schema(description = "通知发送人")
    private String userCode;

    @Schema(description = "通知标题")
    private String title;

    @Schema(description = "通知正文")
    private String content;

    @Schema(description = "扩展URL")
    private String extUrl;
}
