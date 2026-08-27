package com.wkclz.micro.flowable.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class FlowableErrorLogProperties {

    @Value("${sh.flowable.error-log.enabled:1}")
    private int enabled;

    @Value("${sh.flowable.error-log.include-stack:1}")
    private int includeStack;

}
