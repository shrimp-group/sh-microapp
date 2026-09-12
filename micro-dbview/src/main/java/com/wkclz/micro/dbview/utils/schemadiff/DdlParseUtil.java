package com.wkclz.micro.dbview.utils.schemadiff;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.dbview.utils.schemadiff.model.ColumnDef;
import com.wkclz.micro.dbview.utils.schemadiff.model.IndexDef;
import com.wkclz.micro.dbview.utils.schemadiff.model.TableStructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MySQL CREATE TABLE DDL 解析器（纯静态，零依赖，可整包移植）。
 * 输入：SHOW CREATE TABLE 输出或同格式 DDL 文本；输出：TableStructure 集合。
 */
public final class DdlParseUtil {

    private static final Logger log = LoggerFactory.getLogger(DdlParseUtil.class);

    private static final Pattern INDEX_NAME = Pattern.compile("`([^`]+)`");

    /** 双词关键词大小写不敏感、任意空白分隔（NOT\nNULL 等手写格式防御） */
    private static final Pattern NOT_NULL = Pattern.compile("^NOT\\s+NULL", Pattern.CASE_INSENSITIVE);
    private static final Pattern CHARACTER_SET = Pattern.compile("^CHARACTER\\s+SET", Pattern.CASE_INSENSITIVE);
    private static final Pattern ON_UPDATE = Pattern.compile("^ON\\s+UPDATE", Pattern.CASE_INSENSITIVE);

    private DdlParseUtil() {
    }

    /** 解析 DDL 文本，可含多条 CREATE TABLE，忽略非建表语句 */
    public static Map<String, TableStructure> parse(String ddlText) {
        log.info("parse ddl text, length: {}", ddlText == null ? 0 : ddlText.length());
        if (ddlText == null || ddlText.isBlank()) {
            throw ValidationException.of("DDL 文本不能为空");
        }
        Map<String, TableStructure> result = new LinkedHashMap<>();
        for (String statement : splitStatements(stripComments(ddlText))) {
            String trimmed = statement.trim();
            if (trimmed.toUpperCase().startsWith("CREATE TABLE")) {
                TableStructure ts = parseTable(trimmed);
                result.put(ts.getTableName(), ts);
            }
        }
        log.info("parse ddl text done, table count: {}", result.size());
        return result;
    }

    /** 解析单条 CREATE TABLE 语句 */
    public static TableStructure parseTable(String createTableSql) {
        String sql = stripComments(createTableSql).trim();
        if (sql.endsWith(";")) {
            sql = sql.substring(0, sql.length() - 1).trim();
        }
        String upper = sql.toUpperCase();
        if (!upper.startsWith("CREATE TABLE")) {
            throw ValidationException.of("不是 CREATE TABLE 语句: " + abbreviate(sql));
        }
        int bodyStart = sql.indexOf('(');
        if (bodyStart < 0) {
            throw ValidationException.of("CREATE TABLE 缺少表体定义: " + abbreviate(sql));
        }
        int bodyEnd = findMatchingParen(sql, bodyStart);
        if (bodyEnd < 0) {
            throw ValidationException.of("CREATE TABLE 括号不匹配: " + abbreviate(sql));
        }

        TableStructure ts = new TableStructure();
        ts.setTableName(extractTableName(sql.substring(0, bodyStart).trim()));
        log.info("parse table ddl, table: {}", ts.getTableName());
        ts.setOriginalDdl(createTableSql.trim());

        parseBody(sql.substring(bodyStart + 1, bodyEnd), ts);
        parseTableOptions(sql.substring(bodyEnd + 1).trim(), ts);
        return ts;
    }

    // ---------- 表体 ----------

