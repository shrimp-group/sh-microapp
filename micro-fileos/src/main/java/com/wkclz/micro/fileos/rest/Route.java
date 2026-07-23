package com.wkclz.micro.fileos.rest;

import com.wkclz.core.annotation.Router;

@Router(module = "micro-fileos", prefix = Route.PREFIX)
public interface Route {

    String PREFIX = "/micro-fileos";

    String BUCKET_PAGE = "/bucket/page";
    String BUCKET_INFO = "/bucket/info";
    String BUCKET_CREATE = "/bucket/create";
    String BUCKET_UPDATE = "/bucket/update";
    String BUCKET_REMOVE = "/bucket/remove";
    String BUCKET_OPTIONS = "/bucket/options";

    String DIRECTORY_LIST = "/directory/list";
    String DIRECTORY_TREE = "/directory/tree";
    String DIRECTORY_INFO = "/directory/info";

    String UPLOAD_SIMPLE = "/upload/simple";
    String UPLOAD_SIMPLE_PUBLIC = "/upload/simple/public";
    String UPLOAD_MULTIPART_INIT = "/upload/multipart/init";
    String UPLOAD_MULTIPART_COMPLETE = "/upload/multipart/complete";
    String UPLOAD_MULTIPART_ABORT = "/upload/multipart/abort";

    String DOWNLOAD = "/download/{fileId}";

    String PRESIGN_UPLOAD = "/presign/upload";
    String PRESIGN_UPLOAD_BATCH = "/presign/upload/batch";
    String PRESIGN_MULTIPART_INIT = "/presign/multipart/init";
    String PRESIGN_MULTIPART_COMPLETE = "/presign/multipart/complete";
    String PRESIGN_MULTIPART_ABORT = "/presign/multipart/abort";
    String PRESIGN_COMPLETE = "/presign/complete";
    String PRESIGN_COMPLETE_BATCH = "/presign/complete/batch";

    String SIGN_URL = "/sign/url";
    String SIGN_URLS = "/sign/urls";

    String RECORD_PAGE = "/record/page";
    String RECORD_INFO = "/record/info";
    String RECORD_REMOVE = "/record/remove";

}
