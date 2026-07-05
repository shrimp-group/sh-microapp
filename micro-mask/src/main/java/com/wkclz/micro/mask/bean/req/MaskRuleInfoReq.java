package com.wkclz.micro.mask.bean.req;

import com.wkclz.web.bean.IdReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "脱敏规则详情查询请求")
public class MaskRuleInfoReq extends IdReq {
}
