package com.wkclz.micro.liteflow.bean.req;

import com.wkclz.web.bean.PageReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "规则链分页查询请求")
public class LiteflowChainPageReq extends PageReq {

    @Schema(description = "规则名称")
    private String chainName;

    @Schema(description = "规则描述")
    private String chainDesc;

    @Schema(description = "命名空间")
    private String namespace;

    @Schema(description = "状态")
    private Integer enable;
}
