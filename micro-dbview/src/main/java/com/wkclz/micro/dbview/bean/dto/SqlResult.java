package com.wkclz.micro.dbview.bean.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SqlResult {
    private List<String> columns;
    private List<Map<String, Object>> rows;
    private Long total;
    private Long affectedRows;
    private Long costMs;
    private String sqlType;
    private Boolean truncated;
}
