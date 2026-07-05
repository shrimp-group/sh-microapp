package com.wkclz.micro.fileos.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "批量签名URL请求")
public class SignUrlsReq implements Serializable {

    @NotEmpty(message = "文件存储路径列表不能为空")
    @Schema(description = "文件存储路径列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> fileIds;

    @Schema(description = "过期时间（分钟）")
    private Integer expireMinutes;
}
