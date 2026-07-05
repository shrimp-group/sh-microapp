package com.wkclz.micro.msg.bean.resp;

import com.wkclz.web.bean.EntityResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "消息通知阅读记录分页响应")
public class MsgNotificationRecordPageResp extends EntityResp {

    @Schema(description = "用户名")
    private String userCode;

    @Schema(description = "消息编码")
    private String noticeNo;

    @Schema(description = "阅读状态")
    private Integer readStatus;

    @Schema(description = "阅读时间")
    private LocalDateTime readTime;

    @Schema(description = "消息展示次数")
    private Integer showTimes;
}
