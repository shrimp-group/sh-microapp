package com.wkclz.micro.points;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"com.wkclz.micro.points"})
@MapperScan(basePackages = {"com.wkclz.micro.points.mapper"})
public class PointsAutoConfig {
}
