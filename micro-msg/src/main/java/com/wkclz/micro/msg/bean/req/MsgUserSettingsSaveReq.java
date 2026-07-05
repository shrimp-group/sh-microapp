package com.wkclz.micro.msg.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "用户消息设置保存请求")
public class MsgUserSettingsSaveReq implements Serializable {

    @Schema(description = "事件消息配置(JSON字符串)")
    private String notifyEvent;

    @Schema(description = "系统消息配置(JSON字符串)")
    private String notifySystem;
}
