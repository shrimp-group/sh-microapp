package com.wkclz.micro.dag.bean.resp;

import com.wkclz.micro.dag.bean.dto.NodeParamDef;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class HandlerInfoResp {

    @Schema(description = "handler 编码")
    private String code;

    @Schema(description = "handler 名称")
    private String name;

    @Schema(description = "节点分类 SYSTEM/DEMO/BUSINESS")
    private String category;

    @Schema(description = "参数定义列表（前端按此渲染参数表单，空=未声明）")
    private List<NodeParamDef> paramSchema;
}
