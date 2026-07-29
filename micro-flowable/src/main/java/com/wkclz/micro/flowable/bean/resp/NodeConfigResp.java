package com.wkclz.micro.flowable.bean.resp;

import com.wkclz.web.bean.EntityResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "节点配置响应")
public class NodeConfigResp extends EntityResp {
    @Schema(description = "设计 ID")
    private Long designId;
    @Schema(description = "节点 key")
    private String nodeKey;
    @Schema(description = "节点名称")
    private String nodeName;
    @Schema(description = "节点类型")
    private String nodeType;
    @Schema(description = "审批人类型")
    private String assigneeType;
    @Schema(description = "审批人配置值")
    private String assigneeValue;
    @Schema(description = "表单字段权限 JSON")
    private String formFields;
    @Schema(description = "节点顺序")
    private Integer orderNum;
}
