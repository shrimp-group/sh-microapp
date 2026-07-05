package com.wkclz.micro.wxapp.bean.req;

import com.wkclz.micro.wxapp.bean.req.validation.WxMaLoginFieldsValid;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "微信小程序登录请求")
@WxMaLoginFieldsValid
public class WxMaLoginReq implements Serializable {

    @NotBlank(message = "appId不能为空")
    @Pattern(regexp = "^wx[a-fA-F0-9]{16}$", message = "appId 格式错误!")
    @Schema(description = "小程序appId", requiredMode = Schema.RequiredMode.REQUIRED)
    private String appId;

    @NotBlank(message = "code不能为空")
    @Schema(description = "微信登录code", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @Schema(description = "加密数据")
    private String encryptedData;

    @Schema(description = "加密算法初始向量")
    private String iv;

    @Schema(description = "原始数据")
    private String rawData;

    @Schema(description = "签名")
    private String signature;
}
