package com.wkclz.micro.form.bean.resp;

import com.wkclz.web.bean.EntityResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "通用表单响应")
public class CommonFormResp extends EntityResp {

    @Schema(description = "表单编码")
    private String formCode;

    @Schema(description = "表单名称")
    private String formName;

    @Schema(description = "表单项列表")
    private List<MdmFormResp.MdmFormItemResp> items;
}
