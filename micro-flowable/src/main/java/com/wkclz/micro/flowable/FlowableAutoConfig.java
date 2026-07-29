package com.wkclz.micro.flowable;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan({"com.wkclz.micro.flowable.mapper"})
@ComponentScan(basePackages = {"com.wkclz.micro.flowable"})
public class FlowableAutoConfig {
}
