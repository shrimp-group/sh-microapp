package com.wkclz.micro.pdf;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan({"com.wkclz.micro.pdf.dao"})
@ComponentScan(basePackages = {"com.wkclz.micro.pdf"})
public class PdfAutoConfig {
}