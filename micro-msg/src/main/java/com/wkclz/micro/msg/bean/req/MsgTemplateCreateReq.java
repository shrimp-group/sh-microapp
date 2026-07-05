package com.wkclz.micro.msg.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "消息模板创建请求")
public class MsgTemplateCreateReq implements Serializable {

    @NotBlank(message = "模板编码不能为空")
    @Schema(description = "模板编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String templateCode;

    @NotBlank(message = "模板名称不能为空")
    @Schema(description = "模板名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String templateName;

    @NotBlank(message = "消息标题不能为空")
    @Schema(description = "消息标题", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "备注")
    private String remark;
}
