package com.wkclz.micro.file.api.impl;

import com.wkclz.micro.file.api.FileUploadApi;
import com.wkclz.micro.file.bean.dto.MdmFileRecordDto;
import com.wkclz.micro.file.bean.entity.MdmFileBucket;
import com.wkclz.micro.file.service.FileService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileUploadApiImpl extends AbstractFileApi implements FileUploadApi {

    @Override
    public MdmFileRecordDto upload(MultipartFile file) {
        return upload(file, null, null);
    }

    @Override
    public MdmFileRecordDto upload(MultipartFile file, String businessType) {
        return upload(file, businessType, null);
    }

    @Override
    public MdmFileRecordDto upload(MultipartFile file, String businessType, String bucket) {
        businessType = getBusinessType(businessType);
        MdmFileBucket fsBucket = getBucket(bucket);
        FileService service = getApi(fsBucket);
        return service.upload(file, fsBucket, businessType);
    }

    @Override
    public MdmFileRecordDto uploadPublic(MultipartFile file) {
        return uploadPublic(file, null, null);
    }

    @Override
    public MdmFileRecordDto uploadPublic(MultipartFile file, String businessType) {
        return uploadPublic(file, businessType, null);
    }

    @Override
    public MdmFileRecordDto uploadPublic(MultipartFile file, String businessType, String bucket) {
        businessType = getBusinessType(businessType);
        MdmFileBucket fsBucket = getBucket(bucket);
        FileService service = getApi(fsBucket);
        return service.uploadPublic(file, fsBucket, businessType);
    }

}
