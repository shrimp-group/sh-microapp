package com.wkclz.micro.pay;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan({"com.wkclz.micro.pay.dao"})
@ComponentScan(basePackages = {"com.wkclz.micro.pay"})
public class PayAutoConfig {
}