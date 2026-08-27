package com.wkclz.micro.flowable.bean.entity;

import com.wkclz.core.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "节点配置")
public class FlowableNodeConfig extends BaseEntity {

    @Schema(description = "关联 process_design.id")
    private Long designId;

    @Schema(description = "节点 ID（BPMN taskDefKey）")
    private String nodeKey;

    @Schema(description = "节点名称")
    private String nodeName;

    @Schema(description = "节点类型：START/APPROVAL/CC/GATEWAY/END")
    private String nodeType;

    @Schema(description = "审批人类型：USER/ROLE/DEPT/STARTER/SCRIPT")
    private String assigneeType;

    @Schema(description = "审批人配置值")
    private String assigneeValue;

    @Schema(description = "表单字段权限 JSON")
    private String formFields;

    @Schema(description = "节点顺序")
    private Integer orderNum;

    @Schema(description = "租户编码")
    private String tenantCode;
}
