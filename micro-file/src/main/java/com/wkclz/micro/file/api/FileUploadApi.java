package com.wkclz.micro.file.api;

import com.wkclz.micro.file.bean.dto.MdmFileRecordDto;
import org.springframework.web.multipart.MultipartFile;

public interface FileUploadApi {

    MdmFileRecordDto upload(MultipartFile file);

    MdmFileRecordDto upload(MultipartFile file, String businessType);

    MdmFileRecordDto upload(MultipartFile file, String businessType, String bucket);

    MdmFileRecordDto uploadPublic(MultipartFile file);

    MdmFileRecordDto uploadPublic(MultipartFile file, String businessType);

    MdmFileRecordDto uploadPublic(MultipartFile file, String businessType, String bucket);

}
