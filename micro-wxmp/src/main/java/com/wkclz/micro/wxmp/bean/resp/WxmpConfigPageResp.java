package com.wkclz.micro.wxmp.bean.resp;

import com.wkclz.web.bean.EntityResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "公众号配置分页响应")
public class WxmpConfigPageResp extends EntityResp {

    @Schema(description = "租户编码")
    private String tenantCode;

    @Schema(description = "公众号appid")
    private String appId;

    @Schema(description = "公众号回调服务端的token")
    private String mpToken;

    @Schema(description = "公众号回调服务端的AESKey")
    private String aesKey;

    @Schema(description = "欢迎信息")
    private String welcomeMsg;
}
