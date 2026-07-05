package com.wkclz.micro.dbview.bean.dto;

import lombok.Data;

@Data
public class TableInfo {
    private String tableName;
    private String tableComment;
    private String engine;
    private Long tableRows;
    private Long dataLength;
    private String createTime;
    private String updateTime;
}
