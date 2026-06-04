package com.wkclz.micro.fileos.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class FileosConfig {

    @Value("${sh.fileos.max-size-mb:50}")
    private Integer maxSizeMb;

    @Value("${sh.fileos.image.max-size-mb:10}")
    private Integer imageMaxSizeMb;
    @Value("${sh.fileos.image.extension-names:jpg,jpeg,png,gif,webp,svg,bmp}")
    private String imageExtensionNames;

    @Value("${sh.fileos.video.max-size-mb:500}")
    private Integer videoMaxSizeMb;
    @Value("${sh.fileos.video.extension-names:mp4,mpeg,avi,mov,wmv,rm,rmvb,mkv,flv}")
    private String videoExtensionNames;

    @Value("${sh.fileos.presign.expire-minutes:30}")
    private Integer presignExpireMinutes;

    @Value("${sh.fileos.presign.multipart.expire-minutes:60}")
    private Integer multipartExpireMinutes;

    @Value("${sh.fileos.presign.multipart.default-part-size-mb:5}")
    private Integer multipartDefaultPartSizeMb;

    @Value("${sh.fileos.multipart.max-age-hours:24}")
    private Integer multipartMaxAgeHours;

    @Value("${sh.fileos.hash.enabled:true}")
    private Boolean hashEnabled;

    @Value("${sh.fileos.hash.algorithm:SHA-256}")
    private String hashAlgorithm;




    @Value("${spring.application.name:demo}")
    private String system;

}
