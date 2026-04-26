package com.wkclz.micro.file.utils;

import cn.hutool.core.date.DateUtil;
import com.wkclz.iam.sdk.helper.SessionHelper;
import com.wkclz.micro.file.bean.enums.ContentTypeEnum;
import com.wkclz.spring.config.Sys;
import com.wkclz.tool.tools.RegularTool;
import com.wkclz.web.helper.RequestHelper;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Description:
 * Created: wangkaicun @ 2017-12-23 下午2:48
 */
public class OssUtil {

    /**
     * 获取文件的媒体类型
     */
    public static String getContentType(String originalFilename) {
        if (originalFilename == null) {
            return ContentTypeEnum.DEFAULT.getContentType();
        }
        int i = originalFilename.lastIndexOf(".");
        String subName = i > 0 ? originalFilename.substring(i) : null;
        return ContentTypeEnum.getContentTypeBySubName(subName);
    }

    /**
     * 生成文件的全路径
     */
    public static String getFullName(String businessType, String name) {

        if (StringUtils.isBlank(businessType)) {
            businessType = "default";
        }

        HttpServletRequest request = RequestHelper.getRequest();
        String appCode = SessionHelper.getAppCode(request);
        if (StringUtils.isBlank(appCode)) {
            appCode = "default-app";
        }
        String env = Sys.getCurrentEnv().toString().toLowerCase();
        businessType = businessType.toLowerCase();
        String day = DateUtil.format(new Date(), "yyyyMMdd");
        String path = appCode + "/" + env + "/" + businessType + "/" + day;

        String datetime = DateUtil.format(new Date(), "yyyyMMddHHmmssSSS");
        if (name == null) {
            return datetime;
        }
        name = name.replace("/", "_");
        name = name.replace("\\", "_");
        name = name.replace("..", "");
        name = name.replace("(", "_");
        name = name.replace(")", "_");
        name = name.replace("+", "_");
        name = name.replace(";", "_");
        name = name.replace("&", "_");
        name = datetime + "_" + name;

        return path + "/" + name;
    }


    /**
     * 移除域名信息，以及多余的 /, 双 Byte 字符，完成转换动作
     */
    public static List<String> removeProAndEnCode(List<String> strs) {
        return strs.stream().map(OssUtil::removeProAndEnCode).collect(Collectors.toList());
    }
    /**
     * 移除域名信息，以及多余的 /, 双 Byte 字符，完成转换动作
     */
    public static String removeProAndEnCode(String str) {
        if (str == null) {
            return null;
        }

        // 去掉域名等信息
        if (str.indexOf("://") > 0) {
            str = str.substring(str.indexOf("://") + 3);
            str = str.substring(str.indexOf("/") + 1);
        }

        // 考虑还有两 / 个的情况
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

}
