package com.wkclz.micro.form;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan({"com.wkclz.micro.form.dao"})
@ComponentScan(basePackages = {"com.wkclz.micro.form"})
public class FormAutoConfig {
}