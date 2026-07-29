package com.wkclz.micro.flowable.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "sh.flowable.error-log")
public class FlowableErrorLogProperties {
    private boolean enabled = true;
    private boolean includeStack = true;
}
