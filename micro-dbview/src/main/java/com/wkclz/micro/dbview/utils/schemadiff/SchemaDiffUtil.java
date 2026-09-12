package com.wkclz.micro.dbview.utils.schemadiff;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.dbview.utils.schemadiff.model.ColumnDef;
import com.wkclz.micro.dbview.utils.schemadiff.model.DdlGranularity;
import com.wkclz.micro.dbview.utils.schemadiff.model.DiffAction;
import com.wkclz.micro.dbview.utils.schemadiff.model.DiffItem;
import com.wkclz.micro.dbview.utils.schemadiff.model.DiffOptions;
import com.wkclz.micro.dbview.utils.schemadiff.model.DiffResult;
import com.wkclz.micro.dbview.utils.schemadiff.model.DiffScope;
import com.wkclz.micro.dbview.utils.schemadiff.model.DiffStatus;
import com.wkclz.micro.dbview.utils.schemadiff.model.IndexDef;
import com.wkclz.micro.dbview.utils.schemadiff.model.TableDiff;
import com.wkclz.micro.dbview.utils.schemadiff.model.TableStructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 表结构对比器（纯静态，零依赖，可整包移植）。
 * base=基准侧，other=对比侧；方向反转 = 调换 diff 的前两个参数。
 */
public final class SchemaDiffUtil {

    private static final Logger log = LoggerFactory.getLogger(SchemaDiffUtil.class);

    private static final Pattern INT_DISPLAY_WIDTH = Pattern.compile("^(?i)(tinyint|smallint|mediumint|bigint|int)\\(\\d+\\)(.*)$");

    private static final Pattern INDEX_COLUMN_ORDER = Pattern.compile("(?i)^(.+)\\s+(ASC|DESC)$");

    private SchemaDiffUtil() {
    }

    /** 对比两侧表结构集合，生成差异清单 */
    public static DiffResult diff(Map<String, TableStructure> base, Map<String, TableStructure> other, DiffOptions options) {
        log.info("schema diff start, base tables: {}, other tables: {}", base.size(), other.size());
        DiffOptions opt = options != null ? options : new DiffOptions();
        DiffResult result = new DiffResult();
        List<TableDiff> tableDiffs = new ArrayList<>();
        for (Map.Entry<String, TableStructure> entry : base.entrySet()) {
            TableStructure o = other.get(entry.getKey());
            tableDiffs.add(o == null ? TableDiff.onlyBase(entry.getValue()) : compareTable(entry.getValue(), o, opt));
        }
        for (Map.Entry<String, TableStructure> entry : other.entrySet()) {
            if (!base.containsKey(entry.getKey())) {
                tableDiffs.add(TableDiff.onlyOther(entry.getValue()));
            }
        }
        result.setTables(tableDiffs);
        fillSummary(result);
        log.info("schema diff done, total: {}, changed: {}, onlyBase: {}, onlyOther: {}",
                result.getTotalTables(), result.getChangedTables(), result.getOnlyBaseTables(), result.getOnlyOtherTables());
        return result;
    }

    private static void fillSummary(DiffResult result) {
        int same = 0;
        int changed = 0;
        int onlyBase = 0;
        int onlyOther = 0;
        int items = 0;
        for (TableDiff td : result.getTables()) {
            items += td.getItems().size();
            switch (td.getStatus()) {
                case SAME -> same++;
                case CHANGED -> changed++;
                case ONLY_BASE -> onlyBase++;
                case ONLY_OTHER -> onlyOther++;
            }
        }
        result.setTotalTables(result.getTables().size());
        result.setSameTables(same);
        result.setChangedTables(changed);
        result.setOnlyBaseTables(onlyBase);
        result.setOnlyOtherTables(onlyOther);
        result.setItemCount(items);
    }

