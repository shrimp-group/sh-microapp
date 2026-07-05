package com.wkclz.micro.k8s.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "K8s配置创建请求")
public class K8sConfigCreateReq {

    @NotBlank(message = "集群名称不能为空")
    @Schema(description = "集群名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String clusterName;

    @NotBlank(message = "kubeConfig不能为空")
    @Schema(description = "kubeConfig配置信息", requiredMode = Schema.RequiredMode.REQUIRED)
    private String kubeConfig;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "备注")
    private String remark;
}
