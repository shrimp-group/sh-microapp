package com.wkclz.micro.fileos.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "Bucket选项查询请求")
public class BucketOptionsReq implements Serializable {

    @Schema(description = "OSS服务商标识")
    private String ossSp;
}
