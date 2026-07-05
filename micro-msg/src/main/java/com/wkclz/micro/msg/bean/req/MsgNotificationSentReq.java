package com.wkclz.micro.msg.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "消息通知发布请求")
public class MsgNotificationSentReq implements Serializable {

    @NotBlank(message = "通知标题不能为空")
    @Schema(description = "通知标题", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @NotBlank(message = "通知正文不能为空")
    @Schema(description = "通知正文", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    @Schema(description = "扩展URL")
    private String extUrl;

    @Schema(description = "发送目标(单个用户)")
    private String sentToUser;

    @Schema(description = "发送目标(多个用户)")
    private List<String> sentToUsers;
}
