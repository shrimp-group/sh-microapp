package com.wkclz.micro.msg.bean.req;

import com.wkclz.web.bean.PageReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "消息通知分页查询请求")
public class MsgNotificationPageReq extends PageReq {

    @Schema(description = "消息编码")
    private String noticeNo;

    @Schema(description = "通知发送人")
    private String userCode;

    @Schema(description = "通知标题(模糊搜索)")
    private String title;
}
