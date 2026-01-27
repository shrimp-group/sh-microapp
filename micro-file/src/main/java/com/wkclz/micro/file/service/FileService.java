package com.wkclz.micro.file.service;


import com.wkclz.micro.file.pojo.dto.MdmFsFilesDto;
import com.wkclz.micro.file.pojo.entity.MdmFsBucket;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.TimeUnit;

public interface FileService {

    MdmFsFilesDto upload(MultipartFile file, MdmFsBucket fsBucket, String businessType);
    MdmFsFilesDto uploadPublic(MultipartFile file, MdmFsBucket fsBucket, String businessType);

    String sign(String file, MdmFsBucket fsBucket, Integer expire, TimeUnit timeUnit);
    List<String> sign(List<String> files, MdmFsBucket fsBucket, Integer expire, TimeUnit timeUnit);

    Integer delete(List<String> files, MdmFsBucket fsBucket);
}
