package com.wkclz.micro.dbview.utils.schemadiff.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/** 单表差异，保留两侧结构引用供 DDL 生成使用 */
@Data
public class TableDiff {
    private String tableName;
    private DiffStatus status;
    private List<DiffItem> items = new ArrayList<>();
    private TableStructure baseStructure;
    private TableStructure otherStructure;

    public static TableDiff onlyBase(TableStructure base) {
        TableDiff td = new TableDiff();
        td.setTableName(base.getTableName());
        td.setStatus(DiffStatus.ONLY_BASE);
        td.setBaseStructure(base);
        td.getItems().add(DiffItem.of(DiffScope.TABLE, DiffAction.ADDED, base.getTableName()));
        return td;
    }

    public static TableDiff onlyOther(TableStructure other) {
        TableDiff td = new TableDiff();
        td.setTableName(other.getTableName());
        td.setStatus(DiffStatus.ONLY_OTHER);
        td.setOtherStructure(other);
        td.getItems().add(DiffItem.of(DiffScope.TABLE, DiffAction.REMOVED, other.getTableName()));
        return td;
    }
}
