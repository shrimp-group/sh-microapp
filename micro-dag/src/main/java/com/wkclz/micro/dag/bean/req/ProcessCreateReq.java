package com.wkclz.micro.dag.bean.req;

import com.wkclz.micro.dag.bean.dto.EdgeDef;
import com.wkclz.micro.dag.bean.dto.NodeDef;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/** 新建流程请求 */
@Data
public class ProcessCreateReq {

    @Schema(description = "流程名称")
    private String dagName;

    @Schema(description = "节点列表")
    private List<NodeDef> nodes;

    @Schema(description = "边列表")
    private List<EdgeDef> edges;
}