    private static TableDiff compareTable(TableStructure b, TableStructure o, DiffOptions opt) {
        TableDiff td = new TableDiff();
        td.setTableName(b.getTableName());
        td.setBaseStructure(b);
        td.setOtherStructure(o);
        List<DiffItem> items = new ArrayList<>();

        if (opt.isCompareTableComment()) {
            compareProp(items, DiffScope.TABLE, b.getTableName(), "tableComment", b.getTableComment(), o.getTableComment());
        }
        if (opt.isCompareEngine()) {
            compareProp(items, DiffScope.TABLE, b.getTableName(), "engine", b.getEngine(), o.getEngine());
        }
        if (opt.isCompareAutoIncrement()) {
            compareProp(items, DiffScope.TABLE, b.getTableName(), "autoIncrement", String.valueOf(b.getAutoIncrement()), String.valueOf(o.getAutoIncrement()));
        }
        if (opt.isCompareCharset()) {
            compareProp(items, DiffScope.TABLE, b.getTableName(), "charset", b.getCharset(), o.getCharset());
        }
        if (opt.isCompareCollate()) {
            compareProp(items, DiffScope.TABLE, b.getTableName(), "collate", b.getCollate(), o.getCollate());
        }
        if (opt.isCompareRowFormat()) {
            compareProp(items, DiffScope.TABLE, b.getTableName(), "rowFormat", b.getRowFormat(), o.getRowFormat());
        }

        Map<String, ColumnDef> otherCols = byName(o.getColumns());
        for (ColumnDef bc : b.getColumns()) {
            ColumnDef oc = otherCols.remove(bc.getColumnName());
            if (oc == null) {
                items.add(DiffItem.of(DiffScope.COLUMN, DiffAction.ADDED, bc.getColumnName()));
            } else {
                compareColumn(bc, oc, opt, items);
            }
        }
        for (ColumnDef oc : otherCols.values()) {
            items.add(DiffItem.of(DiffScope.COLUMN, DiffAction.REMOVED, oc.getColumnName()));
        }
        if (opt.isComparePosition()) {
            comparePositions(b, o, items);
        }
        if (opt.isCompareIndex()) {
            compareIndexes(b, o, items);
        }

        td.setItems(items);
        td.setStatus(items.isEmpty() ? DiffStatus.SAME : DiffStatus.CHANGED);
        return td;
    }

    private static void compareColumn(ColumnDef b, ColumnDef o, DiffOptions opt, List<DiffItem> items) {
        String name = b.getColumnName();
        if (opt.isCompareColumnType()) {
            compareProp(items, DiffScope.COLUMN, name, "columnType", normalizeColumnType(b.getColumnType()), normalizeColumnType(o.getColumnType()));
        }
        if (opt.isCompareNullable()) {
            compareProp(items, DiffScope.COLUMN, name, "nullable", String.valueOf(b.getNullable()), String.valueOf(o.getNullable()));
        }
        if (opt.isCompareDefaultValue()) {
            compareProp(items, DiffScope.COLUMN, name, "defaultValue", b.getDefaultValue(), o.getDefaultValue());
        }
        if (opt.isCompareColumnComment()) {
            compareProp(items, DiffScope.COLUMN, name, "columnComment", b.getColumnComment(), o.getColumnComment());
        }
        if (opt.isCompareColumnAutoIncrement()) {
            compareProp(items, DiffScope.COLUMN, name, "autoIncrement", String.valueOf(b.isAutoIncrement()), String.valueOf(o.isAutoIncrement()));
        }
        if (opt.isCompareOnUpdate()) {
            compareProp(items, DiffScope.COLUMN, name, "onUpdate", b.getExtra(), o.getExtra());
        }
        if (opt.isCompareColumnCharset()) {
            compareProp(items, DiffScope.COLUMN, name, "columnCharset", b.getColumnCharset(), o.getColumnCharset());
            compareProp(items, DiffScope.COLUMN, name, "columnCollate", b.getColumnCollate(), o.getColumnCollate());
        }
    }

    /** 相对顺序对比：剔除单侧独有列后比较公共列顺序，仅报第一条位置差异 */
    private static void comparePositions(TableStructure b, TableStructure o, List<DiffItem> items) {
        List<String> baseNames = b.getColumns().stream().map(ColumnDef::getColumnName).toList();
        List<String> otherNames = o.getColumns().stream().map(ColumnDef::getColumnName).toList();
        List<String> commonBase = baseNames.stream().filter(otherNames::contains).toList();
        List<String> commonOther = otherNames.stream().filter(baseNames::contains).toList();
        for (int i = 0; i < commonBase.size(); i++) {
            if (!commonBase.get(i).equals(commonOther.get(i))) {
                items.add(DiffItem.changed(DiffScope.COLUMN, commonBase.get(i), "position",
                        "position=" + (i + 1), "position=" + (commonOther.indexOf(commonBase.get(i)) + 1)));
                break;
            }
        }
    }

