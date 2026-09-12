package com.wkclz.micro.dbview.service;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.dbview.bean.dto.TableInfo;
import com.wkclz.micro.dbview.bean.req.SchemaDiffReq;
import com.wkclz.micro.dbview.bean.req.SchemaDiffReq.DiffSourceReq;
import com.wkclz.micro.dbview.bean.req.SchemaDiffReq.DiffSourceReq.SourceType;
import com.wkclz.micro.dbview.bean.resp.SchemaDiffResp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DbviewDiffServiceTest {

    private static final String BASE_DDL = """
            CREATE TABLE `t_user` (
              `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
              `user_name` varchar(64) NOT NULL COMMENT '用户名',
              PRIMARY KEY (`id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
            """;

    private static final String OTHER_DDL = """
            CREATE TABLE `t_user` (
              `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
              `user_name` varchar(64) NOT NULL COMMENT '用户名',
              `email` varchar(128) DEFAULT NULL COMMENT '邮箱',
              PRIMARY KEY (`id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
            """;

    @Mock
    private DbviewMetadataService metadataService;

    @InjectMocks
    private DbviewDiffService diffService;

    // ===== resolveSource 校验分支 =====

    @Test
    void shouldRejectNullBaseSource() {
        SchemaDiffReq req = req(null, ddlTextSource(BASE_DDL));
        ValidationException ex = assertThrows(ValidationException.class, () -> diffService.diff(req));
        assertEquals("对比源不能为空", ex.getMessage());
    }

    @Test
    void shouldRejectDdlTextWithoutDdlText() {
        SchemaDiffReq req = req(ddlTextSource(null), ddlTextSource(BASE_DDL));
        ValidationException ex = assertThrows(ValidationException.class, () -> diffService.diff(req));
        assertEquals("DDL 文本对比源必须提供 ddlText", ex.getMessage());
    }

    @Test
    void shouldRejectDatasourceWithoutDatasourceId() {
        SchemaDiffReq req = req(datasourceSource(null, "db", null), ddlTextSource(BASE_DDL));
        ValidationException ex = assertThrows(ValidationException.class, () -> diffService.diff(req));
        assertEquals("数据源对比源必须指定 datasourceId", ex.getMessage());
    }

    @Test
    void shouldRejectDatasourceWithoutSchemaName() {
        SchemaDiffReq req = req(datasourceSource(1L, " ", null), ddlTextSource(BASE_DDL));
        ValidationException ex = assertThrows(ValidationException.class, () -> diffService.diff(req));
        assertEquals("数据源对比源必须指定 schemaName", ex.getMessage());
    }

    // ===== DDL_TEXT 成功路径 =====

    @Test
    void shouldDiffByDdlText() {
        SchemaDiffResp resp = diffService.diff(req(ddlTextSource(BASE_DDL), ddlTextSource(OTHER_DDL)));
        assertNotNull(resp);
        assertEquals("DDL_TEXT(" + BASE_DDL.length() + " chars)", resp.getBaseDesc());
        assertEquals("DDL_TEXT(" + OTHER_DDL.length() + " chars)", resp.getOtherDesc());
        assertNotNull(resp.getDiffResult());
        assertNotNull(resp.getSyncDdls());
        assertEquals(1, resp.getDiffResult().getTotalTables());
        assertTrue(resp.getDiffResult().getItemCount() > 0);
    }

    // ===== DATASOURCE 成功路径 =====

    @Test
    void shouldDiffByDatasourceWholeSchema() {
        TableInfo table = new TableInfo();
        table.setTableName("t_user");
        when(metadataService.getTables(1L, "db")).thenReturn(List.of(table));
        when(metadataService.getTableDdl(1L, "db", "t_user")).thenReturn(BASE_DDL);
        when(metadataService.getTables(2L, "db2")).thenReturn(List.of(table));
        when(metadataService.getTableDdl(2L, "db2", "t_user")).thenReturn(OTHER_DDL);

        SchemaDiffResp resp = diffService.diff(req(datasourceSource(1L, "db", null), datasourceSource(2L, "db2", null)));
        assertEquals("DATASOURCE:1.db", resp.getBaseDesc());
        assertEquals("DATASOURCE:2.db2", resp.getOtherDesc());
        assertNotNull(resp.getDiffResult());
        assertNotNull(resp.getSyncDdls());
        assertEquals(1, resp.getDiffResult().getTotalTables());
    }

    @Test
    void shouldDiffByDatasourceSingleTable() {
        TableInfo table = new TableInfo();
        table.setTableName("t_user");
        when(metadataService.getTables(1L, "db")).thenReturn(List.of(table));
        when(metadataService.getTableDdl(1L, "db", "t_user")).thenReturn(BASE_DDL);
        when(metadataService.getTables(2L, "db2")).thenReturn(List.of(table));
        when(metadataService.getTableDdl(2L, "db2", "t_user")).thenReturn(OTHER_DDL);

        SchemaDiffResp resp = diffService.diff(req(datasourceSource(1L, "db", "t_user"), datasourceSource(2L, "db2", "t_user")));
        assertEquals("DATASOURCE:1.db.t_user", resp.getBaseDesc());
        assertEquals("DATASOURCE:2.db2.t_user", resp.getOtherDesc());
        assertNotNull(resp.getDiffResult());
        assertNotNull(resp.getSyncDdls());
        assertEquals(1, resp.getDiffResult().getTotalTables());
    }

    // ===== DATASOURCE 异常路径 =====

    @Test
    void shouldRejectDatasourceEmptySchema() {
        when(metadataService.getTables(1L, "db")).thenReturn(List.of());
        SchemaDiffReq req = req(datasourceSource(1L, "db", null), ddlTextSource(BASE_DDL));
        ValidationException ex = assertThrows(ValidationException.class, () -> diffService.diff(req));
        assertEquals("对比源中没有表: datasourceId=1, schema=db", ex.getMessage());
    }

    @Test
    void shouldRejectDatasourceTableNotFound() {
        TableInfo table = new TableInfo();
        table.setTableName("t_user");
        when(metadataService.getTables(1L, "db")).thenReturn(List.of(table));
        SchemaDiffReq req = req(datasourceSource(1L, "db", "t_missing"), ddlTextSource(BASE_DDL));
        ValidationException ex = assertThrows(ValidationException.class, () -> diffService.diff(req));
        assertEquals("表不存在: db.t_missing", ex.getMessage());
    }

    private DiffSourceReq datasourceSource(Long datasourceId, String schemaName, String tableName) {
        DiffSourceReq source = new DiffSourceReq();
        source.setType(SourceType.DATASOURCE);
        source.setDatasourceId(datasourceId);
        source.setSchemaName(schemaName);
        source.setTableName(tableName);
        return source;
    }

    private DiffSourceReq ddlTextSource(String ddlText) {
        DiffSourceReq source = new DiffSourceReq();
        source.setType(SourceType.DDL_TEXT);
        source.setDdlText(ddlText);
        return source;
    }

    private SchemaDiffReq req(DiffSourceReq base, DiffSourceReq other) {
        SchemaDiffReq req = new SchemaDiffReq();
        req.setBase(base);
        req.setOther(other);
        return req;
    }
}
