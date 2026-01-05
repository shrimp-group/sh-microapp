package com.wkclz.micro.wxapp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@ComponentScan(basePackages = {"com.wkclz.micro.wxapp"})
@MapperScan(basePackages = {"com.wkclz.micro.wxapp.mapper"})
public class MicroAppAutoConfig {
}


