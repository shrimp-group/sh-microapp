package com.wkclz.micro.points.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class PointConfig {


    @Value("${micro.points.expire.enabled:0}")
    private Integer pointsExpireEnabled;


}
