package com.wkclz.micro.file.helper;

import com.wkclz.micro.file.config.FsConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class FileTypeHelper {

    @Autowired
    private FsConfig fsConfig;


    /**
     * 识别文件是否为图片
     */
    public boolean isImage(String fileName) {
        String extnames = fsConfig.getImageExtensionNames();
        return isExtName(fileName, extnames);
    }

    /**
     * 识别文件是否为视频
     */
    public boolean isVideo(String fileName) {
        String extnames = fsConfig.getVideoExtensionNames();
        return isExtName(fileName, extnames);
    }

    /**
     * 根据文件名，和扩展名列表，识别文件扩展名是否在列表内
     */
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
        if (exts.contains(extName)) {
            return true;
        }
        return false;
    }


    /**
     * 根据文件名获取文件扩展名
     */
    public static String getExtName(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return null;
        }
        int dot = fileName.lastIndexOf(".");
        if (dot == -1) {
            return null;
        }
        if (dot == fileName.length() -1) {
            return null;
        }
        return fileName.substring(dot + 1).toLowerCase();
    }
}
