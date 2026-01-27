package com.wkclz.micro.liteflow;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan({"com.wkclz.micro.liteflow.dao"})
@ComponentScan(basePackages = {"com.wkclz.micro.liteflow"})
public class LiteFlowAutoConfig {
}