package com.wkclz.micro.dbview.bean.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DdlExecuteRequest {

    @NotNull
    private Long datasourceId;

    private String schemaName;

    @NotBlank
    private String ddl;

    private Boolean confirmDangerous = false;
}
