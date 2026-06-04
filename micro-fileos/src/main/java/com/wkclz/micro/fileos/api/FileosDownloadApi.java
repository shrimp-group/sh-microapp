package com.wkclz.micro.fileos.api;

import java.io.InputStream;

public interface FileosDownloadApi {

    InputStream download(String fileId);

    InputStream download(String fileId, long offset, long length);

}
