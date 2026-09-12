package com.wkclz.micro.dbview.service;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.dbview.bean.dto.TableInfo;
import com.wkclz.micro.dbview.bean.req.SchemaDiffReq;
import com.wkclz.micro.dbview.bean.req.SchemaDiffReq.DiffSourceReq;
import com.wkclz.micro.dbview.bean.resp.SchemaDiffResp;
import com.wkclz.micro.dbview.utils.schemadiff.DdlParseUtil;
import com.wkclz.micro.dbview.utils.schemadiff.SchemaDiffUtil;
import com.wkclz.micro.dbview.utils.schemadiff.model.DiffOptions;
import com.wkclz.micro.dbview.utils.schemadiff.model.DiffResult;
import com.wkclz.micro.dbview.utils.schemadiff.model.TableStructure;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DbviewDiffService {

    private static final Logger log = LoggerFactory.getLogger(DbviewDiffService.class);

    @Autowired
    private DbviewMetadataService metadataService;

    public SchemaDiffResp diff(SchemaDiffReq req) {
        String baseDesc = describe(req.getBase());
        String otherDesc = describe(req.getOther());
        log.info("schema diff request, base: {}, other: {}", baseDesc, otherDesc);
        DiffOptions options = req.getOptions() != null ? req.getOptions() : new DiffOptions();
        Map<String, TableStructure> base = resolveSource(req.getBase());
        Map<String, TableStructure> other = resolveSource(req.getOther());

        DiffResult diffResult = SchemaDiffUtil.diff(base, other, options);
        List<String> syncDdls = SchemaDiffUtil.generateSyncDdl(diffResult, options);

        SchemaDiffResp resp = new SchemaDiffResp();
        resp.setBaseDesc(baseDesc);
        resp.setOtherDesc(otherDesc);
        resp.setDiffResult(diffResult);
        resp.setSyncDdls(syncDdls);
        log.info("schema diff completed, tables: {}, items: {}, ddl count: {}",
                diffResult.getTotalTables(), diffResult.getItemCount(), syncDdls.size());
        return resp;
    }

    private Map<String, TableStructure> resolveSource(DiffSourceReq source) {
        if (source == null || source.getType() == null) {
            throw ValidationException.of("对比源不能为空");
        }
        return switch (source.getType()) {
            case DATASOURCE -> resolveDatasource(source);
            case DDL_TEXT -> {
                if (StringUtils.isBlank(source.getDdlText())) {
                    throw ValidationException.of("DDL 文本对比源必须提供 ddlText");
                }
                yield DdlParseUtil.parse(source.getDdlText());
            }
        };
    }

    /** 从数据源上下文取表结构：SHOW CREATE TABLE → 统一解析路径（复用元数据缓存） */
    private Map<String, TableStructure> resolveDatasource(DiffSourceReq source) {
        Long datasourceId = source.getDatasourceId();
        if (datasourceId == null) {
            throw ValidationException.of("数据源对比源必须指定 datasourceId");
        }
        String schemaName = source.getSchemaName();
        if (StringUtils.isBlank(schemaName)) {
            throw ValidationException.of("数据源对比源必须指定 schemaName");
        }

        List<String> tableNames = new ArrayList<>();
        if (StringUtils.isNotBlank(source.getTableName())) {
            String tableName = source.getTableName();
            boolean exists = metadataService.getTables(datasourceId, schemaName).stream()
                    .anyMatch(t -> tableName.equals(t.getTableName()));
            if (!exists) {
                log.error("table not found, datasourceId: {}, schema: {}, table: {}", datasourceId, schemaName, tableName);
                throw ValidationException.of("表不存在: " + schemaName + "." + tableName);
            }
            tableNames.add(tableName);
        } else {
            List<TableInfo> tables = metadataService.getTables(datasourceId, schemaName);
            tables.forEach(t -> tableNames.add(t.getTableName()));
        }
        if (tableNames.isEmpty()) {
            log.error("resolve datasource source empty, datasourceId: {}, schema: {}", datasourceId, schemaName);
            throw ValidationException.of("对比源中没有表: datasourceId=" + datasourceId + ", schema=" + schemaName);
        }

        Map<String, TableStructure> result = new LinkedHashMap<>();
        for (String tableName : tableNames) {
            String ddl = metadataService.getTableDdl(datasourceId, schemaName, tableName);
            if (StringUtils.isBlank(ddl)) {
                log.error("resolve datasource get ddl failed, datasourceId: {}, schema: {}, table: {}",
                        datasourceId, schemaName, tableName);
                throw ValidationException.of("获取建表 DDL 失败: " + schemaName + "." + tableName);
            }
            TableStructure ts = DdlParseUtil.parseTable(ddl);
            result.put(ts.getTableName(), ts);
        }
        log.info("resolve datasource source done, datasourceId: {}, schema: {}, tables: {}",
                datasourceId, schemaName, result.size());
        return result;
    }

    private String describe(DiffSourceReq source) {
        if (source == null || source.getType() == null) {
            return "UNKNOWN";
        }
        return switch (source.getType()) {
            case DATASOURCE -> "DATASOURCE:" + source.getDatasourceId() + "." + source.getSchemaName()
                    + (StringUtils.isNotBlank(source.getTableName()) ? "." + source.getTableName() : "");
            case DDL_TEXT -> "DDL_TEXT(" + (source.getDdlText() == null ? 0 : source.getDdlText().length()) + " chars)";
        };
    }
}
