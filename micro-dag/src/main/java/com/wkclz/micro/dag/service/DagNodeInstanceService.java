package com.wkclz.micro.dag.service;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.dag.bean.entity.DagExecutionLog;
import com.wkclz.micro.dag.bean.entity.DagNodeInstance;
import com.wkclz.micro.dag.bean.resp.ExecutionLogResp;
import com.wkclz.micro.dag.bean.resp.NodeDetailResp;
import com.wkclz.micro.dag.bean.resp.NodeInstanceResp;
import com.wkclz.micro.dag.helper.DagKeys;
import com.wkclz.micro.dag.mapper.DagNodeInstanceMapper;
import com.wkclz.mybatis.service.BaseService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class DagNodeInstanceService extends BaseService<DagNodeInstance, DagNodeInstanceMapper> {

    @Resource
    private DagExecutionLogService logService;

    @PostConstruct
    public void init() {
        log.info("micro-dag: 初始化节点实例服务");
    }

    /** 节点详情：实例信息 + 执行记录时间线（按 instanceCode + nodeCode 定位） */
    public NodeDetailResp nodeDetail(String instanceCode, String nodeCode) {
        log.info("micro-dag: 节点详情 instanceCode={} nodeCode={}", instanceCode, nodeCode);
        DagNodeInstance where = new DagNodeInstance();
        where.setInstanceCode(instanceCode);
        where.setNodeCode(nodeCode);
        DagNodeInstance node = selectOneByEntity(where);
        if (node == null) {
            throw ValidationException.of("节点实例不存在: " + DagKeys.instanceNodeKey(instanceCode, nodeCode));
        }
        NodeInstanceResp nodeResp = new NodeInstanceResp();
        BeanUtils.copyProperties(node, nodeResp);

        List<ExecutionLogResp> logs = logService.listByNode(node.getInstanceCode(), node.getNodeCode())
            .stream().map(this::toLogResp).toList();

        NodeDetailResp resp = new NodeDetailResp();
        resp.setNode(nodeResp);
        resp.setLogs(logs);
        return resp;
    }

    private ExecutionLogResp toLogResp(DagExecutionLog l) {
        ExecutionLogResp r = new ExecutionLogResp();
        BeanUtils.copyProperties(l, r);
        return r;
    }
}
