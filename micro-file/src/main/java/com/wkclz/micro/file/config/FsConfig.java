package com.wkclz.micro.file.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class FsConfig {

    @Value("${shrimp.fs.image.max-size-mg:2}")
    private Integer imageMaxSizeMb;
    @Value("${shrimp.fs.image.extension-names:jpg,jpeg,png,gif,webp}")
    private String imageExtensionNames;

    @Value("${shrimp.fs.video.max-size-mb:100}")
    private Integer videoMaxSizeMb;
    @Value("${shrimp.fs.video.extension-names:mp4,mpeg,avi,mov,wmv,rm,rmvb}")
    private String videoExtensionNames;

}