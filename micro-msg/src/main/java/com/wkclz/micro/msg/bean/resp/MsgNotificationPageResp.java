package com.wkclz.micro.msg.bean.resp;

import com.wkclz.web.bean.EntityResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "消息通知分页响应")
public class MsgNotificationPageResp extends EntityResp {

    @Schema(description = "消息编码")
    private String noticeNo;

    @Schema(description = "通知发送人")
    private String userCode;

    @Schema(description = "通知标题")
    private String title;

    @Schema(description = "扩展URL")
    private String extUrl;

    @Schema(description = "消息发送人数")
    private Integer recordCount;

    @Schema(description = "消息阅读人数")
    private Integer readCount;
}
