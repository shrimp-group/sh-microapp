package com.wkclz.micro.fun.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "函数体测试请求")
public class FunFunctionTestReq implements Serializable {

    @NotBlank(message = "funCode不能为空")
    @Schema(description = "函数编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String funCode;

    @NotBlank(message = "funName不能为空")
    @Schema(description = "函数名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String funName;

    @NotBlank(message = "funLanguage不能为空")
    @Schema(description = "函数语言", requiredMode = Schema.RequiredMode.REQUIRED)
    private String funLanguage;

    @NotBlank(message = "funBody不能为空")
    @Schema(description = "函数体", requiredMode = Schema.RequiredMode.REQUIRED)
    private String funBody;

    @NotBlank(message = "funReturn不能为空")
    @Schema(description = "返回类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private String funReturn;

    @Schema(description = "参数列表")
    private String funParams;

    @Schema(description = "测试参数")
    private String param;
}
