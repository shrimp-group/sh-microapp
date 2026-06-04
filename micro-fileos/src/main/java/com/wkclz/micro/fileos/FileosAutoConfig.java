package com.wkclz.micro.fileos;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"com.wkclz.micro.fileos"})
@MapperScan(basePackages = {"com.wkclz.micro.fileos.mapper"})
public class FileosAutoConfig {
}
