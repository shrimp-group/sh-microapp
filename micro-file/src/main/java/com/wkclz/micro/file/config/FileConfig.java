package com.wkclz.micro.file.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class FileConfig {

    @Value("${sh.file.image.max-size-mb:2}")
    private Integer imageMaxSizeMb;
    @Value("${sh.file.image.extension-names:jpg,jpeg,png,gif,webp}")
    private String imageExtensionNames;

    @Value("${sh.file.video.max-size-mb:100}")
    private Integer videoMaxSizeMb;
    @Value("${sh.file.video.extension-names:mp4,mpeg,avi,mov,wmv,rm,rmvb}")
    private String videoExtensionNames;

    @Value("${sh.file.max-size-mb:50}")
    private Integer maxSizeMb;

}
