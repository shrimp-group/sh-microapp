package com.wkclz.micro.msg;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan({"com.wkclz.micro.msg.dao"})
@ComponentScan(basePackages = {"com.wkclz.micro.msg"})
public class MsgAutoConfig {
}