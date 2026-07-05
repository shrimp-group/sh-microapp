package com.wkclz.micro.wxmp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan({"com.wkclz.micro.wxmp.mapper"})
@ComponentScan(basePackages = {"com.wkclz.micro.wxmp"})
public class WxmpAutoConfig {
}