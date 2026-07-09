package com.wkclz.micro.fileos.api.impl;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.iam.contract.context.PrincipalContext;
import com.wkclz.micro.fileos.api.FileosSignApi;
import com.wkclz.micro.fileos.bean.dto.ImageProcessParam;
import com.wkclz.micro.fileos.bean.entity.MdmFileosBucket;
import com.wkclz.micro.fileos.bean.entity.MdmFileosRecord;
import com.wkclz.micro.fileos.helper.ImageProcessHelper;
import com.wkclz.micro.fileos.service.FileosService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FileosSignApiImpl extends AbstractFileosApi implements FileosSignApi {

    private static final Pattern IMG_SRC_PATTERN = Pattern.compile("<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>");

    @Autowired
    private ImageProcessHelper imageProcessHelper;

    @Override
    public String sign(String fileId) {
        return sign(fileId, 10, TimeUnit.MINUTES);
    }

    @Override
    public String sign(String fileId, Integer expire, TimeUnit timeUnit) {
        if (StringUtils.isBlank(fileId)) {
            return fileId;
        }
        if (fileId.contains(",")) {
            String[] parts = fileId.split(",");
            List<String> signedParts = new ArrayList<>();
            for (String part : parts) {
                String trimmed = part.trim();
                if (StringUtils.isBlank(trimmed)) {
                    signedParts.add(trimmed);
                    continue;
                }
                signedParts.add(signSingle(trimmed, expire, timeUnit));
            }
            return String.join(",", signedParts);
        }
        return signSingle(fileId, expire, timeUnit);
    }

    @Override
    public String sign(MdmFileosRecord record) {
        return sign(record, 10, TimeUnit.MINUTES);
    }

    @Override
    public String sign(MdmFileosRecord record, Integer expire, TimeUnit timeUnit) {
        if (record == null) {
            return null;
        }
        if (expire == null) {
            expire = 10;
        }
        if (timeUnit == null) {
            timeUnit = TimeUnit.MINUTES;
        }
        MdmFileosBucket bucket = getBucket(record.getBucketName());
        FileosService service = getApi(bucket);
        String signedUrl = service.sign(record.getFileId(), bucket, expire, timeUnit);
        if (StringUtils.isNotBlank(record.getImageProcess())) {
            ImageProcessParam param = parseImageProcess(record.getImageProcess());
            signedUrl = imageProcessHelper.buildProcessUrl(signedUrl, param, record.getOssSp());
        }
        return signedUrl;
    }

    private String signSingle(String fileId, Integer expire, TimeUnit timeUnit) {
        if (StringUtils.isBlank(fileId)) {
            return fileId;
        }
        String tmpFileId = getFileId(fileId);
        tmpFileId = URLDecoder.decode(tmpFileId, StandardCharsets.UTF_8);
        String tenantCode = PrincipalContext.getTenantCode();
        MdmFileosRecord record = mdmFileosRecordService.getRecordByFileId(tmpFileId, tenantCode);
        if (record == null) {
            log.warn("文件: {} 不存在，无法完成签名", fileId);
            return fileId;
        }

        if (expire == null) {
            expire = 10;
        }
        if (timeUnit == null) {
            timeUnit = TimeUnit.MINUTES;
        }

        MdmFileosBucket bucket = getBucket(record.getBucketName());
        FileosService service = getApi(bucket);
        String signedUrl = service.sign(record.getFileId(), bucket, expire, timeUnit);

        if (StringUtils.isNotBlank(record.getImageProcess())) {
            ImageProcessParam param = parseImageProcess(record.getImageProcess());
            signedUrl = imageProcessHelper.buildProcessUrl(signedUrl, param, record.getOssSp());
        }

        return signedUrl;
    }

    @Override
    public List<String> sign(List<?> fileIds) {
        return sign(fileIds, 10, TimeUnit.MINUTES);
    }

    @Override
    public List<String> sign(List<?> fileIds, Integer expire, TimeUnit timeUnit) {
        if (expire == null) {
            expire = 10;
        }
        if (timeUnit == null) {
            timeUnit = TimeUnit.MINUTES;
        }

        String tenantCode = PrincipalContext.getTenantCode();
        List<SignRecord> rs = new ArrayList<>();
        for (Object f : fileIds) {
            SignRecord r = new SignRecord();
            rs.add(r);
            if (f instanceof String str) {
                r.setOrgValue(str);
                String fileId = getFileId(str);
                fileId = URLDecoder.decode(fileId, StandardCharsets.UTF_8);
                r.setFileId(fileId);
                continue;
            }
            if (f instanceof MdmFileosRecord record) {
                String fileId = URLDecoder.decode(record.getFileId(), StandardCharsets.UTF_8);
                r.setOrgValue(record.getFileId());
                r.setRecord(record);
                r.setFileId(fileId);
                r.setBucketName(record.getBucketName());
                continue;
            }
            throw ValidationException.of("不支持的文件ID类型: {}", f);
        }

        List<String> fileIds4Fetch = rs.stream()
            .filter(t -> t.getBucketName() == null)
            .map(SignRecord::getFileId)
            .toList();
        if (CollectionUtils.isNotEmpty(fileIds4Fetch)) {
            List<MdmFileosRecord> records = mdmFileosRecordService.getRecordByFileIds(fileIds4Fetch, tenantCode);
            for (SignRecord r : rs) {
                if (r.getBucketName() == null) {
                    for (MdmFileosRecord rec : records) {
                        if (r.getFileId().equals(rec.getFileId())) {
                            r.setRecord(rec);
                            r.setBucketName(rec.getBucketName());
                            break;
                        }
                    }
                }
            }
        }

        Map<String, List<SignRecord>> bucketMap = rs.stream()
            .filter(t -> StringUtils.isNotBlank(t.getBucketName()))
            .collect(Collectors.groupingBy(SignRecord::getBucketName));

        for (Map.Entry<String, List<SignRecord>> entry : bucketMap.entrySet()) {
            String bucketName = entry.getKey();
            List<SignRecord> signRecords = entry.getValue();

            MdmFileosBucket bucket = getBucket(bucketName);
            FileosService service = getApi(bucket);
            List<String> ids = signRecords.stream().map(SignRecord::getFileId).collect(Collectors.toList());
            List<String> signs = service.sign(ids, bucket, expire, timeUnit);

            for (int i = 0; i < signRecords.size(); i++) {
                SignRecord sr = signRecords.get(i);
                String signedUrl = signs.get(i);
                if (sr.getRecord() != null && StringUtils.isNotBlank(sr.getRecord().getImageProcess())) {
                    ImageProcessParam param = parseImageProcess(sr.getRecord().getImageProcess());
                    signedUrl = imageProcessHelper.buildProcessUrl(signedUrl, param, sr.getRecord().getOssSp());
                }
                sr.setSignedUrl(signedUrl);
            }
        }

        for (SignRecord r : rs) {
            if (r.getSignedUrl() == null) {
                r.setSignedUrl(r.getOrgValue() != null ? r.getOrgValue() : r.getFileId());
            }
        }

        return rs.stream().map(SignRecord::getSignedUrl).collect(Collectors.toList());
    }

    @Override
    public String signContent(String content) {
        if (StringUtils.isBlank(content)) {
            return content;
        }
        List<String> urls = extractUrls(content);
        if (CollectionUtils.isEmpty(urls)) {
            return content;
        }
        List<String> signs = sign(urls);
        return replaceUrls(content, urls, signs);
    }

    @Override
    public <P, R, V> void sign(P entity, Function<P, R> getter, BiConsumer<P, V> setter) {
        R fileId = getter.apply(entity);
        if (!(fileId instanceof String)) {
            return;
        }
        String sign = sign((String) fileId);
        setter.accept(entity, (V) sign);
    }

    @Override
    public <P, R, V> void sign(List<P> entities, Function<P, R> getter, BiConsumer<P, V> setter) {
        List<String> fileIds = new ArrayList<>();
        Map<Integer, Integer> idxMap = new HashMap<>();
        int idx = 0;
        for (int i = 0; i < entities.size(); i++) {
            P entity = entities.get(i);
            R fileId = getter.apply(entity);
            if (fileId instanceof String str) {
                fileIds.add(str);
                idxMap.put(idx++, i);
            }
        }
        if (CollectionUtils.isEmpty(fileIds)) {
            return;
        }
        List<String> signs = sign(fileIds);
        for (int i = 0; i < signs.size(); i++) {
            Integer origIdx = idxMap.get(i);
            P p = entities.get(origIdx);
            V v = (V) signs.get(i);
            setter.accept(p, v);
        }
    }

    private List<String> extractUrls(String text) {
        List<String> urls = new ArrayList<>();
        Matcher matcher = IMG_SRC_PATTERN.matcher(text);
        while (matcher.find()) {
            urls.add(matcher.group(1));
        }
        return urls;
    }

    private String replaceUrls(String text, List<String> from, List<String> to) {
        for (int i = 0; i < from.size(); i++) {
            text = text.replace(from.get(i), to.get(i));
        }
        return text;
    }

    private ImageProcessParam parseImageProcess(String imageProcess) {
        if (StringUtils.isBlank(imageProcess)) {
            return null;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(imageProcess, ImageProcessParam.class);
        } catch (Exception e) {
            log.warn("解析图片处理参数失败: {}", e.getMessage());
            return null;
        }
    }

    private static class SignRecord {
        private String orgValue;
        private String fileId;
        private MdmFileosRecord record;
        private String bucketName;
        private String signedUrl;

        public String getOrgValue() { return orgValue; }
        public void setOrgValue(String orgValue) { this.orgValue = orgValue; }
        public String getFileId() { return fileId; }
        public void setFileId(String fileId) { this.fileId = fileId; }
        public MdmFileosRecord getRecord() { return record; }
        public void setRecord(MdmFileosRecord record) { this.record = record; }
        public String getBucketName() { return bucketName; }
        public void setBucketName(String bucketName) { this.bucketName = bucketName; }
        public String getSignedUrl() { return signedUrl; }
        public void setSignedUrl(String signedUrl) { this.signedUrl = signedUrl; }
    }

}
