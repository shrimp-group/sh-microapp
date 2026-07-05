package com.wkclz.micro.liteflow.bean.req;

import com.wkclz.web.bean.IdReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "脚本详情查询请求")
public class LiteflowScriptInfoReq extends IdReq {
}
