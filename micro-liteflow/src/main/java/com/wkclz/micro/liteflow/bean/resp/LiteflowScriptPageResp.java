package com.wkclz.micro.liteflow.bean.resp;

import com.wkclz.web.bean.EntityResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "脚本分页响应")
public class LiteflowScriptPageResp extends EntityResp {

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
