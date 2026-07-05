package com.wkclz.micro.dbview.bean.dto;

import com.wkclz.micro.dbview.bean.enums.DdlType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DdlRequest {

    @NotNull
    private Long datasourceId;

    private String schemaName;

    @NotBlank
    private String tableName;

    @NotNull
    private DdlType ddlType;

    private ColumnDefinition column;

    private IndexDefinition index;

    private String newTableName;

    private String tableComment;

    private String columnComment;
}
