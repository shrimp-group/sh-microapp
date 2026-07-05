package com.wkclz.micro.k8s.bean.req;

import com.wkclz.web.bean.RemoveReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "K8s配置删除请求")
public class K8sConfigRemoveReq extends RemoveReq {
}