    private static void parseBody(String body, TableStructure ts) {
        int position = 0;
        for (String part : splitTopLevel(body)) {
            String p = part.trim();
            if (p.isEmpty()) {
                continue;
            }
            String pu = p.toUpperCase();
            if (pu.startsWith("PRIMARY KEY")) {
                IndexDef pk = parseIndexDef(p);
                pk.setIndexName("PRIMARY");
                pk.setPrimary(true);
                pk.setUnique(true);
                ts.getIndexes().add(pk);
            } else if (pu.startsWith("UNIQUE KEY") || pu.startsWith("UNIQUE INDEX")) {
                ts.getIndexes().add(parseIndexDef(p));
            } else if (pu.startsWith("FULLTEXT KEY") || pu.startsWith("FULLTEXT INDEX")) {
                IndexDef idx = parseIndexDef(p);
                idx.setIndexType("FULLTEXT");
                ts.getIndexes().add(idx);
            } else if (pu.startsWith("SPATIAL KEY") || pu.startsWith("SPATIAL INDEX")) {
                IndexDef idx = parseIndexDef(p);
                idx.setIndexType("SPATIAL");
                ts.getIndexes().add(idx);
            } else if (pu.startsWith("KEY") || pu.startsWith("INDEX")) {
                ts.getIndexes().add(parseIndexDef(p));
            } else if (pu.startsWith("CONSTRAINT")) {
                log.info("ignore foreign key constraint: {}", abbreviate(p));
            } else {
                ts.getColumns().add(parseColumn(p, ++position));
            }
        }
    }

    private static ColumnDef parseColumn(String def, int position) {
        ColumnDef col = new ColumnDef();
        col.setOrdinalPosition(position);
        String s = def.trim();

        int nameEnd;
        if (s.startsWith("`")) {
            int end = s.indexOf('`', 1);
            if (end < 0) {
                throw ValidationException.of("字段名反引号不匹配: " + abbreviate(s));
            }
            col.setColumnName(s.substring(1, end));
            nameEnd = end + 1;
        } else {
            int sp = indexOfWhitespace(s, 0);
            if (sp < 0) {
                throw ValidationException.of("字段定义不完整: " + abbreviate(s));
            }
            col.setColumnName(s.substring(0, sp));
            nameEnd = sp;
        }

        String rest = s.substring(nameEnd).trim();
        int typeEnd = typeEnd(rest);
        col.setColumnType(rest.substring(0, typeEnd).trim());
        parseColumnOptions(rest.substring(typeEnd).trim(), col);
        return col;
    }

    /** 类型段结束下标：base token + 可选(参数) + unsigned/zerofill，如 decimal(10,2) unsigned */
    static int typeEnd(String rest) {
        int i = 0;
        while (i < rest.length() && !Character.isWhitespace(rest.charAt(i)) && rest.charAt(i) != '(') {
            i++;
        }
        if (i < rest.length() && rest.charAt(i) == '(') {
            int close = findMatchingParen(rest, i);
            if (close < 0) {
                throw ValidationException.of("字段类型括号不匹配: " + abbreviate(rest));
            }
            i = close + 1;
        }
        while (true) {
            int j = i;
            while (j < rest.length() && Character.isWhitespace(rest.charAt(j))) {
                j++;
            }
            String tail = rest.substring(j).toUpperCase();
            if (tail.startsWith("UNSIGNED") || tail.startsWith("ZEROFILL")) {
                i = j + 8;
            } else {
                break;
            }
        }
        return i;
    }

    private static void parseColumnOptions(String rest, ColumnDef col) {
        String rem = rest;
        while (!rem.isEmpty()) {
            String ru = rem.toUpperCase();
            Matcher m = NOT_NULL.matcher(rem);
            if (m.find()) {
                col.setNullable(false);
                rem = rem.substring(m.end()).trim();
            } else if (ru.startsWith("NULL")) {
                col.setNullable(true);
                rem = rem.substring(4).trim();
            } else if (ru.startsWith("DEFAULT")) {
                String[] kv = extractValueToken(rem.substring(7).trim());
                col.setDefaultValue(normalizeDefaultValue(kv[0]));
                rem = kv[1];
            } else if (ru.startsWith("AUTO_INCREMENT")) {
                col.setAutoIncrement(true);
                rem = rem.substring(14).trim();
            } else if (ru.startsWith("COMMENT")) {
                String[] kv = extractValueToken(rem.substring(7).trim());
                col.setColumnComment(kv[0]);
                rem = kv[1];
            } else if ((m = CHARACTER_SET.matcher(rem)).find()) {
                String[] kv = extractPlainToken(rem.substring(m.end()).trim());
                col.setColumnCharset(kv[0]);
                rem = kv[1];
            } else if (ru.startsWith("CHARSET")) {
                String[] kv = extractPlainToken(rem.substring(7).trim());
                col.setColumnCharset(kv[0]);
                rem = kv[1];
            } else if (ru.startsWith("COLLATE")) {
                String[] kv = extractPlainToken(rem.substring(7).trim());
                col.setColumnCollate(kv[0]);
                rem = kv[1];
            } else if ((m = ON_UPDATE.matcher(rem)).find()) {
                String[] kv = extractPlainToken(rem.substring(m.end()).trim());
                col.setExtra(kv[0].toUpperCase().replace("()", ""));
                rem = kv[1];
            } else {
                String[] kv = extractPlainToken(rem);
                if (kv[0].isEmpty()) {
                    break;
                }
                log.info("ignore column option token: {}", kv[0]);
                rem = kv[1];
            }
        }
    }

