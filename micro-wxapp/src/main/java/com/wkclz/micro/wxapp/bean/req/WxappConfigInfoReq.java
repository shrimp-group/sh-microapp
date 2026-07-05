package com.wkclz.micro.wxapp.bean.req;

import com.wkclz.web.bean.IdReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "微信小程序配置详情查询请求")
public class WxappConfigInfoReq extends IdReq {
}
