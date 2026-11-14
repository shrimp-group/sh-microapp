package com.wkclz.micro.dag.bean.resp;

import com.wkclz.micro.dag.bean.dto.EdgeDef;
import com.wkclz.micro.dag.bean.dto.NodeDef;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DefinitionResp {

    @Schema(description = "定义 id")
    private Long id;

    @Schema(description = "流程编码")
    private String dagCode;

    @Schema(description = "流程名称")
    private String dagName;

    @Schema(description = "定义版本")
    private Integer dagVersion;

    @Schema(description = "定义节点列表（含画布坐标 posX/posY）")
    private List<NodeDef> nodes;

    @Schema(description = "定义边列表")
    private List<EdgeDef> edges;

    @Schema(description = "状态 DRAFT/PUBLISHED/OFFLINE")
    private String status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "修改时间")
    private LocalDateTime updateTime;
}
