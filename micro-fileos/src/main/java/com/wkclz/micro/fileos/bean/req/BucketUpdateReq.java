package com.wkclz.micro.fileos.bean.req;

import com.wkclz.web.bean.UpdateReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Bucket更新请求")
public class BucketUpdateReq extends UpdateReq implements Serializable {

    @Schema(description = "Bucket名称")
    private String bucketName;

    @Schema(description = "OSS服务商标识")
    private String ossSp;

    @Schema(description = "内网Endpoint")
    private String endpointInner;

    @Schema(description = "外网Endpoint")
    private String endpointOuter;

    @Schema(description = "区域")
    private String region;

    @Schema(description = "Access Key")
    private String accessKey;

    @Schema(description = "Secret Key")
    private String secretKey;

    @Schema(description = "默认标识")
    private Integer defaultFlag;

    @Schema(description = "路径中的system段")
    private String system;

    @Schema(description = "备注")
    private String remark;
}
