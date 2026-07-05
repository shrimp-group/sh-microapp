package com.wkclz.micro.fun.bean.req;

import com.wkclz.web.bean.PageReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "函数体分页查询请求")
public class FunFunctionPageReq extends PageReq {

    @Schema(description = "分类编码")
    private String categoryCode;

    @Schema(description = "函数编码")
    private String funCode;

    @Schema(description = "函数名称")
    private String funName;

    @Schema(description = "函数语言")
    private String funLanguage;

    @Schema(description = "可见1/0")
    private Integer visible;

    @Schema(description = "内置")
    private Integer defaultFlag;
}
