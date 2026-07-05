package com.wkclz.micro.fun.bean.req;

import com.wkclz.web.bean.UpdateReq;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "函数体修改请求")
public class FunFunctionUpdateReq extends UpdateReq {

    @NotBlank(message = "categoryCode不能为空")
    @Schema(description = "分类编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String categoryCode;

    @NotBlank(message = "funCode不能为空")
    @Schema(description = "函数编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String funCode;

    @NotBlank(message = "funName不能为空")
    @Schema(description = "函数名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String funName;

    @Schema(description = "参数列表")
    private String funParams;

    @NotBlank(message = "funLanguage不能为空")
    @Schema(description = "函数语言", requiredMode = Schema.RequiredMode.REQUIRED)
    private String funLanguage;

    @NotBlank(message = "funBody不能为空")
    @Schema(description = "函数体", requiredMode = Schema.RequiredMode.REQUIRED)
    private String funBody;

    @NotBlank(message = "funReturn不能为空")
    @Schema(description = "返回类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private String funReturn;

    @Schema(description = "函数说明")
    private String funDesc;

    @Schema(description = "模拟数据")
    private String funMockData;

    @Schema(description = "可见1/0")
    private Integer visible;

    @Schema(description = "内置")
    private Integer defaultFlag;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "备注")
    private String remark;
}
