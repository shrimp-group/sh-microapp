package com.wkclz.micro.msg.bean.resp;

import com.wkclz.web.bean.EntityResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户消息设置响应")
public class MsgUserSettingsResp extends EntityResp {

    @Schema(description = "事件消息配置(JSON)")
    private String notifyEvent;

    @Schema(description = "系统消息配置(JSON)")
    private String notifySystem;
}
