package com.wkclz.micro.fileos.bean.dto;

import lombok.Data;

@Data
public class ImageProcessParam {

    private ResizeParam resize;
    private CropParam crop;
    private WatermarkParam watermark;

    @Data
    public static class ResizeParam {
        private Integer width;
        private Integer height;
        private String mode;
    }

    @Data
    public static class CropParam {
        private Integer x;
        private Integer y;
        private Integer width;
        private Integer height;
    }

    @Data
    public static class WatermarkParam {
        private String text;
        private String position;
        private Integer opacity;
        private Integer fontSize;
    }
}
