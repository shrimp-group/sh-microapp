package com.wkclz.micro.dbview.service;

import com.wkclz.core.base.PageData;
import com.wkclz.micro.dbview.bean.entity.DbviewSqlHistory;
import com.wkclz.micro.dbview.mapper.DbviewSqlHistoryMapper;
import com.wkclz.mybatis.helper.PageQuery;
import com.wkclz.mybatis.service.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DbviewSqlHistoryService extends BaseService<DbviewSqlHistory, DbviewSqlHistoryMapper> {

    public PageData<DbviewSqlHistory> getHistoryPage(DbviewSqlHistory entity) {
        return PageQuery.page(entity, mapper::getHistoryList);
    }

    public void insertHistory(Long datasourceId, String userCode, String sql, String sqlType,
                              Integer status, Long affectedRows, Long costMs, String errorMessage) {
        log.info("SQL 执行历史落库, datasourceId={}, userCode={}, sqlType={}, status={}, costMs={}",
            datasourceId, userCode, sqlType, status, costMs);
        DbviewSqlHistory history = new DbviewSqlHistory();
        history.setDatasourceId(datasourceId);
        history.setUserCode(userCode);
        history.setSqlText(sql);
        history.setSqlType(sqlType);
        history.setStatus(status);
        history.setAffectedRows(affectedRows);
        history.setCostMs(costMs);
        history.setErrorMessage(errorMessage);
        mapper.insert(history);
    }
}