    private static void compareIndexes(TableStructure b, TableStructure o, List<DiffItem> items) {
        Map<String, IndexDef> otherIdx = byIndexName(o.getIndexes());
        for (IndexDef bi : b.getIndexes()) {
            IndexDef oi = otherIdx.remove(bi.getIndexName());
            if (oi == null) {
                items.add(DiffItem.of(DiffScope.INDEX, DiffAction.ADDED, bi.getIndexName()));
            } else if (!indexSignature(bi).equals(indexSignature(oi))) {
                items.add(DiffItem.changed(DiffScope.INDEX, bi.getIndexName(), "definition", indexSignature(bi), indexSignature(oi)));
            }
        }
        for (IndexDef oi : otherIdx.values()) {
            items.add(DiffItem.of(DiffScope.INDEX, DiffAction.REMOVED, oi.getIndexName()));
        }
    }

    private static String indexSignature(IndexDef i) {
        // BTREE 为 MySQL 默认索引类型，5.7 惯例输出、8.0 常省略，不参与对比
        String sig = (i.isUnique() ? "UNIQUE " : "")
                + (i.getIndexType() != null && !"BTREE".equals(i.getIndexType()) ? i.getIndexType() + " " : "")
                + String.join(",", i.getColumnNames());
        return i.getComment() != null ? sig + " COMMENT '" + i.getComment() + "'" : sig;
    }

    /** 归一化字段类型：去掉整数显示宽度（int(11) → int），避免 MySQL 5.7/8.0 假差异 */
    static String normalizeColumnType(String type) {
        if (type == null) {
            return null;
        }
        Matcher m = INT_DISPLAY_WIDTH.matcher(type.trim());
        if (m.matches()) {
            return m.group(1).toLowerCase() + m.group(2);
        }
        return type.trim();
    }

    private static void compareProp(List<DiffItem> items, DiffScope scope, String objectName, String property, String baseValue, String otherValue) {
        if (!Objects.equals(baseValue, otherValue)) {
            items.add(DiffItem.changed(scope, objectName, property, baseValue, otherValue));
        }
    }

    private static Map<String, ColumnDef> byName(List<ColumnDef> columns) {
        Map<String, ColumnDef> map = new LinkedHashMap<>();
        columns.forEach(c -> map.put(c.getColumnName(), c));
        return map;
    }

    private static Map<String, IndexDef> byIndexName(List<IndexDef> indexes) {
        Map<String, IndexDef> map = new LinkedHashMap<>();
        indexes.forEach(i -> map.put(i.getIndexName(), i));
        return map;
    }

    /** 生成将 other 统一到 base 的 DDL 语句列表 */
    public static List<String> generateSyncDdl(DiffResult result, DiffOptions options) {
        log.info("generate sync ddl start, total: {}, changed: {}, onlyBase: {}, onlyOther: {}",
                result.getTotalTables(), result.getChangedTables(), result.getOnlyBaseTables(), result.getOnlyOtherTables());
        DiffOptions opt = options != null ? options : new DiffOptions();
        List<String> ddls = new ArrayList<>();
        for (TableDiff td : result.getTables()) {
            switch (td.getStatus()) {
                case ONLY_BASE -> {
                    String originalDdl = td.getBaseStructure().getOriginalDdl();
                    if (originalDdl == null) {
                        throw ValidationException.of("originalDdl 为空, table: " + td.getTableName());
                    }
                    String ddl = originalDdl.trim();
                    ddls.add(ddl.endsWith(";") ? ddl : ddl + ";");
                }
                case ONLY_OTHER -> {
                    if (opt.isDropMissingTable()) {
                        ddls.add("DROP TABLE `" + td.getTableName() + "`;");
                    }
                }
                case CHANGED -> ddls.addAll(alterDdl(td, opt));
                default -> log.debug("skip ddl for table: {}, status: {}", td.getTableName(), td.getStatus());
            }
        }
        log.info("generate sync ddl done, count: {}", ddls.size());
        return ddls;
    }

