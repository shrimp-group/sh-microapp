package com.wkclz.micro.audit.bean.req;

import com.wkclz.web.bean.IdReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "变更记录详情查询请求")
public class ChangeLogInfoReq extends IdReq {
}
