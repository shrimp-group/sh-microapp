package com.wkclz.micro.dbview.utils.schemadiff.model;

/** PER_TABLE=每张差异表一条 ALTER 多子句；PER_CHANGE=每个差异项一条独立 DDL */
public enum DdlGranularity {

    PER_TABLE,
    PER_CHANGE
}
