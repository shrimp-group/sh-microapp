package com.wkclz.micro.report.service;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.report.bean.dto.ReportDefinitionDto;
import com.wkclz.micro.report.bean.entity.ReportDefinition;
import com.wkclz.micro.report.bean.entity.ReportDefinitionParam;
import com.wkclz.micro.report.bean.entity.ReportDefinitionResult;
import com.wkclz.micro.report.cache.ReportCache;
import com.wkclz.micro.report.helper.ReportSqlHelper;
import com.wkclz.micro.report.mapper.ReportDefinitionParamMapper;
import com.wkclz.micro.report.mapper.ReportDefinitionResultMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 报表执行服务
 */
@Slf4j
@Service
public class ReportExecService {

    @Autowired
    private ReportCache reportCache;
    @Autowired
    private ReportSqlHelper reportSqlHelper;
    @Autowired
    private ReportDefinitionParamMapper paramMapper;
    @Autowired
    private ReportDefinitionResultMapper resultMapper;

    private static final ScriptEngine JS_ENGINE = new ScriptEngineManager().getEngineByName("js");

    /**
     * 获取报表选项列表（用于左侧选择）
     */
    public List<ReportDefinitionDto> getReportOptions() {
        Collection<ReportDefinition> definitions = reportCache.getAll();
        return definitions.stream()
            .map(ReportDefinitionDto::copy)
            .sorted(Comparator.comparingInt(d -> d.getSort() != null ? d.getSort() : 0))
            .collect(Collectors.toList());
    }

    /**
     * 获取报表详情（含参数和结果元数据）
     */
    public ReportDefinitionDto getReportInfo(String reportCode) {
        ReportDefinition definition = reportCache.get(reportCode);
        if (definition == null) {
            throw ValidationException.of("报表不存在或未启用: " + reportCode);
        }

        ReportDefinitionDto dto = ReportDefinitionDto.copy(definition);

        // 查询参数
        ReportDefinitionParam paramQuery = new ReportDefinitionParam();
        paramQuery.setReportCode(reportCode);
        dto.setParams(paramMapper.getParamList(paramQuery));

        // 查询结果字段
        ReportDefinitionResult resultQuery = new ReportDefinitionResult();
        resultQuery.setReportCode(reportCode);
        dto.setResults(resultMapper.getResultList(resultQuery));

        return dto;
    }

    /**
     * 执行报表查询
     */
    public Object execQuery(String reportCode, Map<String, Object> params, Integer current, Integer size) {
        ReportDefinition definition = reportCache.get(reportCode);
        if (definition == null) {
            throw ValidationException.of("报表不存在或未启用: " + reportCode);
        }

        // 查询参数定义
        ReportDefinitionParam paramQuery = new ReportDefinitionParam();
        paramQuery.setReportCode(reportCode);
        List<ReportDefinitionParam> paramDefs = paramMapper.getParamList(paramQuery);

        // 参数校验
        if (params == null) {
            params = new HashMap<>();
        }
        checkParams(paramDefs, params);

        boolean toCamel = definition.getReportScriptAutocamel() != null && definition.getReportScriptAutocamel() == 1;
        String countSql = (definition.getReportScriptCountSwitch() != null && definition.getReportScriptCountSwitch() == 1)
            ? definition.getReportScriptCount() : null;

        log.info("执行报表查询: reportCode={}, resultType={}, toCamel={}", reportCode, definition.getResultType(), toCamel);
        return reportSqlHelper.sqlExecutor(definition.getResultType(), definition.getReportScript(),
            params, toCamel, countSql, current, size);
    }

    /**
     * 获取报表数据（用于导出）
     */
    public List<LinkedHashMap<String, Object>> getReportData(String reportCode, Map<String, Object> params) {
        ReportDefinition definition = reportCache.get(reportCode);
        if (definition == null) {
            throw ValidationException.of("报表不存在或未启用: " + reportCode);
        }

        // 参数校验
        ReportDefinitionParam paramQuery = new ReportDefinitionParam();
        paramQuery.setReportCode(reportCode);
        List<ReportDefinitionParam> paramDefs = paramMapper.getParamList(paramQuery);
        if (params == null) {
            params = new HashMap<>();
        }
        checkParams(paramDefs, params);

        boolean toCamel = definition.getReportScriptAutocamel() != null && definition.getReportScriptAutocamel() == 1;

        if ("OBJECT".equals(definition.getResultType())) {
            List<LinkedHashMap<String, Object>> list = reportSqlHelper.selectList(definition.getReportScript(), params, toCamel);
            return list != null && !list.isEmpty() ? Collections.singletonList(list.get(0)) : Collections.emptyList();
        } else {
            return reportSqlHelper.selectList(definition.getReportScript(), params, toCamel);
        }
    }

    /**
     * 获取报表结果字段定义（用于导出表头）
     */
    public LinkedHashMap<String, String> getExportHeaders(String reportCode) {
        ReportDefinitionResult resultQuery = new ReportDefinitionResult();
        resultQuery.setReportCode(reportCode);
        List<ReportDefinitionResult> results = resultMapper.getResultList(resultQuery);

        LinkedHashMap<String, String> headers = new LinkedHashMap<>();
        for (ReportDefinitionResult r : results) {
            headers.put(r.getFieldCode(), r.getFieldName());
        }
        return headers;
    }

    /**
     * 参数校验：必填、类型、JS 脚本
     */
    private void checkParams(List<ReportDefinitionParam> paramDefs, Map<String, Object> params) {
        for (ReportDefinitionParam paramDef : paramDefs) {
            String fieldCode = paramDef.getFieldCode();
            Object value = params.get(fieldCode);

            // 必填校验
            if (paramDef.getRequired() != null && paramDef.getRequired() == 1) {
                if (value == null || (value instanceof String && ((String) value).isEmpty())) {
                    throw ValidationException.of(paramDef.getFieldName() + " 不能为空");
                }
            }

            // 类型校验
            if (value != null && StringUtils.isNotBlank(value.toString())) {
                String fieldType = paramDef.getFieldType();
                if ("number".equals(fieldType)) {
                    try {
                        Double.parseDouble(value.toString());
                    } catch (NumberFormatException e) {
                        throw ValidationException.of(paramDef.getFieldName() + " 必须为数字");
                    }
                }
                if ("datetime".equals(fieldType) || "date".equals(fieldType)) {
                    // 日期格式由前端控制，后端仅做基本校验
                }
            }

            // JS 脚本校验
            if (StringUtils.isNotBlank(paramDef.getValidateScript()) && value != null) {
                try {
                    JS_ENGINE.put("value", value);
                    Object evalResult = JS_ENGINE.eval(paramDef.getValidateScript());
                    if (evalResult instanceof Boolean && !(Boolean) evalResult) {
                        throw ValidationException.of(paramDef.getFieldName() + " 校验不通过");
                    }
                } catch (ValidationException e) {
                    throw e;
                } catch (Exception e) {
                    log.warn("JS脚本校验异常: field={}, error={}", fieldCode, e.getMessage());
                }
            }
        }
    }

}