    /** 单表 ALTER：先 DROP（列/索引/主键）后 ADD/MODIFY；PER_TABLE 聚合一条，PER_CHANGE 每子句一条 */
    private static List<String> alterDdl(TableDiff td, DiffOptions opt) {
        List<String> dropClauses = new ArrayList<>();
        List<String> tableClauses = new ArrayList<>();
        List<String> addClauses = new ArrayList<>();
        TableStructure base = td.getBaseStructure();
        Set<String> modifiedCols = new HashSet<>();
        boolean positionChanged = td.getItems().stream()
                .anyMatch(i -> i.getScope() == DiffScope.COLUMN && "position".equals(i.getProperty()));

        for (DiffItem item : td.getItems()) {
            switch (item.getScope()) {
                case TABLE -> tableClauses.add(tableOptionClause(item));
                case COLUMN -> {
                    switch (item.getAction()) {
                        case ADDED -> {
                            ColumnDef col = base.getColumn(item.getObjectName());
                            addClauses.add("ADD COLUMN " + columnBody(col, afterTarget(base, col, td)));
                            modifiedCols.add(col.getColumnName());
                        }
                        case REMOVED -> dropClauses.add("DROP COLUMN `" + item.getObjectName() + "`");
                        case CHANGED -> {
                            if (!"position".equals(item.getProperty()) && !modifiedCols.contains(item.getObjectName())) {
                                ColumnDef col = base.getColumn(item.getObjectName());
                                addClauses.add("MODIFY COLUMN " + columnBody(col, afterTarget(base, col, td)));
                                modifiedCols.add(col.getColumnName());
                            }
                        }
                    }
                }
                case INDEX -> {
                    switch (item.getAction()) {
                        case ADDED -> addClauses.add(addIndexClause(base.getIndex(item.getObjectName())));
                        case REMOVED -> dropClauses.add(dropIndexClause(item.getObjectName()));
                        case CHANGED -> {
                            dropClauses.add(dropIndexClause(item.getObjectName()));
                            addClauses.add(addIndexClause(base.getIndex(item.getObjectName())));
                        }
                    }
                }
            }
        }
        // 位置重排：按 base 顺序补齐未被 MODIFY 的公共字段
        if (positionChanged) {
            Set<String> otherNames = td.getOtherStructure().getColumns().stream()
                    .map(ColumnDef::getColumnName).collect(Collectors.toSet());
            for (ColumnDef col : base.getColumns()) {
                if (!modifiedCols.contains(col.getColumnName()) && otherNames.contains(col.getColumnName())) {
                    addClauses.add("MODIFY COLUMN " + columnBody(col, afterTarget(base, col, td)));
                    modifiedCols.add(col.getColumnName());
                }
            }
        }

        List<String> clauses = new ArrayList<>(dropClauses);
        clauses.addAll(tableClauses);
        clauses.addAll(addClauses);
        if (clauses.isEmpty()) {
            return List.of();
        }
        String table = "`" + td.getTableName() + "`";
        if (opt.getDdlGranularity() == DdlGranularity.PER_TABLE) {
            return List.of("ALTER TABLE " + table + " " + String.join(", ", clauses) + ";");
        }
        return clauses.stream().map(c -> "ALTER TABLE " + table + " " + c + ";").toList();
    }

    /** 字段在基准侧的前驱（跳过被删除字段）；第一个字段返回 FIRST */
    private static String afterTarget(TableStructure base, ColumnDef col, TableDiff td) {
        Set<String> removed = td.getItems().stream()
                .filter(i -> i.getScope() == DiffScope.COLUMN && i.getAction() == DiffAction.REMOVED)
                .map(DiffItem::getObjectName).collect(Collectors.toSet());
        ColumnDef prev = null;
        for (ColumnDef c : base.getColumns()) {
            if (c.getColumnName().equals(col.getColumnName())) {
                break;
            }
            if (!removed.contains(c.getColumnName())) {
                prev = c;
            }
        }
        return prev == null ? "FIRST" : "AFTER `" + prev.getColumnName() + "`";
    }

