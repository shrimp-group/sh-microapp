package com.wkclz.micro.wxapp.bean.req;

import com.wkclz.web.bean.RemoveReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "微信小程序配置删除请求")
public class WxappConfigRemoveReq extends RemoveReq {
}
