package com.wkclz.micro.fun.bean.resp;

import com.wkclz.web.bean.EntityResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "函数体分页响应")
public class FunFunctionPageResp extends EntityResp {

    @Schema(description = "分类编码")
    private String categoryCode;

    @Schema(description = "函数编码")
    private String funCode;

    @Schema(description = "函数名称")
    private String funName;

    @Schema(description = "参数列表")
    private String funParams;

    @Schema(description = "函数语言")
    private String funLanguage;

    @Schema(description = "返回类型")
    private String funReturn;

    @Schema(description = "模拟数据")
    private String funMockData;

    @Schema(description = "可见1/0")
    private Integer visible;

    @Schema(description = "内置")
    private Integer defaultFlag;
}
