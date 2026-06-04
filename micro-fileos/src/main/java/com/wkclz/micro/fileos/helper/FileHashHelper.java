package com.wkclz.micro.fileos.helper;

import com.wkclz.micro.fileos.config.FileosConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Component
public class FileHashHelper {

    @Autowired
    private FileosConfig fileosConfig;

    public String computeHash(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            return computeHash(is);
        } catch (IOException e) {
            log.error("计算文件Hash失败: {}", e.getMessage(), e);
            return null;
        }
    }

    public String computeHash(InputStream is) {
        try {
            String algorithm = fileosConfig.getHashAlgorithm();
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
            byte[] hashBytes = digest.digest();
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            log.error("不支持的Hash算法: {}", fileosConfig.getHashAlgorithm(), e);
            return null;
        } catch (IOException e) {
            log.error("读取流计算Hash失败: {}", e.getMessage(), e);
            return null;
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

}