    // ---------- 索引 ----------

    private static IndexDef parseIndexDef(String def) {
        IndexDef idx = new IndexDef();
        String s = def.trim();
        if (s.toUpperCase().startsWith("UNIQUE")) {
            idx.setUnique(true);
        }
        int bodyStart = s.indexOf('(');
        if (bodyStart < 0) {
            throw ValidationException.of("索引定义缺少列组合: " + abbreviate(s));
        }
        int bodyEnd = findMatchingParen(s, bodyStart);
        String head = s.substring(0, bodyStart).trim();
        Matcher m = INDEX_NAME.matcher(head);
        if (m.find()) {
            idx.setIndexName(m.group(1));
        }
        String cols = s.substring(bodyStart + 1, bodyEnd);
        for (String c : splitTopLevel(cols)) {
            String cc = c.trim();
            if (cc.startsWith("`") && cc.contains("`")) {
                int end = cc.indexOf('`', 1);
                cc = cc.substring(1, end) + cc.substring(end + 1);
            }
            idx.getColumnNames().add(cc);
        }
        String tail = s.substring(bodyEnd + 1).trim();
        Matcher using = Pattern.compile("(?i)USING\\s+(BTREE|HASH)").matcher(tail);
        if (using.find()) {
            idx.setIndexType(using.group(1).toUpperCase());
        }
        Matcher comment = Pattern.compile("(?i)COMMENT\\s+'((?:[^']|'')*)'").matcher(tail);
        if (comment.find()) {
            idx.setComment(unescapeSql(comment.group(1)));
        }
        return idx;
    }

    // ---------- 表属性 ----------

    private static void parseTableOptions(String tail, TableStructure ts) {
        int i = 0;
        while (i < tail.length()) {
            while (i < tail.length() && Character.isWhitespace(tail.charAt(i))) {
                i++;
            }
            if (i >= tail.length()) {
                break;
            }
            int eq = tail.indexOf('=', i);
            if (eq < 0) {
                log.info("no '=' found, stop table options: {}", abbreviate(tail.substring(i)));
                break;
            }
            String key = tail.substring(i, eq).trim().toUpperCase();
            int vStart = eq + 1;
            while (vStart < tail.length() && Character.isWhitespace(tail.charAt(vStart))) {
                vStart++;
            }
            if (vStart >= tail.length()) {
                break;
            }
            String value;
            if (tail.charAt(vStart) == '\'') {
                String[] kv = extractValueToken(tail.substring(vStart));
                value = kv[0];
                i = vStart + (tail.substring(vStart).length() - kv[1].length());
            } else {
                int sp = vStart;
                while (sp < tail.length() && !Character.isWhitespace(tail.charAt(sp))) {
                    sp++;
                }
                value = tail.substring(vStart, sp);
                i = sp;
            }
            switch (key) {
                case "ENGINE" -> ts.setEngine(value);
                case "AUTO_INCREMENT" -> ts.setAutoIncrement(Long.valueOf(value));
                case "DEFAULT CHARSET", "CHARSET", "DEFAULT CHARACTER SET" -> ts.setCharset(value);
                case "COLLATE" -> ts.setCollate(value);
                case "COMMENT" -> ts.setTableComment(value);
                case "ROW_FORMAT" -> ts.setRowFormat(value);
                default -> log.info("ignore table option: {}={}", key, value);
            }
        }
    }

    // ---------- 表名与通用辅助 ----------

