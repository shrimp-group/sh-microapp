package com.wkclz.micro.dag.bean.req;

import com.wkclz.micro.dag.bean.dto.EdgeDef;
import com.wkclz.micro.dag.bean.dto.NodeDef;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/** 保存草稿请求 */
@Data
public class ProcessVersionUpdateReq {

    @Schema(description = "草稿版本 id")
    private Long id;

    @Schema(description = "流程名称")
    private String dagName;

    @Schema(description = "节点列表")
    private List<NodeDef> nodes;

    @Schema(description = "边列表")
    private List<EdgeDef> edges;
}
