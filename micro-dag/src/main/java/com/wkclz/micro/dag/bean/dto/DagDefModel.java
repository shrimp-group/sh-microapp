package com.wkclz.micro.dag.bean.dto;

import lombok.Data;

import java.util.List;

/**
 * 定义的内存装配模型（由 dag_node/dag_edge 表组装）
 */
@Data
public class DagDefModel {

    private String dagCode;
    private String dagName;
    /** 定义版本 */
    private Integer dagVersion;
    private List<NodeDef> nodes;
    private List<EdgeDef> edges;
}
