package com.wkclz.micro.fileos.utils;

import com.wkclz.micro.fileos.bean.enums.ContentTypeEnum;
import com.wkclz.tool.tools.RegularTool;
import org.apache.commons.lang3.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class OssUtil {

    public static String getContentType(String originalFilename) {
        if (originalFilename == null) {
            return ContentTypeEnum.DEFAULT.getContentType();
        }
        int i = originalFilename.lastIndexOf(".");
        String subName = i > 0 ? originalFilename.substring(i) : null;
        return ContentTypeEnum.getContentTypeBySubName(subName);
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

    public static List<String> removeProAndEnCode(List<String> strs) {
        return strs.stream().map(OssUtil::removeProAndEnCode).collect(Collectors.toList());
    }

    public static String removeProAndEnCode(String str) {
        if (str == null) {
            return null;
        }

        if (str.indexOf("://") > 0) {
            str = str.substring(str.indexOf("://") + 3);
            str = str.substring(str.indexOf("/") + 1);
        }

        if (str.startsWith("/")) {
            str = str.substring(1);
        }
        if (str.startsWith("/")) {
            str = str.substring(1);
        }

        if (RegularTool.haveDoubleByte(str)) {
            StringBuilder sb = new StringBuilder();
            for (char c : str.toCharArray()) {
                if (RegularTool.isDoubleByte(c)) {
                    sb.append(URLEncoder.encode(String.valueOf(c), StandardCharsets.UTF_8));
                } else {
                    sb.append(c);
                }
            }
            str = sb.toString();
        }
        return str;
    }

    public static String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return null;
        }
        fileName = fileName.replace("/", "_");
        fileName = fileName.replace("\\", "_");
        fileName = fileName.replace("..", "_");
        fileName = fileName.replace("(", "_");
        fileName = fileName.replace(")", "_");
        fileName = fileName.replace("+", "_");
        fileName = fileName.replace(";", "_");
        fileName = fileName.replace("&", "_");
        return fileName;
    }

}
