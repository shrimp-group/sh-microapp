package com.wkclz.micro.file.api;

import com.wkclz.micro.file.bean.entity.MdmFileRecord;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Function;

public interface FileSignApi {

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
