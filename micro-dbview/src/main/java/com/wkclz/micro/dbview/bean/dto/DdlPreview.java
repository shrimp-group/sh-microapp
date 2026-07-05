package com.wkclz.micro.dbview.bean.dto;

import lombok.Data;

@Data
public class DdlPreview {
    private String ddl;
    private Long datasourceId;
    private String schemaName;
    private String tableName;
}
