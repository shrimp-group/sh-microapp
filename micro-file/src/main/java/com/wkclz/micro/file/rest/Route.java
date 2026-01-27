package com.wkclz.micro.file.rest;


import com.wkclz.core.annotation.Desc;
import com.wkclz.core.annotation.Router;

/**
 * Description:
 * Created: wangkaicun @ 2017-10-19 上午12:46
 */


@Router(module = "micro-file", prefix = Route.PREFIX)
public interface Route {

    String PREFIX = "/micro-fs";


    @Desc("0. common-文件上传")
    String COMMON_UPLOAD = "/common/upload";
    @Desc("0. common-文件上传-开放")
    String COMMON_UPLOAD_PUBLIC = "/common/upload/public";


    @Desc("1. 文件系统-Bucket-分页")
    String BUCKET_PAGE = "/bucket/page";
    @Desc("2. 文件系统-Bucket-详情")
    String BUCKET_INFO = "/bucket/info";
    @Desc("3. 文件系统-Bucket-创建")
    String BUCKET_CREATE = "/bucket/create";
    @Desc("4. 文件系统-Bucket-修改")
    String BUCKET_UPDATE = "/bucket/update";
    @Desc("5. 文件系统-Bucket-删除")
    String BUCKET_REMOVE = "/bucket/remove";
    @Desc("6. 文件系统-Bucket-选项")
    String BUCKET_OPTIONS = "/bucket/options";

    @Desc("6. 文件系统-已上传文件-分页")
    String FILES_PAGE = "/files/page";
    @Desc("7. 文件系统-已上传文件-详情")
    String FILES_INFO = "/files/info";
    @Desc("8. 文件系统-已上传文件-删除")
    String FILES_REMOVE = "/files/remove";

}
