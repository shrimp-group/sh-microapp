package com.wkclz.micro.dbview.utils.schemadiff.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/** 对比总结果，统计字段由 SchemaDiffUtil.diff 填充 */
@Data
public class DiffResult {
    private List<TableDiff> tables = new ArrayList<>();
    private int totalTables;
    private int sameTables;
    private int changedTables;
    private int onlyBaseTables;
    private int onlyOtherTables;
    private int itemCount;
}
