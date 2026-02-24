package com.wkclz.micro.k8s;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan({"com.wkclz.micro.k8s.dao"})
@ComponentScan(basePackages = {"com.wkclz.micro.k8s"})
public class K8sAutoConfig {
}