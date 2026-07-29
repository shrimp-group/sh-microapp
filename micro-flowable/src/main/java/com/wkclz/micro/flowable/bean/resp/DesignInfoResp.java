package com.wkclz.micro.flowable.bean.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "设计详情响应")
public class DesignInfoResp extends DesignPageResp {
    @Schema(description = "BPMN XML 内容")
    private String xmlContent;
    @Schema(description = "节点配置列表")
    private List<NodeConfigResp> nodes;
}
