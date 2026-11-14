package com.wkclz.micro.dag.service;

import com.wkclz.micro.dag.bean.entity.DagExecutionLog;
import com.wkclz.micro.dag.bean.enums.ExecutionEvent;
import com.wkclz.micro.dag.mapper.DagExecutionLogMapper;
import com.wkclz.mybatis.service.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class DagExecutionLogService extends BaseService<DagExecutionLog, DagExecutionLogMapper> {

    /** 写入一条执行记录（CLAIM/SUCCESS/FAIL/... 全事件统一入口） */
    public void log(String instanceCode, String nodeCode, Integer attempt,
                    ExecutionEvent event, String detailJson, String worker) {
        log.info("micro-dag: 执行记录 event={} instanceCode={} nodeCode={} attempt={}", event, instanceCode, nodeCode, attempt);
        DagExecutionLog entity = new DagExecutionLog();
        entity.setInstanceCode(instanceCode);
        entity.setNodeCode(nodeCode);
        entity.setAttempt(attempt);
        entity.setEvent(event.name());
        entity.setDetailJson(detailJson);
        entity.setWorker(worker);
        insert(entity);
    }

    /** 按节点查询执行记录（时间正序） */
    public List<DagExecutionLog> listByNode(String instanceCode, String nodeCode) {
        return mapper.listByNode(instanceCode, nodeCode);
    }
}
