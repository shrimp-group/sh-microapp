package com.wkclz.micro.dict;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan({"com.wkclz.micro.dict.dao"})
@ComponentScan(basePackages = {"com.wkclz.micro.dict"})
public class DictAutoConfig {
}