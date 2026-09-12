package com.wkclz.micro.dbview.utils.schemadiff.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 索引定义。primary=true 表示主键（indexName=PRIMARY）。
 * columnNames 保留前缀长度表达式，如 col(191)。
 */
@Data
public class IndexDef {
    private String indexName;
    private boolean unique;
    private boolean primary;
    private List<String> columnNames = new ArrayList<>();
    private String indexType;
    private String comment;
}
