package com.wkclz.micro.file.service;


import com.wkclz.micro.file.bean.dto.MdmFileRecordDto;
import com.wkclz.micro.file.bean.entity.MdmFileBucket;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.TimeUnit;

public interface FileService {

    MdmFileRecordDto upload(MultipartFile file, MdmFileBucket bucket, String businessType);
    MdmFileRecordDto uploadPublic(MultipartFile file, MdmFileBucket bucket, String businessType);

    String sign(String file, MdmFileBucket bucket, Integer expire, TimeUnit timeUnit);
    List<String> sign(List<String> files, MdmFileBucket bucket, Integer expire, TimeUnit timeUnit);

    Integer delete(List<String> files, MdmFileBucket bucket);
}
