package com.wkclz.micro.pdf.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class PdfConfig {

    @Value("${shrimp.pdf.simsun.path:/usr/share/fonts/zh/simsun.ttf}")
    private String simsunPath;

}
