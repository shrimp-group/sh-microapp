package com.wkclz.micro.fileos.api;

import com.wkclz.micro.fileos.bean.entity.MdmFileosRecord;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Function;

public interface FileosSignApi {

    String sign(String fileId);

    String sign(String fileId, Integer expire, TimeUnit timeUnit);

    String sign(MdmFileosRecord record);

    String sign(MdmFileosRecord record, Integer expire, TimeUnit timeUnit);

    List<String> sign(List<?> fileIds);

    List<String> sign(List<?> fileIds, Integer expire, TimeUnit timeUnit);

    String signContent(String content);

    /**
     * 通用内容签名：支持富文本（img src）、逗号分隔（URL/fileId）、JSON 对象转字符串中的图片签名，
     * 签名失败的片段原样保留
     */
    String signContentPlus(String content);

    <P, R, V> void sign(P entity, Function<P, R> getter, BiConsumer<P, V> setter);

    <P, R, V> void sign(List<P> entities, Function<P, R> getter, BiConsumer<P, V> setter);

}
