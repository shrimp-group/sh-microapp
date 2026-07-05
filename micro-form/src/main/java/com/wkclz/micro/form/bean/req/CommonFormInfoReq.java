package com.wkclz.micro.form.bean.req;

import com.wkclz.web.bean.IdReq;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "通用表单详情请求")
public class CommonFormInfoReq extends IdReq {

    @NotBlank(message = "表单编码不能为空")
    @Schema(description = "表单编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String formCode;
}
