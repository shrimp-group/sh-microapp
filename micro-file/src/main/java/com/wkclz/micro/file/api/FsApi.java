package com.wkclz.micro.file.api;

import com.wkclz.micro.file.pojo.dto.MdmFsFilesDto;
import com.wkclz.micro.file.pojo.entity.MdmFsFiles;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Function;

public interface FsApi {

    /**
     * 上传到私有读
     */
    MdmFsFilesDto upload(MultipartFile file);
    MdmFsFilesDto upload(MultipartFile file, String businessType);
    MdmFsFilesDto upload(MultipartFile file, String businessType, String bucket);

    /**
     * 上传到公有读 【慎用！】
     */
    MdmFsFilesDto uploadPublic(MultipartFile file);
    MdmFsFilesDto uploadPublic(MultipartFile file, String businessType);
    MdmFsFilesDto uploadPublic(MultipartFile file, String businessType, String bucket);

    /**
     * 私有读签名
     */


    <P, R, V> void sign(P fsFile, Function<P, R> getter, BiConsumer<P, V> setter);
    <P, R, V> void sign(List<P> fsFiles, Function<P, R> getter, BiConsumer<P, V> setter);

    String signContent(String content);
    String signs(String fileIds);
    String sign(String fileId);
    String[] sign(String[] fileIds);
    String sign(MdmFsFiles fsFiles);
    List<String> sign(List fsFiles);
    String sign(String fileId, Integer expire, TimeUnit timeUnit);
    String sign(MdmFsFiles fsFiles, Integer expire, TimeUnit timeUnit);
    List<String> sign(List fsFiles, Integer expire, TimeUnit timeUnit);

    /**
     * 删除本地
     */
    Integer delete(String fileId);
    Integer delete(List<String> fileIds);

}
