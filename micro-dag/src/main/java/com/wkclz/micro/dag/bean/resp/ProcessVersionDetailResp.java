package com.wkclz.micro.dag.bean.resp;

import com.wkclz.micro.dag.bean.dto.EdgeDef;
import com.wkclz.micro.dag.bean.dto.NodeDef;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/** 流程版本详情响应（编辑加载与历史只读查看共用） */
@Schema(description = "流程版本详情响应（编辑加载与历史只读查看共用）")
@Data
public class ProcessVersionDetailResp {

    @Schema(description = "版本 id")
    private Long id;

    @Schema(description = "流程编码")
    private String dagCode;

    @Schema(description = "流程名称（主表当前值）")
    private String dagName;

    @Schema(description = "版本号")
    private Integer dagVersion;

    @Schema(description = "状态：DRAFT/PUBLISHED")
    private String status;

    @Schema(description = "节点列表")
    private List<NodeDef> nodes;

    @Schema(description = "边列表")
    private List<EdgeDef> edges;
}
