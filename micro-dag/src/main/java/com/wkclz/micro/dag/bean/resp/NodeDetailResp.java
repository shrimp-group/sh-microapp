package com.wkclz.micro.dag.bean.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class NodeDetailResp {

    @Schema(description = "节点实例信息")
    private NodeInstanceResp node;

    @Schema(description = "执行记录时间线")
    private List<ExecutionLogResp> logs;
}
