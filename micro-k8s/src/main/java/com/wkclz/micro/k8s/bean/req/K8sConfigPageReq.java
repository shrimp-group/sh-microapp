package com.wkclz.micro.k8s.bean.req;

import com.wkclz.web.bean.PageReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "K8s配置分页查询请求")
public class K8sConfigPageReq extends PageReq {

    @Schema(description = "集群名称")
    private String clusterName;
}
