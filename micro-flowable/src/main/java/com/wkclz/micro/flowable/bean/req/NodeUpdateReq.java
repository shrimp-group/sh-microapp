package com.wkclz.micro.flowable.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "节点配置更新请求")
public class NodeUpdateReq {
    @NotNull(message = "id 不能为空")
    @Schema(description = "节点配置 ID")
    private Long id;
    @Schema(description = "审批人类型")
    private String assigneeType;
    @Schema(description = "审批人配置值")
    private String assigneeValue;
    @Schema(description = "表单字段权限 JSON")
    private String formFields;
    @Schema(description = "乐观锁版本")
    private Integer version;
}
