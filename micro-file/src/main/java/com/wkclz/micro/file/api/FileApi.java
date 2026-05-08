package com.wkclz.micro.file.api;

import com.wkclz.micro.file.bean.dto.MdmFileRecordDto;
import com.wkclz.micro.file.bean.entity.MdmFileRecord;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Deprecated
public interface FileApi {

    MdmFileRecordDto upload(MultipartFile file);

    MdmFileRecordDto upload(MultipartFile file, String businessType);

    MdmFileRecordDto upload(MultipartFile file, String businessType, String bucket);

    MdmFileRecordDto uploadPublic(MultipartFile file);

    MdmFileRecordDto uploadPublic(MultipartFile file, String businessType);

    MdmFileRecordDto uploadPublic(MultipartFile file, String businessType, String bucket);

    Integer delete(String fileId);

    Integer delete(List<String> fileIds);

    <P, R, V> void sign(P fsFile, Function<P, R> getter, BiConsumer<P, V> setter);

    <P, R, V> void sign(List<P> fsFiles, Function<P, R> getter, BiConsumer<P, V> setter);

    String signContent(String content);

    String signs(String fileIds);

    String sign(String fileId);

    String[] sign(String[] fileIds);

    String sign(MdmFileRecord fsFile);

    List<String> sign(List<?> fsFiles);

    String sign(String fileId, Integer expire, TimeUnit timeUnit);

    String sign(MdmFileRecord fsFile, Integer expire, TimeUnit timeUnit);

    List<String> sign(List<?> fsFiles, Integer expire, TimeUnit timeUnit);

}
