package com.wkclz.micro.report.service;

import com.wkclz.core.base.PageData;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.report.bean.dto.ReportDefinitionDto;
import com.wkclz.micro.report.bean.entity.ReportDefinition;
import com.wkclz.micro.report.bean.entity.ReportDefinitionHis;
import com.wkclz.micro.report.cache.ReportCache;
import com.wkclz.micro.report.mapper.ReportDefinitionHisMapper;
import com.wkclz.micro.report.mapper.ReportDefinitionMapper;
import com.wkclz.micro.report.mapper.ReportDefinitionParamMapper;
import com.wkclz.micro.report.mapper.ReportDefinitionResultMapper;
import com.github.pagehelper.PageHelper;
import com.wkclz.mybatis.service.BaseService;
import com.wkclz.spring.helper.SnowflakeHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class ReportDefinitionService extends BaseService<ReportDefinition, ReportDefinitionMapper> {

    @Autowired
    private ReportDefinitionHisMapper hisMapper;
    @Autowired
    private ReportDefinitionParamMapper paramMapper;
    @Autowired
    private ReportDefinitionResultMapper resultMapper;
    @Autowired
    private ReportCache reportCache;
    @Autowired
    private com.wkclz.micro.report.helper.ReportSqlHelper reportSqlHelper;

    /**
     * 分页查询报表定义
     */
    public PageData<ReportDefinitionDto> getDefinitionPage(ReportDefinition entity) {
        entity.init();
        PageHelper.startPage(entity.getCurrent().intValue(), entity.getSize().intValue());
        List<ReportDefinitionDto> list = mapper.getDefinitionList(entity);
        com.github.pagehelper.Page<ReportDefinitionDto> page = (com.github.pagehelper.Page<ReportDefinitionDto>) list;
        return PageData.of(page.getResult(), page.getTotal(), (long) page.getPageNum(), (long) page.getPageSize());
    }

    /**
     * 查看报表定义详情
     */
    public ReportDefinition getDefinitionDetail(Long id) {
        return selectById(id);
    }

    /**
     * 新增报表定义
     */
    @Transactional(rollbackFor = Exception.class)
    public ReportDefinition definitionCreate(ReportDefinition entity) {
        // 自动生成 reportCode
        if (StringUtils.isBlank(entity.getReportCode())) {
            entity.setReportCode("rpt_" + SnowflakeHelper.getSnowflakeId());
        }
        // 默认值
        if (entity.getResultType() == null) { entity.setResultType("PAGE"); }
        if (entity.getEnableFlag() == null) { entity.setEnableFlag(1); }
        if (entity.getReportScriptCountSwitch() == null) { entity.setReportScriptCountSwitch(0); }
        if (entity.getReportScriptAutocamel() == null) { entity.setReportScriptAutocamel(1); }

        insert(entity);
        log.info("新增报表定义: reportCode={}, reportName={}", entity.getReportCode(), entity.getReportName());

        // 写入历史表
        writeHistory(entity);
        reportCache.clearCache();
        return entity;
    }

    /**
     * 修改报表定义
     */
    @Transactional(rollbackFor = Exception.class)
    public ReportDefinition definitionUpdate(ReportDefinition entity) {
        ReportDefinition existing = selectById(entity.getId());
        if (existing == null) {
            throw ValidationException.of("数据不存在");
        }

        // 如果 reportCode 变更，级联更新 param 和 result 表
        if (!existing.getReportCode().equals(entity.getReportCode())) {
            paramMapper.updateReportCodeBatch(existing.getReportCode(), entity.getReportCode());
            resultMapper.updateReportCodeBatch(existing.getReportCode(), entity.getReportCode());
            log.info("报表编码变更: {} -> {}", existing.getReportCode(), entity.getReportCode());
        }

        updateById(entity);
        log.info("修改报表定义: reportCode={}", entity.getReportCode());

        // 写入历史表
        ReportDefinition updated = selectById(entity.getId());
        writeHistory(updated);
        reportCache.clearCache();
        return updated;
    }

    /**
     * 删除报表定义（级联删除参数和结果字段）
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer definitionRemove(ReportDefinition entity) {
        ReportDefinition existing = selectById(entity.getId());
        if (existing == null) {
            throw ValidationException.of("数据不存在");
        }

        // 级联删除参数和结果字段
        paramMapper.deleteByReportCode(existing.getReportCode());
        resultMapper.deleteByReportCode(existing.getReportCode());
        log.info("级联删除报表参数和结果字段: reportCode={}", existing.getReportCode());

        Integer result = deleteById(entity);
        reportCache.clearCache();
        log.info("删除报表定义: reportCode={}", existing.getReportCode());
        return result;
    }

    /**
     * SQL 测试
     */
    public Object definitionTest(String reportCode, String resultType, String reportScript,
                                  String reportScriptCount, Integer reportScriptCountSwitch,
                                  Integer reportScriptAutocamel, Integer current, Integer size,
                                  java.util.Map<String, Object> params) {
        boolean toCamel = reportScriptAutocamel != null && reportScriptAutocamel == 1;
        String countSql = (reportScriptCountSwitch != null && reportScriptCountSwitch == 1) ? reportScriptCount : null;

        log.info("SQL测试: reportCode={}, resultType={}, toCamel={}", reportCode, resultType, toCamel);
        return reportSqlHelper.sqlExecutor(resultType, reportScript, params, toCamel, countSql, current, size);
    }

    /**
     * 写入历史表
     */
    private void writeHistory(ReportDefinition entity) {
        ReportDefinitionHis his = new ReportDefinitionHis();
        his.setDataId(entity.getId());
        his.setReportCode(entity.getReportCode());
        his.setReportName(entity.getReportName());
        his.setResultType(entity.getResultType());
        his.setEnableFlag(entity.getEnableFlag());
        his.setReportScript(entity.getReportScript());
        his.setReportScriptCountSwitch(entity.getReportScriptCountSwitch());
        his.setReportScriptCount(entity.getReportScriptCount());
        his.setReportScriptAutocamel(entity.getReportScriptAutocamel());
        his.setSort(entity.getSort());
        his.setRemark(entity.getRemark());
        hisMapper.insert(his);
    }

}
