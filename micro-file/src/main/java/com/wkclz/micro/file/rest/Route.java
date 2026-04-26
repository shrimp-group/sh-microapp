package com.wkclz.micro.file.rest;


import com.wkclz.core.annotation.ApiDesc;
import com.wkclz.core.annotation.Router;

/**
 * ApiDescription:
 * Created: wangkaicun @ 2017-10-19 上午12:46
 */


@Router(module = "micro-file", prefix = Route.PREFIX)
public interface Route {

    String PREFIX = "/micro-file";


    @ApiDesc("0. common-文件上传")
    String COMMON_UPLOAD = "/common/upload";
    @ApiDesc("0. common-文件上传-开放")
    String COMMON_UPLOAD_PUBLIC = "/common/upload/public";


    @ApiDesc("1. 文件系统-Bucket-分页")
    String BUCKET_PAGE = "/bucket/page";
    @ApiDesc("2. 文件系统-Bucket-详情")
    String BUCKET_INFO = "/bucket/info";
    @ApiDesc("3. 文件系统-Bucket-创建")
    String BUCKET_CREATE = "/bucket/create";
    @ApiDesc("4. 文件系统-Bucket-修改")
    String BUCKET_UPDATE = "/bucket/update";
    @ApiDesc("5. 文件系统-Bucket-删除")
    String BUCKET_REMOVE = "/bucket/remove";
    @ApiDesc("6. 文件系统-Bucket-选项")
    String BUCKET_OPTIONS = "/bucket/options";

    @ApiDesc("6. 文件系统-已上传文件-分页")
    String RECORD_PAGE = "/record/page";
    @ApiDesc("7. 文件系统-已上传文件-详情")
    String RECORD_INFO = "/record/info";
    @ApiDesc("8. 文件系统-已上传文件-删除")
    String RECORD_REMOVE = "/record/remove";

}
