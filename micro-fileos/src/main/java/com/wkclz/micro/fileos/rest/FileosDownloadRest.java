package com.wkclz.micro.fileos.rest;

import com.wkclz.core.base.R;
import com.wkclz.micro.fileos.api.FileosDownloadApi;
import com.wkclz.micro.fileos.api.FileosSignApi;
import com.wkclz.micro.fileos.bean.entity.MdmFileosBucket;
import com.wkclz.micro.fileos.bean.entity.MdmFileosRecord;
import com.wkclz.micro.fileos.helper.BucketCache;
import com.wkclz.micro.fileos.service.MdmFileosRecordService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@RequestMapping(Route.PREFIX)
public class FileosDownloadRest {

    @Autowired
    private FileosDownloadApi fileosDownloadApi;
    @Autowired
    private FileosSignApi fileosSignApi;
    @Autowired
    private MdmFileosRecordService mdmFileosRecordService;
    @Autowired
    private BucketCache bucketCache;

    @GetMapping(Route.DOWNLOAD)
    public void download(@PathVariable String fileId, HttpServletRequest request, HttpServletResponse response) {
        MdmFileosRecord record = mdmFileosRecordService.getRecordByFileId(fileId, null);
        if (record == null) {
            log.warn("下载文件不存在: fileId={}", fileId);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        MdmFileosBucket bucket = bucketCache.get(record.getBucketName());
        if (bucket == null) {
            log.error("下载文件Bucket不存在: bucketName={}", record.getBucketName());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        String fileName = StringUtils.isNotBlank(record.getFileName()) ? record.getFileName() : fileId;
        Long fileSize = record.getFileSize();

        try {
            String rangeHeader = request.getHeader("Range");
            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                String[] ranges = rangeHeader.substring(6).split("-");
                long start = Long.parseLong(ranges[0]);
                long end = ranges.length > 1 && StringUtils.isNotBlank(ranges[1])
                        ? Long.parseLong(ranges[1])
                        : fileSize - 1;
                long contentLength = end - start + 1;

                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                response.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + fileSize);
                response.setContentLengthLong(contentLength);
            } else {
                response.setContentLengthLong(fileSize != null ? fileSize : 0);
            }

            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
            if (fileSize != null) {
                response.setHeader("Accept-Ranges", "bytes");
            }

            InputStream inputStream;
            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                String[] ranges = rangeHeader.substring(6).split("-");
                long start = Long.parseLong(ranges[0]);
                long end = ranges.length > 1 && StringUtils.isNotBlank(ranges[1])
                        ? Long.parseLong(ranges[1])
                        : fileSize - 1;
                long length = end - start + 1;
                inputStream = fileosDownloadApi.download(fileId, start, length);
            } else {
                inputStream = fileosDownloadApi.download(fileId);
            }

            try (OutputStream out = response.getOutputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                out.flush();
            }
        } catch (Exception e) {
            log.error("文件下载失败: fileId={}, fileName={}", fileId, fileName, e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

}
