package com.wkclz.micro.wxapp.bean.req;

import com.wkclz.web.bean.PageReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "微信小程序配置分页查询请求")
public class WxappConfigPageReq extends PageReq {

    @Schema(description = "租户编码")
    private String tenantCode;

    @Schema(description = "小程序appId")
    private String appId;
}
