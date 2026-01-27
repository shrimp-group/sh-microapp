package com.wkclz.micro.file;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan({"com.wkclz.micro.file.dao"})
@ComponentScan(basePackages = {"com.wkclz.micro.file"})
public class FileAutoConfig {
}