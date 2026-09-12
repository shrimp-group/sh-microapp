package com.wkclz.micro.dbview.utils.schemadiff;

import com.wkclz.micro.dbview.utils.schemadiff.model.ColumnDef;
import com.wkclz.micro.dbview.utils.schemadiff.model.DdlGranularity;
import com.wkclz.micro.dbview.utils.schemadiff.model.DiffAction;
import com.wkclz.micro.dbview.utils.schemadiff.model.DiffItem;
import com.wkclz.micro.dbview.utils.schemadiff.model.DiffOptions;
import com.wkclz.micro.dbview.utils.schemadiff.model.DiffResult;
import com.wkclz.micro.dbview.utils.schemadiff.model.DiffScope;
import com.wkclz.micro.dbview.utils.schemadiff.model.DiffStatus;
import com.wkclz.micro.dbview.utils.schemadiff.model.IndexDef;
import com.wkclz.micro.dbview.utils.schemadiff.model.TableStructure;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SchemaDiffUtilTest {

    private TableStructure table(String name, ColumnDef... columns) {
        TableStructure ts = new TableStructure();
        ts.setTableName(name);
        ts.getColumns().addAll(List.of(columns));
        ts.setOriginalDdl("CREATE TABLE `" + name + "` (`id` bigint NOT NULL);");
        return ts;
    }

    private ColumnDef column(String name, String type, boolean nullable, String defaultValue, String comment) {
        ColumnDef c = new ColumnDef();
        c.setColumnName(name);
        c.setColumnType(type);
        c.setNullable(nullable);
        c.setDefaultValue(defaultValue);
        c.setColumnComment(comment);
        return c;
    }

    @Test
    void shouldDetectOnlyBaseAndOnlyOtherTables() {
        TableStructure a = table("t_a", column("id", "bigint", false, null, null));
        TableStructure b = table("t_b", column("id", "bigint", false, null, null));
        DiffResult result = SchemaDiffUtil.diff(Map.of("t_a", a), Map.of("t_b", b), new DiffOptions());
        assertEquals(2, result.getTotalTables());
        assertEquals(1, result.getOnlyBaseTables());
        assertEquals(1, result.getOnlyOtherTables());
        assertEquals(2, result.getItemCount());
    }

    @Test
    void shouldDetectIdenticalTablesAsSame() {
        ColumnDef col = column("id", "bigint", false, null, "主键");
        DiffResult result = SchemaDiffUtil.diff(Map.of("t", table("t", col)), Map.of("t", table("t", column("id", "bigint", false, null, "主键"))), new DiffOptions());
        assertEquals(DiffStatus.SAME, result.getTables().get(0).getStatus());
        assertEquals(1, result.getSameTables());
        assertEquals(0, result.getItemCount());
    }

    @Test
    void shouldDetectColumnAddRemoveChange() {
        TableStructure base = table("t",
                column("id", "bigint", false, null, null),
                column("name", "varchar(64)", true, null, "姓名"),
                column("age", "int", true, null, null));
        TableStructure other = table("t",
                column("id", "bigint", false, null, null),
                column("name", "varchar(128)", true, "", "用户名"),
                column("email", "varchar(64)", true, null, null));
        DiffResult result = SchemaDiffUtil.diff(Map.of("t", base), Map.of("t", other), new DiffOptions());
        List<DiffItem> items = result.getTables().get(0).getItems();
        // age 新增、email 移除、name 的类型/默认值/注释变更
        assertEquals(5, items.size());
        assertTrue(items.stream().anyMatch(i -> i.getScope() == DiffScope.COLUMN && i.getAction() == DiffAction.ADDED && i.getObjectName().equals("age")));
        assertTrue(items.stream().anyMatch(i -> i.getScope() == DiffScope.COLUMN && i.getAction() == DiffAction.REMOVED && i.getObjectName().equals("email")));
        assertTrue(items.stream().anyMatch(i -> i.getScope() == DiffScope.COLUMN && "columnType".equals(i.getProperty()) && i.getBaseValue().equals("varchar(64)")));
        assertTrue(items.stream().anyMatch(i -> i.getScope() == DiffScope.COLUMN && "columnComment".equals(i.getProperty())));
        assertTrue(items.stream().anyMatch(i -> i.getScope() == DiffScope.COLUMN && "defaultValue".equals(i.getProperty())));
    }

    @Test
    void shouldNormalizeIntDisplayWidth() {
        // int(11) 与 int 不应产生假差异（MySQL 5.7 vs 8.0）
        TableStructure base = table("t", column("id", "int(11)", false, null, null));
        TableStructure other = table("t", column("id", "int", false, null, null));
        DiffResult result = SchemaDiffUtil.diff(Map.of("t", base), Map.of("t", other), new DiffOptions());
        assertEquals(DiffStatus.SAME, result.getTables().get(0).getStatus());
    }

    private IndexDef index(String name, boolean unique, String... columns) {
        IndexDef idx = new IndexDef();
        idx.setIndexName(name);
        idx.setUnique(unique);
        idx.getColumnNames().addAll(List.of(columns));
        return idx;
    }

    @Test
    void shouldRespectCompareIndexSwitch() {
        TableStructure base = table("t", column("id", "bigint", false, null, null));
        base.getIndexes().add(index("uk_id", true, "id"));
        TableStructure other = table("t", column("id", "bigint", false, null, null));

        DiffOptions opt = new DiffOptions();
        opt.setCompareIndex(false);
        DiffResult result = SchemaDiffUtil.diff(Map.of("t", base), Map.of("t", other), opt);
        assertEquals(DiffStatus.SAME, result.getTables().get(0).getStatus());

        opt.setCompareIndex(true);
        result = SchemaDiffUtil.diff(Map.of("t", base), Map.of("t", other), opt);
        assertEquals(1, result.getItemCount());
        assertEquals(DiffScope.INDEX, result.getTables().get(0).getItems().get(0).getScope());
    }

    @Test
    void shouldDetectIndexChangeAndRemove() {
        TableStructure base = table("t", column("id", "bigint", false, null, null), column("code", "varchar(32)", false, null, null));
        base.getIndexes().add(index("idx_code", false, "code"));
        TableStructure other = table("t", column("id", "bigint", false, null, null), column("code", "varchar(32)", false, null, null));
        other.getIndexes().add(index("idx_code", true, "code", "id"));
        other.getIndexes().add(index("idx_old", false, "code"));

        DiffResult result = SchemaDiffUtil.diff(Map.of("t", base), Map.of("t", other), new DiffOptions());
        List<DiffItem> items = result.getTables().get(0).getItems();
        assertEquals(2, items.size());
        assertTrue(items.stream().anyMatch(i -> i.getScope() == DiffScope.INDEX && i.getAction() == DiffAction.CHANGED && i.getObjectName().equals("idx_code")));
        assertTrue(items.stream().anyMatch(i -> i.getScope() == DiffScope.INDEX && i.getAction() == DiffAction.REMOVED && i.getObjectName().equals("idx_old")));
    }

    @Test
    void shouldRespectSwitchesForTableAndColumnProps() {
        TableStructure base = table("t", column("id", "bigint", false, null, null));
        base.setTableComment("A");
        base.setEngine("InnoDB");
        base.getColumns().get(0).setColumnComment("注释");
        TableStructure other = table("t", column("id", "bigint", false, null, null));
        other.setTableComment("B");
        other.setEngine("MyISAM");

        // 关闭注释与引擎对比 → 仅剩字段注释差异
        DiffOptions opt = new DiffOptions();
        opt.setCompareTableComment(false);
        opt.setCompareEngine(false);
        DiffResult result = SchemaDiffUtil.diff(Map.of("t", base), Map.of("t", other), opt);
        List<DiffItem> items = result.getTables().get(0).getItems();
        assertEquals(1, items.size());
        assertEquals("columnComment", items.get(0).getProperty());
    }

    @Test
    void shouldDetectPositionChange() {
        TableStructure base = table("t", column("id", "bigint", false, null, null), column("name", "varchar(64)", true, null, null));
        TableStructure other = table("t", column("name", "varchar(64)", true, null, null), column("id", "bigint", false, null, null));
        DiffResult result = SchemaDiffUtil.diff(Map.of("t", base), Map.of("t", other), new DiffOptions());
        List<DiffItem> items = result.getTables().get(0).getItems();
        assertEquals(1, items.size());
        assertEquals("position", items.get(0).getProperty());
        assertEquals("id", items.get(0).getObjectName());
    }

    @Test
    void shouldReverseDirectionWhenSwappingSides() {
        TableStructure base = table("t", column("id", "bigint", false, null, null), column("age", "int", true, null, null));
        TableStructure other = table("t", column("id", "bigint", false, null, null));
        DiffResult forward = SchemaDiffUtil.diff(Map.of("t", base), Map.of("t", other), new DiffOptions());
        DiffResult reverse = SchemaDiffUtil.diff(Map.of("t", other), Map.of("t", base), new DiffOptions());
        assertEquals(DiffAction.ADDED, forward.getTables().get(0).getItems().get(0).getAction());
        assertEquals(DiffAction.REMOVED, reverse.getTables().get(0).getItems().get(0).getAction());
    }

    @Test
    void shouldGeneratePerTableAlterDdl() {
        // base: id, name varchar(64), addr(新增), PK(id), KEY idx_addr(addr)
        // other: id, name varchar(128), email(多余), PK(id)
        TableStructure base = table("t", column("id", "bigint", false, null, null),
                column("name", "varchar(64)", false, null, null), column("addr", "varchar(255)", true, null, null));
        base.setOriginalDdl("CREATE TABLE `t` (`id` bigint NOT NULL);");
        base.getIndexes().add(index("PRIMARY", true, "id"));
        base.getIndexes().add(index("idx_addr", false, "addr"));
        TableStructure other = table("t", column("id", "bigint", false, null, null),
                column("name", "varchar(128)", false, null, null), column("email", "varchar(64)", true, null, null));
        other.getIndexes().add(index("PRIMARY", true, "id"));

        DiffResult result = SchemaDiffUtil.diff(Map.of("t", base), Map.of("t", other), new DiffOptions());
        List<String> ddls = SchemaDiffUtil.generateSyncDdl(result, new DiffOptions());
        assertEquals(1, ddls.size());
        String ddl = ddls.get(0);
        assertTrue(ddl.startsWith("ALTER TABLE `t` "));
        assertTrue(ddl.contains("DROP COLUMN `email`"));
        assertTrue(ddl.contains("MODIFY COLUMN `name` varchar(64) NOT NULL AFTER `id`"));
        assertTrue(ddl.contains("ADD COLUMN `addr` varchar(255) AFTER `name`"));
        assertTrue(ddl.contains("ADD INDEX `idx_addr` (`addr`)"));
        // 先 DROP 后 ADD/MODIFY
        assertTrue(ddl.indexOf("DROP COLUMN") < ddl.indexOf("ADD COLUMN"));
    }

    @Test
    void shouldGenerateCreateForOnlyBaseAndSkipDropByDefault() {
        TableStructure base = table("t_new", column("id", "bigint", false, null, null));
        base.setOriginalDdl("CREATE TABLE `t_new` (`id` bigint NOT NULL) ENGINE=InnoDB;");
        TableStructure other = table("t_old", column("id", "bigint", false, null, null));
        DiffResult result = SchemaDiffUtil.diff(Map.of("t_new", base), Map.of("t_old", other), new DiffOptions());
        List<String> ddls = SchemaDiffUtil.generateSyncDdl(result, new DiffOptions());
        assertEquals(1, ddls.size());
        assertTrue(ddls.get(0).startsWith("CREATE TABLE `t_new`"));
        // dropMissingTable 默认 false：不生成 DROP TABLE
        DiffOptions opt = new DiffOptions();
        opt.setDropMissingTable(true);
        ddls = SchemaDiffUtil.generateSyncDdl(result, opt);
        assertEquals(2, ddls.size());
        assertEquals("DROP TABLE `t_old`;", ddls.get(1));
    }

    @Test
    void shouldGeneratePrimaryKeyAndTableOptionClauses() {
        TableStructure base = table("t", column("id", "bigint", false, null, null), column("code", "varchar(32)", false, null, null));
        base.setEngine("InnoDB");
        base.setTableComment("订单表");
        base.getIndexes().add(index("PRIMARY", true, "id", "code"));
        TableStructure other = table("t", column("id", "bigint", false, null, null), column("code", "varchar(32)", false, null, null));
        other.setEngine("MyISAM");
        other.setTableComment("旧注释");
        other.getIndexes().add(index("PRIMARY", true, "id"));

        DiffResult result = SchemaDiffUtil.diff(Map.of("t", base), Map.of("t", other), new DiffOptions());
        List<String> ddls = SchemaDiffUtil.generateSyncDdl(result, new DiffOptions());
        assertEquals(1, ddls.size());
        String ddl = ddls.get(0);
        assertTrue(ddl.contains("DROP PRIMARY KEY"));
        assertTrue(ddl.contains("ADD PRIMARY KEY (`id`, `code`)"));
        assertTrue(ddl.contains("ENGINE=InnoDB"));
        assertTrue(ddl.contains("COMMENT '订单表'"));
    }

    @Test
    void shouldGeneratePerChangeDdl() {
        TableStructure base = table("t", column("id", "bigint", false, null, null), column("age", "int", true, null, null));
        TableStructure other = table("t", column("id", "bigint", false, null, null));
        DiffOptions opt = new DiffOptions();
        opt.setDdlGranularity(DdlGranularity.PER_CHANGE);
        DiffResult result = SchemaDiffUtil.diff(Map.of("t", base), Map.of("t", other), opt);
        List<String> ddls = SchemaDiffUtil.generateSyncDdl(result, opt);
        assertEquals(1, ddls.size());
        assertEquals("ALTER TABLE `t` ADD COLUMN `age` int AFTER `id`;", ddls.get(0));
    }

    @Test
    void shouldEscapeBackslashInComment() {
        // 语义值 C:\data 在 DDL 文本中必须重新转义为 C:\\data，否则 round-trip 语义损坏
        TableStructure base = table("t", column("path", "varchar(128)", true, null, "C:\\data"));
        TableStructure other = table("t", column("path", "varchar(128)", true, null, null));
        DiffResult result = SchemaDiffUtil.diff(Map.of("t", base), Map.of("t", other), new DiffOptions());
        List<String> ddls = SchemaDiffUtil.generateSyncDdl(result, new DiffOptions());
        assertEquals(1, ddls.size());
        assertTrue(ddls.get(0).contains("COMMENT 'C:\\\\data'"));
        assertFalse(ddls.get(0).contains("COMMENT 'C:\\data'"));
    }

    @Test
    void shouldIgnoreBtreeIndexTypeInSignature() {
        // 5.7 惯例输出 USING BTREE，8.0 常省略，BTREE 为默认类型不参与对比
        TableStructure base = table("t", column("code", "varchar(32)", false, null, null));
        IndexDef bi = index("idx_code", false, "code");
        bi.setIndexType("BTREE");
        base.getIndexes().add(bi);
        TableStructure other = table("t", column("code", "varchar(32)", false, null, null));
        other.getIndexes().add(index("idx_code", false, "code"));

        DiffResult result = SchemaDiffUtil.diff(Map.of("t", base), Map.of("t", other), new DiffOptions());
        assertEquals(DiffStatus.SAME, result.getTables().get(0).getStatus());
    }

    @Test
    void shouldGenerateFulltextIndexClause() {
        // FULLTEXT 不允许 USING 后缀，需生成 ADD FULLTEXT INDEX 前缀
        TableStructure base = table("t", column("content", "text", true, null, null));
        IndexDef ft = index("ft_content", false, "content");
        ft.setIndexType("FULLTEXT");
        base.getIndexes().add(ft);
        TableStructure other = table("t", column("content", "text", true, null, null));

        DiffResult result = SchemaDiffUtil.diff(Map.of("t", base), Map.of("t", other), new DiffOptions());
        List<String> ddls = SchemaDiffUtil.generateSyncDdl(result, new DiffOptions());
        assertEquals(1, ddls.size());
        assertTrue(ddls.get(0).contains("ADD FULLTEXT INDEX `ft_content`"));
        assertFalse(ddls.get(0).contains("USING"));
    }

    @Test
    void shouldKeepDescSuffixOutsideBackticks() {
        // DESC 后缀不能被包进反引号：`code` DESC
        TableStructure base = table("t", column("code", "varchar(32)", false, null, null));
        base.getIndexes().add(index("idx_code", false, "code DESC"));
        TableStructure other = table("t", column("code", "varchar(32)", false, null, null));

        DiffResult result = SchemaDiffUtil.diff(Map.of("t", base), Map.of("t", other), new DiffOptions());
        List<String> ddls = SchemaDiffUtil.generateSyncDdl(result, new DiffOptions());
        assertEquals(1, ddls.size());
        assertTrue(ddls.get(0).contains("`code` DESC"));
        assertFalse(ddls.get(0).contains("`code DESC`"));
    }
}
