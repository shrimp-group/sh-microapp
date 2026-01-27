package com.wkclz.micro.rmcheck;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan({"com.wkclz.micro.rmcheck.dao"})
@ComponentScan(basePackages = {"com.wkclz.micro.rmcheck"})
public class RmcheckAutoConfig {
}