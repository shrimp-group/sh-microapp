package com.wkclz.micro.msg.bean.resp;

import com.wkclz.web.bean.EntityResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户消息记录响应")
public class MsgUserRecordResp extends EntityResp {

    @Schema(description = "用户编码")
    private String userCode;

    @Schema(description = "消息编码")
    private String noticeNo;

    @Schema(description = "阅读状态")
    private Integer readStatus;

    @Schema(description = "阅读时间")
    private LocalDateTime readTime;

    @Schema(description = "发送人用户编码")
    private String sender;

    @Schema(description = "通知标题")
    private String title;

    @Schema(description = "通知正文")
    private String content;

    @Schema(description = "扩展URL")
    private String extUrl;
}
