package com.wkclz.micro.report.service;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.report.bean.dto.ReportDefinitionResultDto;
import com.wkclz.micro.report.bean.entity.ReportDefinitionResult;
import com.wkclz.micro.report.helper.ReportSqlHelper;
import com.wkclz.micro.report.mapper.ReportDefinitionResultMapper;
import com.wkclz.mybatis.service.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class ReportDefinitionResultService extends BaseService<ReportDefinitionResult, ReportDefinitionResultMapper> {

    @Autowired
    private ReportSqlHelper reportSqlHelper;

    /**
     * 查询结果字段列表
     */
    public List<ReportDefinitionResult> getResultList(String reportCode) {
        ReportDefinitionResult result = new ReportDefinitionResult();
        result.setReportCode(reportCode);
        return mapper.getResultList(result);
    }

    public List<ReportDefinitionResult> getResultList(ReportDefinitionResult result) {
        return mapper.getResultList(result);
    }

    /**
     * 新增结果字段
     */
    public ReportDefinitionResult resultCreate(ReportDefinitionResult entity) {
        // 默认值
        if (entity.getFieldType() == null) { entity.setFieldType("string"); }
        if (entity.getFieldFormType() == null) { entity.setFieldFormType("TEXT"); }
        if (entity.getWidth() == null) { entity.setWidth(100); }

        insert(entity);
        log.info("新增报表结果字段: reportCode={}, fieldCode={}", entity.getReportCode(), entity.getFieldCode());
        return entity;
    }

    /**
     * 修改结果字段
     */
    public ReportDefinitionResult resultUpdate(ReportDefinitionResult entity) {
        updateById(entity);
        log.info("修改报表结果字段: reportCode={}, fieldCode={}", entity.getReportCode(), entity.getFieldCode());
        return entity;
    }

    /**
     * 删除结果字段
     */
    public Integer resultRemove(ReportDefinitionResult entity) {
        Integer result = deleteById(entity);
        log.info("删除报表结果字段: id={}", entity.getId());
        return result;
    }

    /**
     * 从 SQL 脚本自动提取结果字段
     * 已保存的结果字段保留在列表中，新增的字段排在后面
     */
    public List<ReportDefinitionResult> resultsExtract(ReportDefinitionResultDto dto) {
        if (StringUtils.isBlank(dto.getReportScript())) {
            throw ValidationException.of("SQL脚本不能为空");
        }

        List<String> columnNames = reportSqlHelper.sql2Results(dto.getReportScript());
        if (columnNames.isEmpty()) {
            // 即使没有提取到新字段，也返回已有字段
            return getResultList(dto.getReportCode());
        }

        // 查询已有结果字段
        List<ReportDefinitionResult> existingResults = getResultList(dto.getReportCode());

        boolean toCamel = dto.getReportScriptAutocamel() != null && dto.getReportScriptAutocamel() == 1;

        // 构建结果：已有字段 + 新提取的字段
        Set<String> processedCodes = new LinkedHashSet<>();
        List<ReportDefinitionResult> result = new ArrayList<>();

        // 先添加已有字段（保持原有顺序）
        for (ReportDefinitionResult existing : existingResults) {
            if (existing.getFieldCode() != null) {
                processedCodes.add(existing.getFieldCode());
                result.add(existing);
            }
        }

        // 再添加新提取的字段
        for (String colName : columnNames) {
            String fieldName = toCamel ? toCamelCase(colName) : colName;
            if (processedCodes.contains(fieldName)) {
                continue;
            }
            processedCodes.add(fieldName);
            ReportDefinitionResult r = new ReportDefinitionResult();
            r.setReportCode(dto.getReportCode());
            r.setFieldCode(fieldName);
            r.setFieldName(fieldName);
            r.setFieldType("string");
            r.setFieldFormType("TEXT");
            r.setWidth(100);
            r.setSort(result.size() + 1);
            result.add(r);
        }

        log.info("从SQL提取结果字段: reportCode={}, 已有={}, 新提取={}", dto.getReportCode(), existingResults.size(), result.size() - existingResults.size());
        return result;
    }

    /**
     * 下划线转驼峰
     */
    private String toCamelCase(String snakeCase) {
        if (snakeCase == null || !snakeCase.contains("_")) {
            return snakeCase;
        }
        StringBuilder sb = new StringBuilder();
        boolean nextUpper = false;
        for (char c : snakeCase.toCharArray()) {
            if (c == '_') {
                nextUpper = true;
            } else {
                if (nextUpper) {
                    sb.append(Character.toUpperCase(c));
                    nextUpper = false;
                } else {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }

}
