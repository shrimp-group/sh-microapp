package com.wkclz.micro.file.api.impl;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.file.api.FileSignApi;
import com.wkclz.micro.file.bean.dto.FileRecord;
import com.wkclz.micro.file.bean.entity.MdmFileBucket;
import com.wkclz.micro.file.bean.entity.MdmFileRecord;
import com.wkclz.micro.file.helper.ContentFileHelper;
import com.wkclz.micro.file.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FileSignApiImpl extends AbstractFileApi implements FileSignApi {

    @Override
    public <P, R, V> void sign(P fsFile, Function<P, R> getter, BiConsumer<P, V> setter) {
        R fileId = getter.apply(fsFile);
        if (!(fileId instanceof String)) {
            return;
        }
        String sign = sign((String) fileId);
        setter.accept(fsFile, (V) sign);
    }

    @Override
    public <P, R, V> void sign(List<P> fsFiles, Function<P, R> getter, BiConsumer<P, V> setter) {
        List<String> fileIds = new ArrayList<>();
        Map<Integer, Integer> newOldIdxMap = new HashMap<>();
        int idx = 0;
        for (int i = 0; i < fsFiles.size(); i++) {
            P fsFile = fsFiles.get(i);
            R fileId = getter.apply(fsFile);
            if (fileId instanceof String str) {
                fileIds.add(str);
                newOldIdxMap.put(idx++, i);
            }
        }
        if (CollectionUtils.isEmpty(fileIds)) {
            return;
        }
        List<String> signs = sign(fileIds);
        for (int i = 0; i < signs.size(); i++) {
            Integer oldInx = newOldIdxMap.get(i);
            P p = fsFiles.get(oldInx);
            V v = (V) signs.get(i);
            setter.accept(p, v);
        }
    }

    @Override
    public String signContent(String content) {
        if (StringUtils.isBlank(content)) {
            return content;
        }
        List<String> files = ContentFileHelper.extractUrls(content);
        if (CollectionUtils.isEmpty(files)) {
            return content;
        }
        List<String> signs = sign(files);
        return ContentFileHelper.replaceUrls(content, files, signs);
    }

    @Override
    public String signs(String fileIds) {
        if (StringUtils.isBlank(fileIds)) {
            return fileIds;
        }
        String[] split = fileIds.split(",");
        List<String> signs = sign(Arrays.asList(split));
        return StringUtils.join(signs, ",");
    }

    @Override
    public String sign(String fileId) {
        if (StringUtils.isBlank(fileId)) {
            return fileId;
        }
        String[] fileIds = fileId.split(",");
        if (fileIds.length == 1) {
            return sign(fileId, 10, TimeUnit.MINUTES);
        }
        String[] sign = sign(fileIds);
        return StringUtils.join(sign, ",");
    }

    @Override
    public String[] sign(String[] fileIds) {
        List<String> list = Arrays.asList(fileIds);
        List<String> signs = sign(list, 10, TimeUnit.MINUTES);
        return signs.toArray(new String[0]);
    }

    @Override
    public String sign(MdmFileRecord fsFile) {
        return sign(fsFile, 10, TimeUnit.MINUTES);
    }

    @Override
    public List<String> sign(List<?> fsFiles) {
        return sign(fsFiles, 10, TimeUnit.MINUTES);
    }

    @Override
    public String sign(String fileId, Integer expire, TimeUnit timeUnit) {
        if (StringUtils.isBlank(fileId)) {
            return fileId;
        }
        String tmpFileId = getFileId(fileId);
        MdmFileRecord fsFile = mdmFileRecordService.getFilesByFileId(tmpFileId);
        if (fsFile == null) {
            log.warn("文件： {} 不存在，无法完成签名", fileId);
            return fileId;
        }
        return sign(fsFile, expire, timeUnit);
    }

    @Override
    public String sign(MdmFileRecord fsFile, Integer expire, TimeUnit timeUnit) {
        if (fsFile == null) {
            return null;
        }
        if (expire == null) {
            expire = 10;
        }
        if (timeUnit == null) {
            timeUnit = TimeUnit.MINUTES;
        }
        MdmFileBucket fsBucket = getBucket(fsFile.getBucket());
        FileService service = getApi(fsBucket);
        return service.sign(fsFile.getFileId(), fsBucket, expire, timeUnit);
    }

    @Override
    public List<String> sign(List<?> fsFiles, Integer expire, TimeUnit timeUnit) {
        if (expire == null) {
            expire = 10;
        }
        if (timeUnit == null) {
            timeUnit = TimeUnit.MINUTES;
        }

        List<FileRecord> rs = new ArrayList<>();
        for (Object f : fsFiles) {
            FileRecord r = new FileRecord();
            rs.add(r);
            if (f instanceof String str) {
                r.setOrgFileName(str);
                String fileId = getFileId(str);
                fileId = URLDecoder.decode(fileId, StandardCharsets.UTF_8);
                r.setFileId(fileId);
                continue;
            }
            if (f instanceof MdmFileRecord file) {
                String fileId = URLDecoder.decode(file.getFileId(), StandardCharsets.UTF_8);
                r.setOrgFileName(file.getFileId());
                r.setFile(file);
                r.setFileId(fileId);
                r.setBucket(file.getBucket());
                continue;
            }
            throw ValidationException.of("error type of the file: {}", f);
        }

        List<String> tempFileIds4Fetch = rs.stream().filter(t -> t.getBucket() == null).map(FileRecord::getFileId).toList();
        if (CollectionUtils.isNotEmpty(tempFileIds4Fetch)) {
            List<MdmFileRecord> tempFiles = mdmFileRecordService.getFilesByFileIds(tempFileIds4Fetch);
            for (FileRecord r : rs) {
                if (r.getBucket() == null) {
                    for (MdmFileRecord tempFile : tempFiles) {
                        if (r.getFileId().equals(tempFile.getFileId())) {
                            r.setFile(tempFile);
                            r.setFileId(tempFile.getFileId());
                            r.setBucket(tempFile.getBucket());
                            break;
                        }
                    }
                }
            }
        }

        Map<String, List<FileRecord>> rsMap = rs.stream()
            .filter(t -> StringUtils.isNotBlank(t.getBucket()))
            .collect(Collectors.groupingBy(FileRecord::getBucket));

        for (Map.Entry<String, List<FileRecord>> entry : rsMap.entrySet()) {
            String bucket = entry.getKey();
            List<FileRecord> fileRecords = entry.getValue();

            MdmFileBucket fsBucket = getBucket(bucket);
            FileService service = getApi(fsBucket);
            List<String> list = fileRecords.stream().map(FileRecord::getFileId).collect(Collectors.toList());
            List<String> signs = service.sign(list, fsBucket, expire, timeUnit);
            for (FileRecord fileRecord : fileRecords) {
                for (int i = 0; i < list.size(); i++) {
                    if (fileRecord.getFileId().equals(list.get(i))) {
                        fileRecord.setPreviewUrl(signs.get(i));
                        break;
                    }
                }
            }
        }
        for (FileRecord r : rs) {
            if (r.getPreviewUrl() == null) {
                r.setPreviewUrl(r.getOrgFileName());
            }
            if (r.getPreviewUrl() == null) {
                r.setPreviewUrl(r.getFileId());
            }
        }
        return rs.stream().map(FileRecord::getPreviewUrl).collect(Collectors.toList());
    }

}
