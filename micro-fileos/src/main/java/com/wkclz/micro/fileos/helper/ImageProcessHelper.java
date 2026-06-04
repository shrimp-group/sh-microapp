package com.wkclz.micro.fileos.helper;

import com.wkclz.micro.fileos.bean.dto.ImageProcessParam;
import com.wkclz.micro.fileos.bean.enums.OssSpEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Component
public class ImageProcessHelper {

    public String buildOssProcessParam(ImageProcessParam param) {
        if (param == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();

        if (param.getResize() != null) {
            ImageProcessParam.ResizeParam resize = param.getResize();
            sb.append("image/resize");
            if (StringUtils.isNotBlank(resize.getMode())) {
                sb.append(",m_").append(resize.getMode());
            }
            if (resize.getWidth() != null) {
                sb.append(",w_").append(resize.getWidth());
            }
            if (resize.getHeight() != null) {
                sb.append(",h_").append(resize.getHeight());
            }
        }

        if (param.getCrop() != null) {
            if (sb.length() > 0) {
                sb.append("/");
            }
            ImageProcessParam.CropParam crop = param.getCrop();
            sb.append("image/crop");
            if (crop.getX() != null) {
                sb.append(",x_").append(crop.getX());
            }
            if (crop.getY() != null) {
                sb.append(",y_").append(crop.getY());
            }
            if (crop.getWidth() != null) {
                sb.append(",w_").append(crop.getWidth());
            }
            if (crop.getHeight() != null) {
                sb.append(",h_").append(crop.getHeight());
            }
        }

        if (param.getWatermark() != null) {
            if (sb.length() > 0) {
                sb.append("/");
            }
            ImageProcessParam.WatermarkParam watermark = param.getWatermark();
            sb.append("image/watermark");
            if (StringUtils.isNotBlank(watermark.getText())) {
                String encoded = Base64.getEncoder().encodeToString(watermark.getText().getBytes(StandardCharsets.UTF_8));
                sb.append(",text_").append(encoded);
            }
            if (StringUtils.isNotBlank(watermark.getPosition())) {
                sb.append(",g_").append(watermark.getPosition());
            }
            if (watermark.getOpacity() != null) {
                sb.append(",t_").append(watermark.getOpacity());
            }
            if (watermark.getFontSize() != null) {
                sb.append(",size_").append(watermark.getFontSize());
            }
        }

        return sb.length() > 0 ? sb.toString() : null;
    }

    public String buildProcessUrl(String baseUrl, ImageProcessParam param, String ossSp) {
        if (StringUtils.isBlank(baseUrl) || param == null) {
            return baseUrl;
        }

        String processParam = buildOssProcessParam(param);
        if (StringUtils.isBlank(processParam)) {
            return baseUrl;
        }

        if (OssSpEnum.ALI_OSS.name().equals(ossSp)) {
            String separator = baseUrl.contains("?") ? "&" : "?";
            return baseUrl + separator + "x-oss-process=" + processParam;
        }

        log.warn("图片处理参数仅支持阿里云 OSS，当前 OSS 服务商: {}，返回原始 URL", ossSp);
        return baseUrl;
    }

}
