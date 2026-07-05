package com.wkclz.micro.dbview;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan({"com.wkclz.micro.dbview.mapper"})
@ComponentScan(basePackages = {"com.wkclz.micro.dbview"})
public class DbviewAutoConfig {
}
