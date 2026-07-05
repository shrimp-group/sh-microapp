package com.wkclz.micro.k8s.bean.resp;

import com.wkclz.web.bean.EntityResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "K8s配置响应")
public class K8sConfigResp extends EntityResp {

    @Schema(description = "集群名称")
    private String clusterName;

    @Schema(description = "kubeConfig配置信息")
    private String kubeConfig;
}
