package com.wkclz.micro.liteflow.bean.req;

import com.wkclz.web.bean.PageReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "脚本分页查询请求")
public class LiteflowScriptPageReq extends PageReq {

    @Schema(description = "脚本ID")
    private String scriptId;

    @Schema(description = "脚本名称")
    private String scriptName;

    @Schema(description = "脚本类型")
    private String scriptType;

    @Schema(description = "脚本语言")
    private String scriptLanguage;

    @Schema(description = "可用状态")
    private Integer enable;
}
