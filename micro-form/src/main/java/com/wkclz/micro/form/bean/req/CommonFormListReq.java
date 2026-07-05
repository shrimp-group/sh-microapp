package com.wkclz.micro.form.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "通用表单列表请求")
public class CommonFormListReq implements Serializable {

    @Schema(description = "表单编码")
    private String formCode;

    @Schema(description = "表单名称")
    private String formName;
}
