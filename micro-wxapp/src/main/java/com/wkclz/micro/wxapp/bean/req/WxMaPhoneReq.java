package com.wkclz.micro.wxapp.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "微信小程序获取手机号请求")
public class WxMaPhoneReq implements Serializable {

    @Schema(description = "会话密钥")
    private String sessionKey;

    @Schema(description = "签名")
    private String signature;

    @Schema(description = "原始数据")
    private String rawData;

    @Schema(description = "加密数据")
    private String encryptedData;

    @Schema(description = "加密算法初始向量")
    private String iv;
}
