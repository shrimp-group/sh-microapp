package com.wkclz.micro.dbview.bean.dto;

import lombok.Data;

@Data
public class ColumnDefinition {
    private String columnName;
    private String columnType;
    private Boolean nullable = true;
    private String defaultValue;
    private String columnComment;
    private String afterColumn;
    private String oldColumnName;
}