    /** 完整字段定义（不含 ADD/MODIFY 前缀，含 AFTER/FIRST 定位） */
    private static String columnBody(ColumnDef col, String after) {
        StringBuilder sb = new StringBuilder();
        sb.append("`").append(col.getColumnName()).append("` ").append(col.getColumnType());
        if (Boolean.FALSE.equals(col.getNullable())) {
            sb.append(" NOT NULL");
        }
        if (col.getDefaultValue() != null) {
            if (isNumericDefault(col.getDefaultValue()) || "CURRENT_TIMESTAMP".equals(col.getDefaultValue().toUpperCase())) {
                sb.append(" DEFAULT ").append(col.getDefaultValue());
            } else {
                sb.append(" DEFAULT '").append(escape(col.getDefaultValue())).append("'");
            }
        }
        if (col.isAutoIncrement()) {
            sb.append(" AUTO_INCREMENT");
        }
        if (col.getColumnComment() != null) {
            sb.append(" COMMENT '").append(escape(col.getColumnComment())).append("'");
        }
        if (col.getExtra() != null) {
            sb.append(" ON UPDATE ").append(col.getExtra());
        }
        if (col.getColumnCharset() != null) {
            sb.append(" CHARACTER SET ").append(col.getColumnCharset());
        }
        if (col.getColumnCollate() != null) {
            sb.append(" COLLATE ").append(col.getColumnCollate());
        }
        sb.append(" ").append(after);
        return sb.toString();
    }

    private static boolean isNumericDefault(String value) {
        return value.matches("-?\\d+(\\.\\d+)?");
    }

    private static String addIndexClause(IndexDef idx) {
        if (idx.isPrimary() || "PRIMARY".equals(idx.getIndexName())) {
            return "ADD PRIMARY KEY (" + indexColumns(idx) + ")";
        }
        StringBuilder sb = new StringBuilder("ADD ");
        if (idx.isUnique()) {
            sb.append("UNIQUE ");
        } else if ("FULLTEXT".equals(idx.getIndexType()) || "SPATIAL".equals(idx.getIndexType())) {
            sb.append(idx.getIndexType()).append(" ");
        }
        sb.append("INDEX `").append(idx.getIndexName()).append("` (").append(indexColumns(idx)).append(")");
        if (idx.getIndexType() != null && !"BTREE".equals(idx.getIndexType())
                && !"FULLTEXT".equals(idx.getIndexType()) && !"SPATIAL".equals(idx.getIndexType())) {
            sb.append(" USING ").append(idx.getIndexType());
        }
        if (idx.getComment() != null) {
            sb.append(" COMMENT '").append(escape(idx.getComment())).append("'");
        }
        return sb.toString();
    }

    private static String indexColumns(IndexDef idx) {
        return idx.getColumnNames().stream().map(SchemaDiffUtil::indexColumn).collect(Collectors.joining(", "));
    }

    /** 索引列包裹反引号，保留前缀长度与 ASC/DESC 后缀：col(191) DESC → `col`(191) DESC */
    private static String indexColumn(String name) {
        String trimmed = name.trim();
        String order = "";
        Matcher m = INDEX_COLUMN_ORDER.matcher(trimmed);
        if (m.matches()) {
            trimmed = m.group(1).trim();
            order = " " + m.group(2);
        }
        int p = trimmed.lastIndexOf('(');
        if (p > 0 && trimmed.endsWith(")")) {
            return "`" + trimmed.substring(0, p) + "`" + trimmed.substring(p) + order;
        }
        return "`" + trimmed + "`" + order;
    }

    private static String dropIndexClause(String indexName) {
        return "PRIMARY".equals(indexName) ? "DROP PRIMARY KEY" : "DROP INDEX `" + indexName + "`";
    }

    private static String tableOptionClause(DiffItem item) {
        String v = item.getBaseValue();
        return switch (item.getProperty()) {
            case "tableComment" -> "COMMENT '" + escape(v) + "'";
            case "engine" -> "ENGINE=" + v;
            case "autoIncrement" -> "AUTO_INCREMENT=" + v;
            case "charset" -> "DEFAULT CHARSET=" + v;
            case "collate" -> "COLLATE=" + v;
            case "rowFormat" -> "ROW_FORMAT=" + v;
            default -> {
                log.error("unknown table option, property: {}, item: {}", item.getProperty(), item);
                throw ValidationException.of("未知表属性差异: " + item.getProperty());
            }
        };
    }

    private static String escape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("'", "''");
    }
}
