package com.wkclz.micro.dbview.utils.schemadiff.model;

import lombok.Data;

/**
 * 字段定义。extra 仅存 on update 附加属性（auto_increment 已剥离为独立字段）。
 * defaultValue 为 null 表示无默认值；字段级 charset/collate 为 null 表示继承表。
 */
@Data
public class ColumnDef {
    private String columnName;
    private String columnType;
    private Boolean nullable = true;
    private String defaultValue;
    private String columnComment;
    private boolean autoIncrement;
    private String extra;
    private String columnCharset;
    private String columnCollate;
    private int ordinalPosition;
}
