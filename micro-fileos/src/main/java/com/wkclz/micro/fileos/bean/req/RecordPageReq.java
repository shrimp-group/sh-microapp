package com.wkclz.micro.fileos.bean.req;

import com.wkclz.web.bean.PageReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "文件记录分页查询请求")
public class RecordPageReq extends PageReq implements Serializable {

    @Schema(description = "文件存储路径")
    private String fileId;

    @Schema(description = "原始文件名")
    private String fileName;

    @Schema(description = "文件扩展名")
    private String fileType;

    @Schema(description = "业务分类")
    private String category;

    @Schema(description = "所属目录路径")
    private String dirPath;

    @Schema(description = "是否公共读")
    private Integer isPublic;

    @Schema(description = "OSS服务商")
    private String ossSp;

    @Schema(description = "所属Bucket")
    private String bucketName;

    @Schema(description = "上传方式")
    private String uploadType;

    @Schema(description = "上传状态")
    private String uploadStatus;
}
