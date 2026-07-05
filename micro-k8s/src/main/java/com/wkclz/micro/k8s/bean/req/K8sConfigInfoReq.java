package com.wkclz.micro.k8s.bean.req;

import com.wkclz.web.bean.IdReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "K8s配置详情查询请求")
public class K8sConfigInfoReq extends IdReq {
}
