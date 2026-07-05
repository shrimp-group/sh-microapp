package com.wkclz.micro.dbview.bean.dto;

import lombok.Data;

import java.util.List;

@Data
public class TableDetail {
    private TableInfo tableInfo;
    private List<ColumnInfo> columns;
    private List<IndexInfo> indexes;
    private String ddl;
}
