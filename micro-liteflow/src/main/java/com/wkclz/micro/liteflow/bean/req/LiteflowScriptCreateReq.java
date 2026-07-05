package com.wkclz.micro.liteflow.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "脚本创建请求")
public class LiteflowScriptCreateReq implements Serializable {

    @NotBlank(message = "脚本ID不能为空")
    @Schema(description = "脚本ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String scriptId;

    @NotBlank(message = "脚本名称不能为空")
    @Schema(description = "脚本名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String scriptName;

    @Schema(description = "脚本数据")
    private String scriptData;

    @Schema(description = "脚本类型")
    private String scriptType;

    @Schema(description = "脚本语言")
    private String scriptLanguage;

    @Schema(description = "可用状态")
    private Integer enable;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "备注")
    private String remark;
}
