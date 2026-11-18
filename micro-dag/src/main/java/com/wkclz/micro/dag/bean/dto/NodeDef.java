package com.wkclz.micro.dag.bean.dto;

import com.wkclz.micro.dag.bean.enums.NodeType;
import lombok.Data;

import java.util.Map;

/** 节点定义（与 dag_node 行一一对应），节点类型见 {@link NodeType} */
@Data
public class NodeDef {

    private String code;
    private String name;
    /** 节点类型 NORMAL/START/END/BRANCH，缺省 NORMAL；特殊节点免 handler，运行时领取后直达成功 */
    private String nodeType;
    /** NodeHandler 编码 */
    private String handler;
    /** 节点超时 ms，null 或 <=0 表示不限制 */
    private Long timeoutMs;
    /** 最大重试次数 */
    private Integer maxRetry;
    /** 重试间隔秒 */
    private Integer intervalSec;
    /** FIXED / EXPONENTIAL，缺省 FIXED */
    private String backoff;
    /** 节点输入参数（JSON 对象） */
    private Map<String, Object> params;
    /** 画布坐标 X */
    private Integer posX;
    /** 画布坐标 Y */
    private Integer posY;
}
