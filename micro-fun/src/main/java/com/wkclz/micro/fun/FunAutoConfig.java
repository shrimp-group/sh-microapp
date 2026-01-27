package com.wkclz.micro.fun;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan({"com.wkclz.micro.fun.dao"})
@ComponentScan(basePackages = {"com.wkclz.micro.fun"})
public class FunAutoConfig {
}