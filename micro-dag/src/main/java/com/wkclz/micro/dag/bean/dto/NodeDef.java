package com.wkclz.micro.dag.bean.dto;

import com.wkclz.micro.dag.bean.enums.NodeType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/** 节点定义（与 dag_node 行一一对应），节点类型见 {@link NodeType} */
@Schema(description = "节点定义（与 dag_node 行一一对应）")
@Data
public class NodeDef {

    @Schema(description = "节点编码")
    private String code;
    @Schema(description = "节点名称")
    private String name;
    /** 节点类型 NORMAL/START/END/BRANCH，缺省 NORMAL；特殊节点免 handler，运行时领取后直达成功 */
    @Schema(description = "节点类型 NORMAL/START/END/BRANCH，缺省 NORMAL；特殊节点免 handler，运行时领取后直达成功")
    private String nodeType;
    /** NodeHandler 编码 */
    @Schema(description = "NodeHandler 编码")
    private String handler;
    /** 节点超时 ms，null 或 <=0 表示不限制 */
    @Schema(description = "节点超时 ms，null 或 <=0 表示不限制")
    private Long timeoutMs;
    /** 最大重试次数 */
    @Schema(description = "最大重试次数")
    private Integer maxRetry;
    /** 重试间隔秒 */
    @Schema(description = "重试间隔秒")
    private Integer intervalSec;
    /** FIXED / EXPONENTIAL，缺省 FIXED */
    @Schema(description = "退避策略 FIXED/EXPONENTIAL，缺省 FIXED")
    private String backoff;
    /** 节点输入参数（JSON 对象） */
    @Schema(description = "节点输入参数（JSON 对象）")
    private Map<String, Object> params;
    /** 画布坐标 X */
    @Schema(description = "画布坐标 X")
    private Integer posX;
    /** 画布坐标 Y */
    @Schema(description = "画布坐标 Y")
    private Integer posY;
}
