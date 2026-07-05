package com.wkclz.micro.dbview.bean.dto;

import lombok.Data;

@Data
public class IndexInfo {
    private String indexName;
    private String columnName;
    private Boolean nonUnique;
    private String indexType;
    private String comment;
    private Integer seqInIndex;
}
