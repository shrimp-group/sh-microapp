package com.wkclz.micro.form.bean.resp;

import com.wkclz.web.bean.EntityResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "表单分页响应")
public class MdmFormPageResp extends EntityResp {

    @Schema(description = "表单编码")
    private String formCode;

    @Schema(description = "表单名称")
    private String formName;

    @Schema(description = "表单项数量")
    private Integer itemCount;
}
