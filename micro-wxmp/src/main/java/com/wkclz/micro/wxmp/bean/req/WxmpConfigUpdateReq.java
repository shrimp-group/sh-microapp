package com.wkclz.micro.wxmp.bean.req;

import com.wkclz.web.bean.UpdateReq;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "公众号配置更新请求")
public class WxmpConfigUpdateReq extends UpdateReq {

    @NotBlank(message = "appId 不能为空")
    @Schema(description = "公众号appid")
    private String appId;

    @NotBlank(message = "appSecret 不能为空")
    @Schema(description = "公众号Secret")
    private String appSecret;

    @Schema(description = "公众号回调服务端的token")
    private String mpToken;

    @Schema(description = "公众号回调服务端的AESKey")
    private String aesKey;

    @Schema(description = "证书文件cert")
    private String certPem;

    @Schema(description = "证书文件key")
    private String keyPem;

    @Schema(description = "公众号菜单数据")
    private String mpMenuJson;

    @Schema(description = "公众号-对话-回复映射 (default为默认回复)")
    private String mpTalkReplyMap;

    @Schema(description = "欢迎信息")
    private String welcomeMsg;

    @Schema(description = "备注")
    private String remark;
}
