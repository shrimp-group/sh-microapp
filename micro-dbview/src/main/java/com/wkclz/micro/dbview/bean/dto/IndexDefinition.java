package com.wkclz.micro.dbview.bean.dto;

import lombok.Data;

import java.util.List;

@Data
public class IndexDefinition {
    private String indexName;
    private List<String> columnNames;
    private Boolean unique = false;
    private String indexType;
    private String comment;
}
