package com.wkclz.micro.file.api;

import java.util.List;

public interface FileDeleteApi {

    Integer delete(String fileId);

    Integer delete(List<String> fileIds);

}
