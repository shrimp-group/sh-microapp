package com.wkclz.micro.k8s.bean.req;

import com.wkclz.web.bean.UpdateReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "K8s配置更新请求")
public class K8sConfigUpdateReq extends UpdateReq {

    @Schema(description = "集群名称")
    private String clusterName;

    @Schema(description = "kubeConfig配置信息")
    private String kubeConfig;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "备注")
    private String remark;
}
