package com.wkclz.micro.dag;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * micro-dag 自动配置：分布式业务编排引擎
 */
@Configuration
@EnableScheduling
@ConfigurationPropertiesScan("com.wkclz.micro.dag.config")
@MapperScan({"com.wkclz.micro.dag.mapper"})
@ComponentScan(basePackages = {"com.wkclz.micro.dag"})
public class DagAutoConfig {
}
