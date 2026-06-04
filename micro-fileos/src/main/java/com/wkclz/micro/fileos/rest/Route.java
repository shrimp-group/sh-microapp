package com.wkclz.micro.fileos.rest;

import com.wkclz.core.annotation.ApiDesc;
import com.wkclz.core.annotation.Router;

@Router(module = "micro-fileos", prefix = Route.PREFIX)
public interface Route {

    String PREFIX = "/micro-fileos";

    @ApiDesc("1. Bucket-分页")
    String BUCKET_PAGE = "/bucket/page";
    @ApiDesc("2. Bucket-详情")
    String BUCKET_INFO = "/bucket/info";
    @ApiDesc("3. Bucket-创建")
    String BUCKET_CREATE = "/bucket/create";
    @ApiDesc("4. Bucket-修改")
    String BUCKET_UPDATE = "/bucket/update";
    @ApiDesc("5. Bucket-删除")
    String BUCKET_REMOVE = "/bucket/remove";
    @ApiDesc("6. Bucket-选项")
    String BUCKET_OPTIONS = "/bucket/options";

    @ApiDesc("7. 目录-列表")
    String DIRECTORY_LIST = "/directory/list";
    @ApiDesc("8. 目录-树")
    String DIRECTORY_TREE = "/directory/tree";
    @ApiDesc("9. 目录-详情")
    String DIRECTORY_INFO = "/directory/info";

    @ApiDesc("10. 上传-简单上传")
    String UPLOAD_SIMPLE = "/upload/simple";
    @ApiDesc("11. 上传-简单上传-公开")
    String UPLOAD_SIMPLE_PUBLIC = "/upload/simple/public";
    @ApiDesc("12. 上传-分片上传-初始化")
    String UPLOAD_MULTIPART_INIT = "/upload/multipart/init";
    @ApiDesc("13. 上传-分片上传-完成")
    String UPLOAD_MULTIPART_COMPLETE = "/upload/multipart/complete";
    @ApiDesc("14. 上传-分片上传-中止")
    String UPLOAD_MULTIPART_ABORT = "/upload/multipart/abort";

    @ApiDesc("15. 下载")
    String DOWNLOAD = "/download/{fileId}";

    @ApiDesc("16. 预签名-简单上传")
    String PRESIGN_UPLOAD = "/presign/upload";
    @ApiDesc("17. 预签名-批量简单上传")
    String PRESIGN_UPLOAD_BATCH = "/presign/upload/batch";
    @ApiDesc("18. 预签名-分片上传-初始化")
    String PRESIGN_MULTIPART_INIT = "/presign/multipart/init";
    @ApiDesc("19. 预签名-分片上传-完成")
    String PRESIGN_MULTIPART_COMPLETE = "/presign/multipart/complete";
    @ApiDesc("20. 预签名-分片上传-中止")
    String PRESIGN_MULTIPART_ABORT = "/presign/multipart/abort";
    @ApiDesc("21. 预签名-简单上传-完成确认")
    String PRESIGN_COMPLETE = "/presign/complete";
    @ApiDesc("22. 预签名-简单上传-完成确认-批量")
    String PRESIGN_COMPLETE_BATCH = "/presign/complete/batch";

    @ApiDesc("23. 签名-单文件")
    String SIGN_URL = "/sign/url";
    @ApiDesc("24. 签名-多文件")
    String SIGN_URLS = "/sign/urls";

    @ApiDesc("25. 文件记录-分页")
    String RECORD_PAGE = "/record/page";
    @ApiDesc("26. 文件记录-详情")
    String RECORD_INFO = "/record/info";
    @ApiDesc("27. 文件记录-删除")
    String RECORD_REMOVE = "/record/remove";

}
