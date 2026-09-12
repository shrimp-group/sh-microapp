package com.wkclz.micro.dbview.utils.schemadiff.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 表结构中立模型，供 DdlParseUtil 解析产出、SchemaDiffUtil 对比消费。
 * 可移植：仅依赖 sh-core 的 ValidationException、slf4j-api 与 lombok，便于整包移植。
 */
@Data
public class TableStructure {
    private String tableName;
    private String tableComment;
    private String engine;
    private Long autoIncrement;
    private String charset;
    private String collate;
    private String rowFormat;
    private List<ColumnDef> columns = new ArrayList<>();
    private List<IndexDef> indexes = new ArrayList<>();
    /** 解析前的原始 CREATE TABLE 语句，生成"新增表"DDL 时直接使用 */
    private String originalDdl;

    public ColumnDef getColumn(String columnName) {
        return columns.stream().filter(c -> c.getColumnName().equals(columnName)).findFirst().orElse(null);
    }

    public IndexDef getIndex(String indexName) {
        return indexes.stream().filter(i -> i.getIndexName().equals(indexName)).findFirst().orElse(null);
    }
}
