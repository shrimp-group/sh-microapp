package com.wkclz.micro.fileos.bean.req;

import com.wkclz.web.bean.PageReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Bucket分页查询请求")
public class BucketPageReq extends PageReq implements Serializable {

    @Schema(description = "Bucket名称")
    private String bucketName;

    @Schema(description = "OSS服务商标识")
    private String ossSp;

    @Schema(description = "默认标识")
    private Integer defaultFlag;
}
