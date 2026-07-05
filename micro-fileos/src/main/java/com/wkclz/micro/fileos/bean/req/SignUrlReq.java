package com.wkclz.micro.fileos.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "签名URL请求")
public class SignUrlReq implements Serializable {

    @NotBlank(message = "文件存储路径不能为空")
    @Schema(description = "文件存储路径", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fileId;

    @Schema(description = "过期时间（分钟）")
    private Integer expireMinutes;
}
