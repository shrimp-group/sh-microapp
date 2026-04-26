package com.wkclz.micro.file.api.impl;

import com.wkclz.micro.file.api.FileApi;
import com.wkclz.micro.file.api.FileDeleteApi;
import com.wkclz.micro.file.api.FileSignApi;
import com.wkclz.micro.file.api.FileUploadApi;
import com.wkclz.micro.file.bean.dto.MdmFileRecordDto;
import com.wkclz.micro.file.bean.entity.MdmFileRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Deprecated
@Service
public class FileApiImpl implements FileApi {

    @Autowired
    private FileUploadApi fileUploadApi;
    @Autowired
    private FileSignApi fileSignApi;
    @Autowired
    private FileDeleteApi fileDeleteApi;

    @Override
    public MdmFileRecordDto upload(MultipartFile file) {
        return fileUploadApi.upload(file);
    }

    @Override
    public MdmFileRecordDto upload(MultipartFile file, String businessType) {
        return fileUploadApi.upload(file, businessType);
    }

    @Override
    public MdmFileRecordDto upload(MultipartFile file, String businessType, String bucket) {
        return fileUploadApi.upload(file, businessType, bucket);
    }

    @Override
    public MdmFileRecordDto uploadPublic(MultipartFile file) {
        return fileUploadApi.uploadPublic(file);
    }

    @Override
    public MdmFileRecordDto uploadPublic(MultipartFile file, String businessType) {
        return fileUploadApi.uploadPublic(file, businessType);
    }

    @Override
    public MdmFileRecordDto uploadPublic(MultipartFile file, String businessType, String bucket) {
        return fileUploadApi.uploadPublic(file, businessType, bucket);
    }

    @Override
    public <P, R, V> void sign(P fsFile, Function<P, R> getter, BiConsumer<P, V> setter) {
        fileSignApi.sign(fsFile, getter, setter);
    }

    @Override
    public <P, R, V> void sign(List<P> fsFiles, Function<P, R> getter, BiConsumer<P, V> setter) {
        fileSignApi.sign(fsFiles, getter, setter);
    }

    @Override
    public String signContent(String content) {
        return fileSignApi.signContent(content);
    }

    @Override
    public String signs(String fileIds) {
        return fileSignApi.signs(fileIds);
    }

    @Override
    public String sign(String fileId) {
        return fileSignApi.sign(fileId);
    }

    @Override
    public String[] sign(String[] fileIds) {
        return fileSignApi.sign(fileIds);
    }

    @Override
    public String sign(MdmFileRecord fsFile) {
        return fileSignApi.sign(fsFile);
    }

    @Override
    public List<String> sign(List<?> fsFiles) {
        return fileSignApi.sign(fsFiles);
    }

    @Override
    public String sign(String fileId, Integer expire, TimeUnit timeUnit) {
        return fileSignApi.sign(fileId, expire, timeUnit);
    }

    @Override
    public String sign(MdmFileRecord fsFile, Integer expire, TimeUnit timeUnit) {
        return fileSignApi.sign(fsFile, expire, timeUnit);
    }

    @Override
    public List<String> sign(List<?> fsFiles, Integer expire, TimeUnit timeUnit) {
        return fileSignApi.sign(fsFiles, expire, timeUnit);
    }

    @Override
    public Integer delete(String fileId) {
        return fileDeleteApi.delete(fileId);
    }

    @Override
    public Integer delete(List<String> fileIds) {
        return fileDeleteApi.delete(fileIds);
    }

}
