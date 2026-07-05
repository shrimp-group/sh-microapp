package com.wkclz.micro.form.bean.req;

import com.wkclz.web.bean.PageReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "表单分页查询请求")
public class MdmFormPageReq extends PageReq {

    @Schema(description = "表单编码【支持模糊查询】")
    private String formCode;

    @Schema(description = "表单名称【支持模糊查询】")
    private String formName;
}
