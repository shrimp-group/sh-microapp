package com.wkclz.micro.k8s.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "K8s Kind资源列表查询请求")
public class K8sKindListReq {

    @NotBlank(message = "集群名称不能为空")
    @Schema(description = "集群名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String clusterName;

    @Schema(description = "命名空间")
    private String namespace;

    @NotBlank(message = "资源类型不能为空")
    @Schema(description = "资源类型(Kind)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String kind;

    @Schema(description = "名称")
    private String name;
}
