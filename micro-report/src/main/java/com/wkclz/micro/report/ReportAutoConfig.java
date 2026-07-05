package com.wkclz.micro.report;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan({"com.wkclz.micro.report.mapper"})
@ComponentScan(basePackages = {"com.wkclz.micro.report"})
public class ReportAutoConfig {
}
