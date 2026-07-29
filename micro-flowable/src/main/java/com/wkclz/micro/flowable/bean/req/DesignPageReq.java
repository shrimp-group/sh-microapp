package com.wkclz.micro.flowable.bean.req;

import com.wkclz.web.bean.PageReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "流程设计分页查询")
public class DesignPageReq extends PageReq {
    @Schema(description = "流程名称（模糊）")
    private String designName;
    @Schema(description = "流程分类")
    private String category;
    @Schema(description = "状态")
    private String status;
}
