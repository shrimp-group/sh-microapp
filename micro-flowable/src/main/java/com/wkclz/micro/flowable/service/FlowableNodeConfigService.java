package com.wkclz.micro.flowable.service;

import com.wkclz.core.base.BaseEntity;
import com.wkclz.micro.flowable.bean.entity.FlowableNodeConfig;
import com.wkclz.micro.flowable.bean.enums.NodeType;
import com.wkclz.micro.flowable.mapper.FlowableNodeConfigMapper;
import com.wkclz.mybatis.service.BaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FlowableNodeConfigService extends BaseService<FlowableNodeConfig, FlowableNodeConfigMapper> {

    private static final Logger log = LoggerFactory.getLogger(FlowableNodeConfigService.class);

    /**
     * 解析 BPMN XML 并与存量节点配置差量同步：按 nodeKey 对比，新增缺失节点、
     * 删除多余节点、更新名称/类型/顺序变更的节点（保留人工审批配置）。
     * 事务内执行 insertBatch + updateByIdSelective + deleteByIds（同 micro-dict 差量同步模式）。
     * 使用 Flowable BPMN Model API（flowable-bpmn-model 由 sh-flowable-client 传递依赖引入）。
     * 若解析失败仅记录日志，不阻塞设计保存。
     */
    public void syncFromXml(String designCode, String xmlContent) {
        List<FlowableNodeConfig> parsedNodes;
        try {
            parsedNodes = parseNodes(xmlContent);
        } catch (Exception e) {
            log.warn("解析 BPMN XML 失败，跳过节点差量同步: designCode={}", designCode, e);
            return;
        }
        log.info("节点配置差量同步开始: designCode={}, xmlNodes={}", designCode, parsedNodes.size());

        Map<String, FlowableNodeConfig> oldMap = selectByDesignCode(designCode).stream()
                .filter(n -> n.getNodeKey() != null)
                .collect(Collectors.toMap(FlowableNodeConfig::getNodeKey, n -> n, (a, b) -> a));

        List<FlowableNodeConfig> inserts = new ArrayList<>();
        List<FlowableNodeConfig> updates = new ArrayList<>();
        Set<String> parsedKeys = new HashSet<>();
        for (FlowableNodeConfig parsed : parsedNodes) {
            parsedKeys.add(parsed.getNodeKey());
            FlowableNodeConfig old = oldMap.get(parsed.getNodeKey());
            if (old == null) {
                parsed.setDesignCode(designCode);
                // insertBatch 空值也显式插入，NOT NULL 列 sort 需给定默认值
                parsed.setSort(0);
                inserts.add(parsed);
                continue;
            }
            boolean changed = false;
            if (!Objects.equals(parsed.getNodeName(), old.getNodeName())) {
                old.setNodeName(parsed.getNodeName());
                changed = true;
            }
            if (!Objects.equals(parsed.getNodeType(), old.getNodeType())) {
                old.setNodeType(parsed.getNodeType());
                changed = true;
            }
            if (!Objects.equals(parsed.getOrderNum(), old.getOrderNum())) {
                old.setOrderNum(parsed.getOrderNum());
                changed = true;
            }
            if (changed) {
                updates.add(old);
            }
        }
        List<Long> deleteIds = oldMap.values().stream()
                .filter(n -> !parsedKeys.contains(n.getNodeKey()))
                .map(BaseEntity::getId)
                .toList();

        if (!inserts.isEmpty()) {
            insertBatch(inserts);
        }
        for (FlowableNodeConfig update : updates) {
            updateByIdSelective(update);
        }
        if (!deleteIds.isEmpty()) {
            FlowableNodeConfig delParam = new FlowableNodeConfig();
            delParam.setIds(deleteIds);
            deleteByIds(delParam);
        }
        log.info("节点配置差量同步完成: designCode={}, 新增={}, 更新={}, 删除={}", designCode, inserts.size(), updates.size(), deleteIds.size());
    }

    public void deleteByDesignCode(String designCode) {
        log.info("删除设计全部节点配置: designCode={}", designCode);
        List<Long> ids = selectByDesignCode(designCode).stream()
                .map(BaseEntity::getId)
                .toList();
        if (ids.isEmpty()) {
            return;
        }
        FlowableNodeConfig delParam = new FlowableNodeConfig();
        delParam.setIds(ids);
        deleteByIds(delParam);
    }

    private List<FlowableNodeConfig> selectByDesignCode(String designCode) {
        FlowableNodeConfig param = new FlowableNodeConfig();
        param.setDesignCode(designCode);
        return selectByEntity(param);
    }

    private List<FlowableNodeConfig> parseNodes(String xmlContent) {
        org.flowable.bpmn.model.BpmnModel model = new org.flowable.bpmn.converter.BpmnXMLConverter()
                .convertToBpmnModel(
                        () -> new java.io.ByteArrayInputStream(xmlContent.getBytes(java.nio.charset.StandardCharsets.UTF_8)),
                        false, false);
        List<FlowableNodeConfig> nodes = new ArrayList<>();
        int order = 0;
        for (org.flowable.bpmn.model.Process process : model.getProcesses()) {
            for (org.flowable.bpmn.model.FlowElement element : process.getFlowElements()) {
                if (element.getId() == null) {
                    continue;
                }
                FlowableNodeConfig node = new FlowableNodeConfig();
                node.setNodeKey(element.getId());
                node.setNodeName(element.getName() != null ? element.getName() : element.getId());
                node.setOrderNum(order++);
                if (element instanceof org.flowable.bpmn.model.StartEvent) {
                    node.setNodeType(NodeType.START.name());
                } else if (element instanceof org.flowable.bpmn.model.UserTask) {
                    node.setNodeType(NodeType.APPROVAL.name());
                } else if (element instanceof org.flowable.bpmn.model.Gateway) {
                    node.setNodeType(NodeType.GATEWAY.name());
                } else if (element instanceof org.flowable.bpmn.model.EndEvent) {
                    node.setNodeType(NodeType.END.name());
                } else {
                    node.setNodeType(NodeType.APPROVAL.name());
                }
                nodes.add(node);
            }
        }
        return nodes;
    }
}
