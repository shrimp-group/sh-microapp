package com.wkclz.micro.flowable.bean.resp;

import com.wkclz.web.bean.EntityResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "设计分页响应")
public class DesignPageResp extends EntityResp {
    @Schema(description = "设计编码")
    private String designCode;
    @Schema(description = "流程名称")
    private String designName;
    @Schema(description = "流程分类")
    private String category;
    @Schema(description = "关联表单 key")
    private String formKey;
    @Schema(description = "设计版本")
    private Integer designVersion;
    @Schema(description = "状态")
    private String status;
    @Schema(description = "最近部署 ID")
    private String deployId;
    @Schema(description = "最近流程定义 ID")
    private String procDefId;
}
