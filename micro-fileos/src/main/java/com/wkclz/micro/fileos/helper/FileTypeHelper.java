package com.wkclz.micro.fileos.helper;

import com.wkclz.micro.fileos.config.FileosConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class FileTypeHelper {

    private static final Map<String, byte[]> MAGIC_BYTES_MAP;

    static {
        MAGIC_BYTES_MAP = new HashMap<>();
        MAGIC_BYTES_MAP.put(".jpg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});
        MAGIC_BYTES_MAP.put(".jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});
        MAGIC_BYTES_MAP.put(".png", new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A});
        MAGIC_BYTES_MAP.put(".gif", new byte[]{0x47, 0x49, 0x46, 0x38});
        MAGIC_BYTES_MAP.put(".webp", new byte[]{0x52, 0x49, 0x46, 0x46, 0x00, 0x00, 0x00, 0x00, 0x57, 0x45, 0x42, 0x50});
        MAGIC_BYTES_MAP.put(".pdf", new byte[]{0x25, 0x50, 0x44, 0x46, 0x2D});
        MAGIC_BYTES_MAP.put(".zip", new byte[]{0x50, 0x4B, 0x03, 0x04});
        MAGIC_BYTES_MAP.put(".mp4", new byte[]{0x00, 0x00, 0x00, 0x1C, 0x66, 0x74, 0x79, 0x70, 0x6D, 0x70, 0x34, 0x32});
        MAGIC_BYTES_MAP.put(".xml", new byte[]{0x3C, 0x3F, 0x78, 0x6D, 0x6C});
    }

    @Autowired
    private FileosConfig fileosConfig;

    public boolean isImage(String fileName) {
        String extnames = fileosConfig.getImageExtensionNames();
        return isExtName(fileName, extnames);
    }

    public boolean isVideo(String fileName) {
        String extnames = fileosConfig.getVideoExtensionNames();
        return isExtName(fileName, extnames);
    }

    private boolean isExtName(String fileName, String extnames) {
        String extName = getExtName(fileName);
        if (extName == null) {
            return false;
        }
        if (StringUtils.isBlank(extnames)) {
            log.error("请完善 图片|视频 扩展名配置，以便准确识别文件名的扩展名！");
            return false;
        }
        extnames = extnames.toLowerCase();
        List<String> exts = Arrays.asList(extnames.split("[,，;；|]"));
        return exts.contains(extName);
    }

    public static String getExtName(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return null;
        }
        int dot = fileName.lastIndexOf(".");
        if (dot == -1) {
            return null;
        }
        if (dot == fileName.length() - 1) {
            return null;
        }
        return fileName.substring(dot + 1).toLowerCase();
    }

    public boolean validateFileContent(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (StringUtils.isBlank(originalFilename)) {
            return true;
        }
        int dot = originalFilename.lastIndexOf(".");
        if (dot == -1) {
            return true;
        }
        String extWithDot = originalFilename.substring(dot).toLowerCase();
        byte[] expectedBytes = MAGIC_BYTES_MAP.get(extWithDot);
        if (expectedBytes == null) {
            return true;
        }

        if (".webp".equals(extWithDot)) {
            return validateWebp(file, originalFilename);
        }
        if (".mp4".equals(extWithDot)) {
            return validateMp4(file, originalFilename);
        }

        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[expectedBytes.length];
            int read = is.read(header);
            if (read < expectedBytes.length) {
                log.warn("File content validation failed: file too short, filename={}", originalFilename);
                return false;
            }
            for (int i = 0; i < expectedBytes.length; i++) {
                if (header[i] != expectedBytes[i]) {
                    log.warn("File content validation failed: magic bytes mismatch, filename={}", originalFilename);
                    return false;
                }
            }
            return true;
        } catch (IOException e) {
            log.warn("File content validation read failed: {}", e.getMessage());
            return true;
        }
    }

    private boolean validateWebp(MultipartFile file, String originalFilename) {
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[12];
            int read = is.read(header);
            if (read < 12) {
                log.warn("File content validation failed: file too short for webp, filename={}", originalFilename);
                return false;
            }
            if (header[0] != 0x52 || header[1] != 0x49 || header[2] != 0x46 || header[3] != 0x46) {
                log.warn("File content validation failed: webp RIFF mismatch, filename={}", originalFilename);
                return false;
            }
            if (header[8] != 0x57 || header[9] != 0x45 || header[10] != 0x42 || header[11] != 0x50) {
                log.warn("File content validation failed: webp WEBP mismatch, filename={}", originalFilename);
                return false;
            }
            return true;
        } catch (IOException e) {
            log.warn("File content validation read failed: {}", e.getMessage());
            return true;
        }
    }

    private boolean validateMp4(MultipartFile file, String originalFilename) {
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[12];
            int read = is.read(header);
            if (read < 8) {
                log.warn("File content validation failed: file too short for mp4, filename={}", originalFilename);
                return false;
            }
            if (header[4] != 0x66 || header[5] != 0x74 || header[6] != 0x79 || header[7] != 0x70) {
                log.warn("File content validation failed: mp4 ftyp mismatch, filename={}", originalFilename);
                return false;
            }
            return true;
        } catch (IOException e) {
            log.warn("File content validation read failed: {}", e.getMessage());
            return true;
        }
    }

}
