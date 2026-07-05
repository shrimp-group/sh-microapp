package com.wkclz.micro.dbview.rest;

import com.wkclz.core.base.R;
import com.wkclz.micro.dbview.bean.dto.ColumnInfo;
import com.wkclz.micro.dbview.bean.dto.IndexInfo;
import com.wkclz.micro.dbview.bean.dto.TableDetail;
import com.wkclz.micro.dbview.bean.dto.TableInfo;
import com.wkclz.micro.dbview.bean.req.MetadataDetailReq;
import com.wkclz.micro.dbview.bean.req.MetadataSchemasReq;
import com.wkclz.micro.dbview.bean.req.MetadataTablesReq;
import com.wkclz.micro.dbview.service.DbviewMetadataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "元数据管理", description = "数据库元数据查询接口")
@Validated
@RestController
@RequestMapping(Route.PREFIX)
public class MetadataRest {

    @Autowired
    private DbviewMetadataService metadataService;

    @Operation(summary = "1. 元数据-数据库列表")
    @GetMapping(Route.METADATA_SCHEMAS)
    public R<List<String>> schemas(@Valid MetadataSchemasReq req) {
        List<String> schemas = metadataService.getSchemas(req.getDatasourceId());
        return R.ok(schemas);
    }

    @Operation(summary = "2. 元数据-表列表")
    @GetMapping(Route.METADATA_TABLES)
    public R<List<TableInfo>> tables(@Valid MetadataTablesReq req) {
        List<TableInfo> tables = metadataService.getTables(req.getDatasourceId(), req.getSchemaName());
        return R.ok(tables);
    }

    @Operation(summary = "3. 元数据-表详情")
    @GetMapping(Route.METADATA_TABLE_DETAIL)
    public R<TableDetail> tableDetail(@Valid MetadataDetailReq req) {
        TableDetail detail = metadataService.getTableDetail(req.getDatasourceId(), req.getSchemaName(), req.getTableName());
        return R.ok(detail);
    }

    @Operation(summary = "4. 元数据-字段列表")
    @GetMapping(Route.METADATA_COLUMNS)
    public R<List<ColumnInfo>> columns(@Valid MetadataDetailReq req) {
        List<ColumnInfo> columns = metadataService.getColumns(req.getDatasourceId(), req.getSchemaName(), req.getTableName());
        return R.ok(columns);
    }

    @Operation(summary = "5. 元数据-索引列表")
    @GetMapping(Route.METADATA_INDEXES)
    public R<List<IndexInfo>> indexes(@Valid MetadataDetailReq req) {
        List<IndexInfo> indexes = metadataService.getIndexes(req.getDatasourceId(), req.getSchemaName(), req.getTableName());
        return R.ok(indexes);
    }

    @Operation(summary = "6. 元数据-建表DDL")
    @GetMapping(Route.METADATA_TABLE_DDL)
    public R<String> tableDdl(@Valid MetadataDetailReq req) {
        String ddl = metadataService.getTableDdl(req.getDatasourceId(), req.getSchemaName(), req.getTableName());
        return R.ok(ddl);
    }

    @Operation(summary = "7. 元数据-刷新缓存")
    @PostMapping(Route.METADATA_REFRESH_CACHE)
    public R<Void> refreshCache(@RequestParam Long datasourceId) {
        metadataService.refreshCache(datasourceId);
        return R.ok();
    }
}
