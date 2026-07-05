package com.wkclz.micro.wxmp.bean.resp;

import com.wkclz.web.bean.EntityResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "公众号配置详情响应")
public class WxmpConfigResp extends EntityResp {

    @Schema(description = "租户编码")
    private String tenantCode;

    @Schema(description = "公众号appid")
    private String appId;

    @Schema(description = "公众号Secret")
    private String appSecret;

    @Schema(description = "证书文件cert")
    private String certPem;

    @Schema(description = "证书文件key")
    private String keyPem;

    @Schema(description = "公众号回调服务端的token")
    private String mpToken;

    @Schema(description = "公众号回调服务端的AESKey")
    private String aesKey;

    @Schema(description = "公众号菜单数据")
    private String mpMenuJson;

    @Schema(description = "公众号-对话-回复映射 (default为默认回复)")
    private String mpTalkReplyMap;

    @Schema(description = "欢迎信息")
    private String welcomeMsg;
}
