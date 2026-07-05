package com.wkclz.micro.audit;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan({"com.wkclz.micro.audit.mapper"})
@ComponentScan(basePackages = {"com.wkclz.micro.audit"})
public class AuditAutoConfig {
}