    static String extractTableName(String head) {
        Matcher m = Pattern.compile("(?i)CREATE\\s+TABLE\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?(.+)$",
                Pattern.DOTALL).matcher(head);
        if (!m.find()) {
            throw ValidationException.of("无法解析表名: " + abbreviate(head));
        }
        String name = m.group(1).trim();
        // `db`.`t` / db.t / `t` / t → 取最后一段
        int dot = name.lastIndexOf('.');
        if (dot >= 0) {
            name = name.substring(dot + 1);
        }
        return name.replace("`", "").replace("\"", "").trim();
    }

    /** 移除 --、# 行注释与块注释，保留字符串内容 */
    static String stripComments(String text) {
        StringBuilder sb = new StringBuilder(text.length());
        boolean inSingle = false;
        boolean inDouble = false;
        boolean inBacktick = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (inSingle) {
                sb.append(c);
                if (c == '\\' && i + 1 < text.length()) {
                    sb.append(text.charAt(i + 1));
                    i++;
                } else if (c == '\'') {
                    if (i + 1 < text.length() && text.charAt(i + 1) == '\'') {
                        sb.append('\'');
                        i++;
                    } else {
                        inSingle = false;
                    }
                }
                continue;
            }
            if (inDouble) {
                sb.append(c);
                if (c == '\\' && i + 1 < text.length()) {
                    sb.append(text.charAt(i + 1));
                    i++;
                } else if (c == '"') {
                    inDouble = false;
                }
                continue;
            }
            if (inBacktick) {
                sb.append(c);
                if (c == '`') {
                    inBacktick = false;
                }
                continue;
            }
            if (c == '\'') {
                inSingle = true;
                sb.append(c);
                continue;
            }
            if (c == '"') {
                inDouble = true;
                sb.append(c);
                continue;
            }
            if (c == '`') {
                inBacktick = true;
                sb.append(c);
                continue;
            }
            if (c == '-' && i + 1 < text.length() && text.charAt(i + 1) == '-') {
                while (i < text.length() && text.charAt(i) != '\n') {
                    i++;
                }
                sb.append('\n');
                continue;
            }
            if (c == '#') {
                while (i < text.length() && text.charAt(i) != '\n') {
                    i++;
                }
                sb.append('\n');
                continue;
            }
            if (c == '/' && i + 1 < text.length() && text.charAt(i + 1) == '*') {
                i += 2;
                while (i + 1 < text.length() && !(text.charAt(i) == '*' && text.charAt(i + 1) == '/')) {
                    i++;
                }
                i++;
                sb.append(' ');
                continue;
            }
            sb.append(c);
        }
        return sb.toString();
    }

    /** 按分号拆分语句，忽略字符串内分号 */
    static List<String> splitStatements(String text) {
        List<String> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inSingle = false;
        boolean inDouble = false;
        boolean inBacktick = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            sb.append(c);
            if (inSingle) {
                if (c == '\\' && i + 1 < text.length()) {
                    sb.append(text.charAt(i + 1));
                    i++;
                } else if (c == '\'') {
                    if (i + 1 < text.length() && text.charAt(i + 1) == '\'') {
                        sb.append('\'');
                        i++;
                    } else {
                        inSingle = false;
                    }
                }
                continue;
            }
            if (inDouble) {
                if (c == '\\' && i + 1 < text.length()) {
                    sb.append(text.charAt(i + 1));
                    i++;
                } else if (c == '"') {
                    inDouble = false;
                }
                continue;
            }
            if (inBacktick) {
                if (c == '`') {
                    inBacktick = false;
                }
                continue;
            }
            if (c == '\'') {
                inSingle = true;
                continue;
            }
            if (c == '"') {
                inDouble = true;
                continue;
            }
            if (c == '`') {
                inBacktick = true;
                continue;
            }
            if (c == ';') {
                list.add(sb.toString());
                sb.setLength(0);
            }
        }
        if (!sb.toString().isBlank()) {
            list.add(sb.toString());
        }
        return list;
    }

    /** 按顶层逗号拆分（括号内、字符串内的逗号不拆分） */
    static List<String> splitTopLevel(String s) {
        List<String> list = new ArrayList<>();
        int depth = 0;
        boolean inSingle = false;
        boolean inDouble = false;
        boolean inBacktick = false;
        int start = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (inSingle) {
                if (c == '\\' && i + 1 < s.length()) {
                    i++;
                } else if (c == '\'') {
                    if (i + 1 < s.length() && s.charAt(i + 1) == '\'') {
                        i++;
                    } else {
                        inSingle = false;
                    }
                }
                continue;
            }
            if (inDouble) {
                if (c == '\\' && i + 1 < s.length()) {
                    i++;
                } else if (c == '"') {
                    inDouble = false;
                }
                continue;
            }
            if (inBacktick) {
                if (c == '`') {
                    inBacktick = false;
                }
                continue;
            }
            if (c == '\'') {
                inSingle = true;
                continue;
            }
            if (c == '"') {
                inDouble = true;
                continue;
            }
            if (c == '`') {
                inBacktick = true;
                continue;
            }
            if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
            } else if (c == ',' && depth == 0) {
                list.add(s.substring(start, i));
                start = i + 1;
            }
        }
        if (start < s.length()) {
            list.add(s.substring(start));
        }
        return list;
    }

    /** 返回与 openIdx 处 '(' 配对的 ')' 下标，未找到返回 -1 */
    static int findMatchingParen(String s, int openIdx) {
        int depth = 0;
        boolean inSingle = false;
        boolean inDouble = false;
        boolean inBacktick = false;
        for (int i = openIdx; i < s.length(); i++) {
            char c = s.charAt(i);
            if (inSingle) {
                if (c == '\\' && i + 1 < s.length()) {
                    i++;
                } else if (c == '\'') {
                    if (i + 1 < s.length() && s.charAt(i + 1) == '\'') {
                        i++;
                    } else {
                        inSingle = false;
                    }
                }
                continue;
            }
            if (inDouble) {
                if (c == '\\' && i + 1 < s.length()) {
                    i++;
                } else if (c == '"') {
                    inDouble = false;
                }
                continue;
            }
            if (inBacktick) {
                if (c == '`') {
                    inBacktick = false;
                }
                continue;
            }
            if (c == '\'') {
                inSingle = true;
                continue;
            }
            if (c == '"') {
                inDouble = true;
                continue;
            }
            if (c == '`') {
                inBacktick = true;
                continue;
            }
            if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    /** 提取值 token：'字符串'（处理 '' 与 \ 转义）/ (表达式) / 裸 token，返回 [值, 剩余] */
    static String[] extractValueToken(String s) {
        s = s.trim();
        if (s.startsWith("'")) {
            StringBuilder sb = new StringBuilder();
            int i = 1;
            while (i < s.length()) {
                char c = s.charAt(i);
                if (c == '\'') {
                    if (i + 1 < s.length() && s.charAt(i + 1) == '\'') {
                        sb.append('\'');
                        i += 2;
                        continue;
                    }
                    i++;
                    break;
                }
                if (c == '\\' && i + 1 < s.length()) {
                    sb.append(s.charAt(i + 1));
                    i += 2;
                    continue;
                }
                sb.append(c);
                i++;
            }
            return new String[]{sb.toString(), s.substring(Math.min(i, s.length())).trim()};
        }
        if (s.startsWith("(")) {
            int close = findMatchingParen(s, 0);
            if (close < 0) {
                throw ValidationException.of("表达式括号不匹配: " + abbreviate(s));
            }
            return new String[]{s.substring(1, close).trim(), s.substring(close + 1).trim()};
        }
        return extractPlainToken(s);
    }

    /** 提取裸 token（到下一个空白为止），返回 [token, 剩余] */
    static String[] extractPlainToken(String s) {
        s = s.trim();
        int sp = indexOfWhitespace(s, 0);
        if (sp < 0) {
            return new String[]{s, ""};
        }
        return new String[]{s.substring(0, sp), s.substring(sp).trim()};
    }

    static String normalizeDefaultValue(String value) {
        return "NULL".equalsIgnoreCase(value) ? null : value;
    }

    static String unescapeSql(String value) {
        return value == null ? null : value.replace("''", "'");
    }

    static int indexOfWhitespace(String s, int from) {
        for (int i = from; i < s.length(); i++) {
            if (Character.isWhitespace(s.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    static String abbreviate(String s) {
        return s.length() <= 64 ? s : s.substring(0, 64) + "...";
    }
}
