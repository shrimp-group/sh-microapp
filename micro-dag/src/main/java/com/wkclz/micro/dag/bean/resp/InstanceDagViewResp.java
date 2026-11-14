package com.wkclz.micro.dag.bean.resp;

import com.wkclz.micro.dag.bean.dto.EdgeDef;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class InstanceDagViewResp {

    @Schema(description = "实例信息")
    private InstanceResp instance;

    @Schema(description = "节点实例状态列表")
    private List<NodeInstanceResp> nodes;

    @Schema(description = "定义快照的边列表（画布连线用）")
    private List<EdgeDef> edges;

    @Schema(description = "流程上下文（分支渲染用）")
    private Map<String, Object> ctx;

    @Schema(description = "节点输出 ${out.<nodeCode>.*}")
    private Map<String, Map<String, Object>> nodeOutputs;
}
