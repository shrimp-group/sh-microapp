package com.wkclz.micro.dbview.utils.schemadiff;

import com.wkclz.micro.dbview.utils.schemadiff.model.ColumnDef;
import com.wkclz.micro.dbview.utils.schemadiff.model.IndexDef;
import com.wkclz.micro.dbview.utils.schemadiff.model.TableStructure;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DdlParseUtilTest {

    private static final String SIMPLE_DDL = """
            CREATE TABLE `t_user` (
              `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
              `user_name` varchar(64) NOT NULL COMMENT '用户名',
              PRIMARY KEY (`id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
            """;

    @Test
    void shouldParseBasicTable() {
        TableStructure ts = DdlParseUtil.parseTable(SIMPLE_DDL);
        assertEquals("t_user", ts.getTableName());
        assertEquals("用户表", ts.getTableComment());
        assertEquals("InnoDB", ts.getEngine());
        assertEquals("utf8mb4", ts.getCharset());
        assertEquals(2, ts.getColumns().size());
        assertEquals(1, ts.getIndexes().size());
    }

    @Test
    void shouldParseColumnBasics() {
        TableStructure ts = DdlParseUtil.parseTable(SIMPLE_DDL);
        ColumnDef id = ts.getColumn("id");
        assertEquals("bigint", id.getColumnType());
        assertEquals(Boolean.FALSE, id.getNullable());
        assertTrue(id.isAutoIncrement());
        assertEquals("主键", id.getColumnComment());
        assertEquals(1, id.getOrdinalPosition());

        ColumnDef name = ts.getColumn("user_name");
        assertEquals("varchar(64)", name.getColumnType());
        assertEquals(2, name.getOrdinalPosition());
    }

    @Test
    void shouldParsePrimaryKey() {
        TableStructure ts = DdlParseUtil.parseTable(SIMPLE_DDL);
        IndexDef pk = ts.getIndex("PRIMARY");
        assertTrue(pk.isPrimary());
        assertTrue(pk.isUnique());
        assertEquals("id", pk.getColumnNames().get(0));
    }

    @Test
    void shouldParseMultiTablesAndIgnoreOthers() {
        String ddl = """
                INSERT INTO t_log VALUES (1);
                -- a comment line
                CREATE TABLE `t_a` (`id` int NOT NULL);
                CREATE TABLE `t_b` (`id` int NOT NULL);
                DROP TABLE t_c;
                """;
        Map<String, TableStructure> result = DdlParseUtil.parse(ddl);
        assertEquals(2, result.size());
        assertTrue(result.containsKey("t_a"));
        assertTrue(result.containsKey("t_b"));
    }

    @Test
    void shouldThrowOnInvalidDdl() {
        assertThrows(Exception.class, () -> DdlParseUtil.parse(null));
        assertThrows(Exception.class, () -> DdlParseUtil.parse("   "));
        assertThrows(Exception.class, () -> DdlParseUtil.parseTable("INSERT INTO t VALUES (1)"));
    }

    @Test
    void shouldParseColumnAdvancedAttributes() {
        String ddl = """
                CREATE TABLE `t_order` (
                  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
                  `amount` decimal(10,2) NOT NULL DEFAULT '0.00',
                  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态',
                  `remark` varchar(255) DEFAULT NULL,
                  `json_ext` json DEFAULT NULL COMMENT '扩展{"a":1}',
                  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                  `name_cn` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
                  `code` varchar(32) DEFAULT 'A''B',
                  PRIMARY KEY (`id`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
                """;
        TableStructure ts = DdlParseUtil.parseTable(ddl);
        assertEquals(9, ts.getColumns().size());

        assertEquals("bigint unsigned", ts.getColumn("id").getColumnType());
        assertEquals("decimal(10,2)", ts.getColumn("amount").getColumnType());
        assertEquals("0.00", ts.getColumn("amount").getDefaultValue());
        assertEquals("0", ts.getColumn("status").getDefaultValue());
        assertNull(ts.getColumn("remark").getDefaultValue());
        assertEquals("扩展{\"a\":1}", ts.getColumn("json_ext").getColumnComment());
        assertEquals("CURRENT_TIMESTAMP", ts.getColumn("created_at").getDefaultValue());
        assertEquals("CURRENT_TIMESTAMP", ts.getColumn("updated_at").getExtra());
        assertEquals("utf8mb4", ts.getColumn("name_cn").getColumnCharset());
        assertEquals("utf8mb4_bin", ts.getColumn("name_cn").getColumnCollate());
        assertEquals("A'B", ts.getColumn("code").getDefaultValue());
        assertEquals("utf8mb4_0900_ai_ci", ts.getCollate());
    }

    @Test
    void shouldParseAdvancedIndexesAndTableOptions() {
        String ddl = """
                CREATE TABLE `t_sku` (
                  `id` bigint NOT NULL AUTO_INCREMENT,
                  `sku_code` varchar(64) NOT NULL,
                  `spu_id` bigint NOT NULL,
                  `content` text COMMENT '全文',
                  `geo` point,
                  PRIMARY KEY (`id`,`sku_code`),
                  UNIQUE KEY `uk_spu` (`spu_id`),
                  KEY `idx_code` (`sku_code`(16)) USING BTREE,
                  FULLTEXT KEY `ft_content` (`content`),
                  SPATIAL KEY `sp_geo` (`geo`)
                ) ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='SKU表';
                """;
        TableStructure ts = DdlParseUtil.parseTable(ddl);

        IndexDef pk = ts.getIndex("PRIMARY");
        assertEquals(2, pk.getColumnNames().size());
        assertEquals("id", pk.getColumnNames().get(0));
        assertEquals("sku_code", pk.getColumnNames().get(1));

        assertEquals(Boolean.TRUE, ts.getIndex("uk_spu").isUnique());
        assertEquals("sku_code(16)", ts.getIndex("idx_code").getColumnNames().get(0));
        assertEquals("FULLTEXT", ts.getIndex("ft_content").getIndexType());
        assertEquals("SPATIAL", ts.getIndex("sp_geo").getIndexType());
        assertEquals(5, ts.getIndexes().size());

        assertEquals(1000L, ts.getAutoIncrement());
        assertEquals("DYNAMIC", ts.getRowFormat());
    }

    @Test
    void shouldParseIndexCommentAndCaseInsensitiveName() {
        String ddl = """
                CREATE TABLE t_idx (
                  id int NOT NULL,
                  name varchar(32) DEFAULT NULL,
                  KEY `idx_name` (`name`) COMMENT '姓名''s index'
                ) ENGINE=InnoDB;
                """;
        TableStructure ts = DdlParseUtil.parseTable(ddl);
        assertEquals("姓名's index", ts.getIndex("idx_name").getComment());
        assertEquals("t_idx", ts.getTableName());
    }

    @Test
    void shouldEscapeBackslashAndSpecialCharsInStrings() {
        String ddl = """
                /* header comment */
                CREATE TABLE IF NOT EXISTS `mydb`.`t_esc` (
                  `code` varchar(32) DEFAULT 'a\\'b',
                  `txt` varchar(64) DEFAULT 'a,b(c);d'
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                # hash comment
                CREATE TABLE `t_next` (`id` int NOT NULL);
                """;
        Map<String, TableStructure> result = DdlParseUtil.parse(ddl);
        assertEquals(2, result.size());
        TableStructure ts = result.get("t_esc");
        assertEquals("t_esc", ts.getTableName());
        assertEquals("a'b", ts.getColumn("code").getDefaultValue());
        assertEquals("a,b(c);d", ts.getColumn("txt").getDefaultValue());
        assertTrue(result.containsKey("t_next"));
    }

    @Test
    void shouldParseMysqldumpStyleTableOptions() {
        String ddl = """
                CREATE TABLE `t_dump` (
                  `id` int NOT NULL
                ) ENGINE=InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci;
                """;
        TableStructure ts = DdlParseUtil.parseTable(ddl);
        assertEquals("utf8mb4", ts.getCharset());
        assertEquals("utf8mb4_general_ci", ts.getCollate());
    }
}
