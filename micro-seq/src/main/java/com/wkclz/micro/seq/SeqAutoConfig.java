package com.wkclz.micro.seq;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan({"com.wkclz.micro.seq.dao"})
@ComponentScan(basePackages = {"com.wkclz.micro.seq"})
public class SeqAutoConfig {
}