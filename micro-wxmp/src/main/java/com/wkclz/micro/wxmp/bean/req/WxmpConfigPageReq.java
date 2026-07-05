package com.wkclz.micro.wxmp.bean.req;

import com.wkclz.web.bean.PageReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "公众号配置分页查询请求")
public class WxmpConfigPageReq extends PageReq {

    @Schema(description = "公众号appid")
    private String appId;
}
