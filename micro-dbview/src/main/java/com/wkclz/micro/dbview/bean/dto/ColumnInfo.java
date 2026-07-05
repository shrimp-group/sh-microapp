package com.wkclz.micro.dbview.bean.dto;

import lombok.Data;

@Data
public class ColumnInfo {
    private String tableName;
    private String columnName;
    private String columnType;
    private String columnComment;
    private Boolean nullable;
    private String defaultValue;
    private Boolean isPrimaryKey;
    private Integer ordinalPosition;
    private String extra;
}
