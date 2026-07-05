package com.wkclz.micro.material;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan({"com.wkclz.micro.material.mapper"})
@ComponentScan(basePackages = {"com.wkclz.micro.material"})
public class MaterialAutoConfig {
}
