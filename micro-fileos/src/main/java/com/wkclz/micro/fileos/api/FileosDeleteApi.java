package com.wkclz.micro.fileos.api;

import java.util.List;

public interface FileosDeleteApi {

    Integer delete(String fileId);

    Integer delete(List<String> fileIds);

}
