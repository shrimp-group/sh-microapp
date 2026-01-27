package com.wkclz.micro.file.api.impl;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.file.api.FsApi;
import com.wkclz.micro.file.helper.BucketCache;
import com.wkclz.micro.file.helper.ContentFileHelper;
import com.wkclz.micro.file.pojo.dto.FileRecord;
import com.wkclz.micro.file.pojo.dto.MdmFsFilesDto;
import com.wkclz.micro.file.pojo.entity.MdmFsBucket;
import com.wkclz.micro.file.pojo.entity.MdmFsFiles;
import com.wkclz.micro.file.pojo.enums.OssSpEnum;
import com.wkclz.micro.file.service.FileService;
import com.wkclz.micro.file.service.MdmFsFilesService;
import com.wkclz.spring.config.SpringContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FsImpl implements FsApi {

    private static final Pattern URI_PATTERN = Pattern.compile("https?://[^/]+(/[^?#]*)");

    @Autowired
    private BucketCache bucketCache;
    @Autowired
    private MdmFsFilesService mdmFsFilesService;


    @Override
    public MdmFsFilesDto upload(MultipartFile file) {
        return upload(file, null, null);
    }
    @Override
    public MdmFsFilesDto upload(MultipartFile file, String businessType) {
        return upload(file, businessType, null);
    }
    @Override
    public MdmFsFilesDto upload(MultipartFile file, String businessType, String bucket) {
        businessType = getBusinessType(businessType);
        MdmFsBucket fsBucket = getBucket(bucket);
        FileService service = getApi(fsBucket);
        return service.upload(file, fsBucket, businessType);
    }

    @Override
    public MdmFsFilesDto uploadPublic(MultipartFile file) {
        return upload(file, null, null);
    }
    @Override
    public MdmFsFilesDto uploadPublic(MultipartFile file, String businessType) {
        return upload(file, businessType, null);
    }
    @Override
    public MdmFsFilesDto uploadPublic(MultipartFile file, String businessType, String bucket) {
        businessType = getBusinessType(businessType);
        MdmFsBucket fsBucket = getBucket(bucket);
        FileService service = getApi(fsBucket);
        return service.uploadPublic(file, fsBucket, businessType);
    }


    @Override
    public <P, R, V> void sign(P fsFile, Function<P, R> getter, BiConsumer<P, V> setter) {
        R fileId = getter.apply(fsFile);
        if (!(fileId instanceof String)) {
            return;
        }
        String sign = sign((String)fileId);
        setter.accept(fsFile, (V)sign);
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
            V v = (V)signs.get(i);
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
    public String sign(MdmFsFiles fsFile) {
        return sign(fsFile, 10, TimeUnit.MINUTES);
    }
    @Override
    public List<String> sign(List fsFiles) {
        return sign(fsFiles, 10, TimeUnit.MINUTES);
    }
    @Override
    public String sign(String fileId, Integer expire, TimeUnit timeUnit) {
        if (StringUtils.isBlank(fileId)) {
            return fileId;
        }
        String tmpFileId = getFileId(fileId);
        MdmFsFiles fsFile = mdmFsFilesService.getFilesByFileId(tmpFileId);
        if (fsFile == null) {
            log.warn("文件： {} 不存在，无法完成签名", fileId);
            return fileId;
        }
        return sign(fsFile, expire, timeUnit);
    }
    @Override
    public String sign(MdmFsFiles fsFile, Integer expire, TimeUnit timeUnit) {
        if (fsFile == null) {
            return null;
        }
        if (expire == null) {
            expire = 10;
        }
        if (timeUnit == null) {
            timeUnit = TimeUnit.MINUTES;
        }
        MdmFsBucket fsBucket = getBucket(fsFile.getBucket());
        FileService service = getApi(fsBucket);
        return service.sign(fsFile.getFileId(), fsBucket, expire, timeUnit);
    }
    @Override
    public List<String> sign(List fsFiles, Integer expire, TimeUnit timeUnit) {
        if (expire == null) {
            expire = 10;
        }
        if (timeUnit == null) {
            timeUnit = TimeUnit.MINUTES;
        }

        // 入参分类
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
            if (f instanceof MdmFsFiles file) {
                String fileId = URLDecoder.decode(file.getFileId(), StandardCharsets.UTF_8);
                r.setOrgFileName(file.getFileId());
                r.setFile(file);
                r.setFileId(fileId);
                r.setBucket(file.getBucket());
                continue;
            }
            throw ValidationException.of("error type of the file: {}", f);
        }

        // 为没有 bucket 的信息找到 bucket
        List<String> tempFileIds4Fetch = rs.stream().filter(t -> t.getBucket() == null).map(FileRecord::getFileId).toList();
        if (CollectionUtils.isNotEmpty(tempFileIds4Fetch)) {
            List<MdmFsFiles> tempFiles = mdmFsFilesService.getFilesByFileIds(tempFileIds4Fetch);
            for (FileRecord r : rs) {
                if (r.getBucket() == null) {
                    for (MdmFsFiles tempFile : tempFiles) {
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

        // 按 bucket 分组
        Map<String, List<FileRecord>> rsMap = rs.stream()
            .filter(t -> StringUtils.isNotBlank(t.getBucket()))
            .collect(Collectors.groupingBy(FileRecord::getBucket));

        // 按 bucket 进行签名
        for (Map.Entry<String, List<FileRecord>> entry : rsMap.entrySet()) {
            String bucket = entry.getKey();
            List<FileRecord> fileRecords = entry.getValue();

            MdmFsBucket fsBucket = getBucket(bucket);
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




    @Override
    public Integer delete(String fileId) {
        return delete(Collections.singletonList(fileId));
    }
    @Override
    public Integer delete(List<String> fileIds) {
        List<MdmFsFiles> fsFiles = mdmFsFilesService.getFilesByFileIds(fileIds);

        if (fsFiles.size() != fileIds.size()) {
            throw ValidationException.of("待删除的文件可能已经丢失，请核实后再操作!");
        }
        Map<String, List<MdmFsFiles>> filesSpMap = fsFiles.stream().collect(Collectors.groupingBy(MdmFsFiles::getOssSp));

        // 遍历 oss sp
        for (Map.Entry<String, List<MdmFsFiles>> entry : filesSpMap.entrySet()) {
            String ossSp = entry.getKey();
            List<MdmFsFiles> spFiles = entry.getValue();

            FileService service = getApi(ossSp);
            Map<String, List<MdmFsFiles>> filesBucketMap = spFiles.stream().collect(Collectors.groupingBy(MdmFsFiles::getBucket));

            // 遍历 oss bucket
            for (String bucket : filesSpMap.keySet()) {
                List<MdmFsFiles> bucketFiles = filesBucketMap.get(bucket);
                MdmFsBucket fsBucket = getBucket(bucket);
                List<String> collect = bucketFiles.stream().map(MdmFsFiles::getFileId).collect(Collectors.toList());
                service.delete(collect, fsBucket);
            }
        }
        List<Long> ids = fsFiles.stream().map(MdmFsFiles::getId).toList();
        MdmFsFiles forDelete = new MdmFsFiles();
        forDelete.setIds(ids);
        mdmFsFilesService.deleteByIds(forDelete);
        return fsFiles.size();
    }


    /**
     * 获取 businessType
     */
    private static String getBusinessType(String businessType) {
        if (StringUtils.isNotBlank(businessType)) {
            return businessType;
        }
        return "common";
    }

    /**
     * 获取 bucket 配置信息
     */
    private MdmFsBucket getBucket(String bucket) {
        if (StringUtils.isNotBlank(bucket)) {
            MdmFsBucket mdmFsBucket = bucketCache.get(bucket);
            if (mdmFsBucket == null) {
                throw ValidationException.of("bucket: {} 未配置，请联系管理员检查！");
            }
            return mdmFsBucket;
        }
        MdmFsBucket mdmFsBucket = bucketCache.get();
        if (mdmFsBucket == null) {
            throw ValidationException.of("无法匹配可用的 bucket 配置，请联系管理员完善 bucket 配置！");
        }
        return mdmFsBucket;
    }

    private FileService getApi(MdmFsBucket bucket) {
        if (bucket == null) {
            throw ValidationException.of("无 bucket 信息");
        }
        String ossSp = bucket.getOssSp();
        return getApi(ossSp);
    }

    private FileService getApi(String ossSp) {
        if (StringUtils.isBlank(ossSp)) {
            throw ValidationException.of("bucket: {} 未维护 sp 信息，请联系管理员！");
        }
        if (!EnumUtils.isValidEnum(OssSpEnum.class, ossSp)) {
            throw ValidationException.of("bucket: {} 未支持的 sp，请联系管理员！");
        }
        OssSpEnum anEnum = EnumUtils.getEnum(OssSpEnum.class, ossSp);
        return SpringContextHolder.getBean(anEnum.getServiceName());
    }

    private static String getFileId(String orgStr) {
        if (StringUtils.isBlank(orgStr)) {
            return orgStr;
        }
        if (!orgStr.startsWith("http")) {
            return orgStr;
        }
        Matcher matcher = URI_PATTERN.matcher(orgStr);
        if (matcher.find()) {
            // 返回第一个捕获组，即URI路径部分
            String group = matcher.group(1);
            return group.substring(1);
        }
        return orgStr;
    }

}
