package com.wkclz.micro.msg.bean.req;

import com.wkclz.web.bean.PageReq;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "消息通知阅读记录分页查询请求")
public class MsgNotificationRecordPageReq extends PageReq {

    @NotBlank(message = "消息编码不能为空")
    @Schema(description = "消息编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String noticeNo;

    @Schema(description = "接收人用户名")
    private String userCode;
}
