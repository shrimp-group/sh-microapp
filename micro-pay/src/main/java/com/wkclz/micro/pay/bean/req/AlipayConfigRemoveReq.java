package com.wkclz.micro.pay.bean.req;

import com.wkclz.web.bean.RemoveReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "支付宝配置删除请求")
public class AlipayConfigRemoveReq extends RemoveReq {
}
