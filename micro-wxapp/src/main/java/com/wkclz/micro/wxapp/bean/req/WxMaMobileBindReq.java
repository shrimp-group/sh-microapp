package com.wkclz.micro.wxapp.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "微信小程序手机号绑定请求")
public class WxMaMobileBindReq implements Serializable {

    @NotBlank(message = "code不能为空")
    @Schema(description = "微信getPhoneNumber返回的code", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @NotBlank(message = "appId不能为空")
    @Schema(description = "小程序appId", requiredMode = Schema.RequiredMode.REQUIRED)
    private String appId;
}
