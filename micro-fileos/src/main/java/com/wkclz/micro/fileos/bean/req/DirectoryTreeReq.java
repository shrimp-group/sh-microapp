package com.wkclz.micro.fileos.bean.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "目录树查询请求")
public class DirectoryTreeReq implements Serializable {

    @Schema(description = "所属Bucket")
    private String bucketName;
}
