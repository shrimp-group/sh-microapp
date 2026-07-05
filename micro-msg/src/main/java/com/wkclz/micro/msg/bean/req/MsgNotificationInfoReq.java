package com.wkclz.micro.msg.bean.req;

import com.wkclz.web.bean.IdReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "消息通知详情查询请求")
public class MsgNotificationInfoReq extends IdReq {
}
