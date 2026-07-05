package com.wkclz.micro.mask;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan({"com.wkclz.micro.mask.mapper"})
@ComponentScan(basePackages = {"com.wkclz.micro.mask"})
public class MaskAutoConfig {
}