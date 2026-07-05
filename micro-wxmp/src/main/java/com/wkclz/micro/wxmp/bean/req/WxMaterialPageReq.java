package com.wkclz.micro.wxmp.bean.req;

import com.wkclz.web.bean.PageReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "微信素材分页查询请求")
public class WxMaterialPageReq extends PageReq {

    @Schema(description = "公众号appid")
    private String appId;
}
