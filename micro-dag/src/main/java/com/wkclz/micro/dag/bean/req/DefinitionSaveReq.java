package com.wkclz.micro.dag.bean.req;

import com.wkclz.micro.dag.bean.dto.EdgeDef;
import com.wkclz.micro.dag.bean.dto.NodeDef;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class DefinitionSaveReq {

    @Schema(description = "定义 id，新建传空")
    private Long id;

    @Schema(description = "流程编码")
    private String dagCode;

    @Schema(description = "流程名称")
    private String dagName;

    @Schema(description = "定义节点列表（与 dag_node 行一一对应，含画布坐标 posX/posY）")
    private List<NodeDef> nodes;

    @Schema(description = "定义边列表（与 dag_edge 行一一对应）")
    private List<EdgeDef> edges;
}
