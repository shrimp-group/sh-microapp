package com.wkclz.micro.material.bean.req;

import com.wkclz.web.bean.RemoveReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "素材删除请求")
public class MaterialRemoveReq extends RemoveReq {
}
