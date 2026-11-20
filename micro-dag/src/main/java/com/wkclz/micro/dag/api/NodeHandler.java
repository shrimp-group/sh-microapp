package com.wkclz.micro.dag.api;

import com.wkclz.micro.dag.bean.dto.NodeParamDef;
import com.wkclz.micro.dag.bean.enums.NodeCategory;

import java.util.List;

/**
 * 业务节点处理器 SPI：业务方实现并注册为 Spring Bean 即可被编排引用
 */
public interface NodeHandler {

    /** 节点编码，全局唯一，与 dag_node.handler 对应 */
    String nodeCode();

    /** 节点名称（设计器展示用），默认取 nodeCode */
    default String nodeName() {
        return nodeCode();
    }

    /** 节点分类：系统节点永久生效，demo 节点受 dag.demo-enabled 限制，业务节点默认 BUSINESS */
    default NodeCategory category() {
        return NodeCategory.BUSINESS;
    }

    /**
     * 执行节点业务
     * ctx.checkInterrupted() 为协作式中断检查点，长任务应周期性调用
     */
    NodeResult execute(NodeContext ctx) throws Exception;

    /** 重跑前清理钩子：清理该 traceCode 历史写入的数据 */
    default void cleanup(NodeContext ctx) {
    }

    /** 节点参数定义：随 handler/list 下发渲染表单，并在保存/发布时参与校验；缺省无参数 */
    default List<NodeParamDef> paramSchema() {
        return List.of();
    }
}
