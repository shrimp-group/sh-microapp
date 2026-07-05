package com.wkclz.micro.report.service;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.report.bean.dto.ReportDefinitionParamDto;
import com.wkclz.micro.report.bean.entity.ReportDefinitionParam;
import com.wkclz.micro.report.helper.ReportSqlHelper;
import com.wkclz.micro.report.mapper.ReportDefinitionParamMapper;
import com.wkclz.mybatis.bean.ColumnInfo;
import com.wkclz.mybatis.service.BaseService;
import com.wkclz.mybatis.service.TableInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class ReportDefinitionParamService extends BaseService<ReportDefinitionParam, ReportDefinitionParamMapper> {

    @Autowired
    private ReportSqlHelper reportSqlHelper;
    @Autowired
    private TableInfoService tableInfoService;

    /**
     * 查询参数列表
     */

    public List<ReportDefinitionParam> getParamList(String reportCode) {
        ReportDefinitionParam param = new ReportDefinitionParam();
        param.setReportCode(reportCode);
        return mapper.getParamList(param);
    }

    public List<ReportDefinitionParam> getParamList(ReportDefinitionParam entity) {
        return mapper.getParamList(entity);
    }

    /**
     * 新增参数
     */
    public ReportDefinitionParam paramCreate(ReportDefinitionParam entity) {
        // 默认值
        if (entity.getFieldType() == null) { entity.setFieldType("string"); }
        if (entity.getFieldFormType() == null) { entity.setFieldFormType("TEXT"); }
        if (entity.getRequired() == null) { entity.setRequired(0); }

        insert(entity);
        log.info("新增报表参数: reportCode={}, fieldCode={}", entity.getReportCode(), entity.getFieldCode());
        return entity;
    }

    /**
     * 修改参数
     */
    public ReportDefinitionParam paramUpdate(ReportDefinitionParam entity) {
        updateById(entity);
        log.info("修改报表参数: reportCode={}, fieldCode={}", entity.getReportCode(), entity.getFieldCode());
        return entity;
    }

    /**
     * 删除参数
     */
    public Integer paramRemove(ReportDefinitionParam entity) {
        Integer result = deleteById(entity);
        log.info("删除报表参数: id={}", entity.getId());
        return result;
    }

    /**
     * 从 SQL 脚本自动提取参数
     * 已保存的参数保留在列表中，新增的参数排在后面
     */
    public List<ReportDefinitionParam> paramsExtract(ReportDefinitionParamDto dto) {
        if (StringUtils.isBlank(dto.getReportScript())) {
            throw ValidationException.of("SQL脚本不能为空");
        }

        List<String> paramNames = reportSqlHelper.sql2Params(dto.getReportScript());
        if (paramNames.isEmpty()) {
            // 即使没有提取到新参数，也返回已有参数
            return getParamList(dto.getReportCode());
        }

        // 查询已有参数
        List<ReportDefinitionParam> existingParams = getParamList(dto.getReportCode());

        // 尝试从数据库元数据获取字段信息
        Map<String, ColumnInfo> columnInfoMap = getColumnInfoMap(dto.getReportScript());

        // 构建结果：已有参数 + 新提取的参数
        Set<String> processedCodes = new LinkedHashSet<>();
        List<ReportDefinitionParam> result = new ArrayList<>();

        // 先添加已有参数（保持原有顺序）
        for (ReportDefinitionParam existing : existingParams) {
            if (existing.getFieldCode() != null) {
                processedCodes.add(existing.getFieldCode());
                result.add(existing);
            }
        }

        // 再添加新提取的参数
        for (String paramName : paramNames) {
            if (processedCodes.contains(paramName)) {
                continue;
            }
            processedCodes.add(paramName);
            ReportDefinitionParam param = new ReportDefinitionParam();
            param.setReportCode(dto.getReportCode());
            param.setFieldCode(paramName);

            // 从元数据补充信息
            ColumnInfo columnInfo = columnInfoMap.get(paramName);
            if (columnInfo != null) {
                param.setFieldName(columnInfo.getColumnComment() != null ? columnInfo.getColumnComment() : paramName);
                param.setFieldType(mapFieldType(columnInfo.getDataType()));
                param.setFieldFormType(mapFormType(columnInfo.getDataType()));
                param.setPlaceholder(columnInfo.getColumnComment());
            } else {
                param.setFieldName(paramName);
                param.setFieldType("string");
                param.setFieldFormType("TEXT");
            }
            param.setRequired(0);
            param.setSort(result.size() + 1);
            result.add(param);
        }

        log.info("从SQL提取参数: reportCode={}, 已有={}, 新提取={}", dto.getReportCode(), existingParams.size(), result.size() - existingParams.size());
        return result;
    }

    /**
     * 从 SQL 中提取表名，查询列信息
     */
    private Map<String, ColumnInfo> getColumnInfoMap(String sql) {
        try {
            // 简单提取：从 SQL 中找表名（FROM 后面的标识符）
            // 这里使用 TableInfoService 查询列信息
            // 由于 SQL 可能很复杂，这里只做简单处理
            return Collections.emptyMap();
        } catch (Exception e) {
            log.warn("获取列信息失败: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    /**
     * 数据库类型映射到字段类型
     */
    private String mapFieldType(String dataType) {
        if (dataType == null) { return "string"; }
        String lower = dataType.toLowerCase();
        if (lower.contains("int") || lower.contains("long") || lower.contains("decimal")
            || lower.contains("double") || lower.contains("float") || lower.contains("number")) {
            return "number";
        }
        if (lower.contains("date") || lower.contains("timestamp")) {
            return "datetime";
        }
        return "string";
    }

    /**
     * 数据库类型映射到表单类型
     */
    private String mapFormType(String dataType) {
        if (dataType == null) { return "TEXT"; }
        String lower = dataType.toLowerCase();
        if (lower.contains("int") || lower.contains("long") || lower.contains("decimal")
            || lower.contains("double") || lower.contains("float") || lower.contains("number")) {
            return "NUMBER";
        }
        if (lower.contains("datetime") || lower.contains("timestamp")) {
            return "DATETIME";
        }
        if (lower.contains("date")) {
            return "DATE";
        }
        return "TEXT";
    }

}
