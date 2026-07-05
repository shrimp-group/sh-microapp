package com.wkclz.micro.dbview.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "sh.dbview")
public class DbviewConfig {

    private Integer maxRows = 1000;

    private Integer maxRowsLimit = 10000;

    private Integer sqlTimeoutSeconds = 30;

    private Integer metadataCacheTtl = 300;

    private String aesKey;

    private Integer historyRetainDays = 30;
}
