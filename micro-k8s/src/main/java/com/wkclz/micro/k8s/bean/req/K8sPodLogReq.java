package com.wkclz.micro.k8s.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "K8s Pod日志请求")
public class K8sPodLogReq {

    @NotBlank(message = "集群名称不能为空")
    @Schema(description = "集群名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String clusterName;

    @NotBlank(message = "命名空间不能为空")
    @Schema(description = "命名空间", requiredMode = Schema.RequiredMode.REQUIRED)
    private String namespace;

    @NotBlank(message = "Pod名称不能为空")
    @Schema(description = "Pod名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "容器名称，为空时取第一个容器")
    private String containerName;

    @Schema(description = "日志行数")
    private Integer tailLines;

    @Schema(description = "是否显示时间戳")
    private Boolean timestamps;

    @Schema(description = "仅显示最近多少秒内的日志")
    private Integer sinceSeconds;
}
