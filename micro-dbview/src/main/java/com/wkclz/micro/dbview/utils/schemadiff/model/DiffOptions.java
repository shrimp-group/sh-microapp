package com.wkclz.micro.dbview.utils.schemadiff.model;

import lombok.Data;

/** 对比开关：凡是能由 DDL 统一的属性均可开关，默认全部开启 */
@Data
public class DiffOptions {
    private boolean compareTableComment = true;
    private boolean compareEngine = true;
    private boolean compareAutoIncrement = true;
    private boolean compareCharset = true;
    private boolean compareCollate = true;
    private boolean compareRowFormat = true;

    private boolean compareColumnType = true;
    private boolean compareNullable = true;
    private boolean compareDefaultValue = true;
    private boolean compareColumnComment = true;
    private boolean compareColumnAutoIncrement = true;
    private boolean compareOnUpdate = true;
    private boolean compareColumnCharset = true;
    private boolean comparePosition = true;

    private boolean compareIndex = true;

    /** 对比侧多出的表是否生成 DROP TABLE，默认 false */
    private boolean dropMissingTable = false;
    private DdlGranularity ddlGranularity = DdlGranularity.PER_TABLE;
}
