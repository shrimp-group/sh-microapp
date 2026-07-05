package com.wkclz.micro.report.helper;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.util.JdbcUtils;
import com.wkclz.core.base.PageData;
import com.wkclz.core.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectItem;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 报表 SQL 执行引擎
 * 支持动态 SQL 执行、参数提取、结果列提取、安全检查、分页查询、驼峰转换
 */
@Slf4j
@Component
public class ReportSqlHelper {

    private static final Pattern PARAM_PATTERN = Pattern.compile("#\\{([^}]+)}");
    private static final Pattern COLLECTION_PATTERN = Pattern.compile("collection=\"([^\"]+)\"");

    @Autowired
    private SqlSession sqlSession;

    /**
     * SQL 安全检查：仅允许 SELECT 语句
     * 先清理 MyBatis 动态标签，再进行 SQL 解析
     */
    public boolean isPureSelect(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return false;
        }
        try {
            String cleanSql = cleanMyBatisTags(sql);
            List<SQLStatement> statements = SQLUtils.parseStatements(cleanSql, JdbcUtils.MYSQL);
            for (SQLStatement stmt : statements) {
                if (!(stmt instanceof SQLSelectStatement)) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            log.warn("SQL解析失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 从 SQL 中提取参数名（#{paramName} 格式）
     */
    public List<String> sql2Params(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> params = new LinkedHashSet<>();

        // 提取 #{paramName} 格式的参数
        Matcher matcher = PARAM_PATTERN.matcher(sql);
        while (matcher.find()) {
            String param = matcher.group(1).trim();
            // 处理带有属性访问的情况，如 #{item.field} -> item
            if (param.contains(".")) {
                param = param.substring(0, param.indexOf("."));
            }
            params.add(param);
        }

        // 提取 collection="xxx" 格式的参数（foreach 标签）
        Matcher collectionMatcher = COLLECTION_PATTERN.matcher(sql);
        while (collectionMatcher.find()) {
            params.add(collectionMatcher.group(1));
        }

        // 移除分页内置参数
        params.remove("current");
        params.remove("size");
        params.remove("offset");

        return new ArrayList<>(params);
    }

    /**
     * 从 SQL 中提取结果列名
     */
    public List<String> sql2Results(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return Collections.emptyList();
        }
        try {
            // 预处理：移除 MyBatis 动态标签
            String cleanSql = cleanMyBatisTags(sql);
            net.sf.jsqlparser.statement.Statement statement = CCJSqlParserUtil.parse(cleanSql);
            if (!(statement instanceof Select)) {
                return Collections.emptyList();
            }
            Select select = (Select) statement;
            if (!(select.getSelectBody() instanceof PlainSelect)) {
                return Collections.emptyList();
            }
            PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
            List<String> columns = new ArrayList<>();
            for (SelectItem item : plainSelect.getSelectItems()) {
                // 使用 toString() 获取列表达式，再提取列名或别名
                String itemStr = item.toString();
                // 如果有 AS 别名，取别名
                if (itemStr.toUpperCase().contains(" AS ")) {
                    String alias = itemStr.substring(itemStr.toUpperCase().lastIndexOf(" AS ") + 4).trim();
                    columns.add(alias);
                } else if (itemStr.equals("*")) {
                    // SELECT * 无法提取列名，跳过
                    continue;
                } else {
                    // 取最后一部分作为列名（处理 table.column 格式）
                    String colName = itemStr;
                    if (colName.contains(".")) {
                        colName = colName.substring(colName.lastIndexOf(".") + 1);
                    }
                    // 去除引号
                    colName = colName.replace("`", "").replace("\"", "").replace("'", "");
                    columns.add(colName);
                }
            }
            return columns;
        } catch (JSQLParserException e) {
            log.warn("SQL结果列提取失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 执行 SQL 查询，返回列表
     * @param toCamel 是否将下划线字段名转为驼峰
     */
    public List<LinkedHashMap<String, Object>> selectList(String sql, Map<String, Object> params, boolean toCamel) {
        String statementId = registerSql(sql);
        if (params == null) {
            params = new HashMap<>();
        }
        List<LinkedHashMap<String, Object>> result = sqlSession.selectList(statementId, params);
        if (result == null) {
            return Collections.emptyList();
        }
        if (toCamel) {
            return convertKeysToCamel(result);
        }
        return result;
    }

    /**
     * 执行分页查询
     * 使用手动 LIMIT 分页，因为 PageHelper 对动态注册的 MappedStatement 不生效
     * @param toCamel 是否将下划线字段名转为驼峰
     */
    public PageData<LinkedHashMap<String, Object>> selectPage(String sql, Map<String, Object> params,
                                                                String countSql, Integer current, Integer size,
                                                                boolean toCamel) {
        if (current == null || current < 1) { current = 1; }
        if (size == null || size < 1) { size = 10; }

        // 查询总数
        long total = 0;
        if (countSql != null && !countSql.trim().isEmpty()) {
            String countStatementId = registerSql(countSql);
            List<LinkedHashMap<String, Object>> countResult = sqlSession.selectList(countStatementId, params);
            if (countResult != null && !countResult.isEmpty()) {
                Object countVal = countResult.get(0).values().iterator().next();
                total = Long.parseLong(String.valueOf(countVal));
            }
        } else {
            // 自动生成 COUNT SQL
            String autoCountSql = generateCountSql(sql);
            if (autoCountSql != null) {
                String countStatementId = registerSql(autoCountSql);
                List<LinkedHashMap<String, Object>> countResult = sqlSession.selectList(countStatementId, params);
                if (countResult != null && !countResult.isEmpty()) {
                    Object countVal = countResult.get(0).values().iterator().next();
                    total = Long.parseLong(String.valueOf(countVal));
                }
            }
        }

        // 查询数据：手动拼接 LIMIT
        if (params == null) {
            params = new HashMap<>();
        }
        int offset = (current - 1) * size;
        String pagedSql = sql + " LIMIT " + offset + ", " + size;

        String dataStatementId = registerSql(pagedSql);
        List<LinkedHashMap<String, Object>> data = sqlSession.selectList(dataStatementId, params);

        if (data != null && toCamel) {
            data = convertKeysToCamel(data);
        }

        return PageData.of(data != null ? data : Collections.emptyList(), total, (long) current, (long) size);
    }

    /**
     * 统一执行器
     * @param resultType OBJECT/LIST/PAGE
     * @param sql SQL脚本
     * @param params 参数
     * @param toCamel 是否驼峰转换
     * @param countSql 自定义COUNT SQL（仅PAGE类型使用）
     * @param current 当前页（仅PAGE类型使用）
     * @param size 每页大小（仅PAGE类型使用）
     */
    public Object sqlExecutor(String resultType, String sql, Map<String, Object> params, boolean toCamel,
                              String countSql, Integer current, Integer size) {
        if (!isPureSelect(sql)) {
            throw ValidationException.of("仅允许SELECT查询语句");
        }

        if ("OBJECT".equals(resultType)) {
            List<LinkedHashMap<String, Object>> list = selectList(sql, params, toCamel);
            if (list.isEmpty()) {
                return null;
            }
            if (list.size() > 1) {
                throw ValidationException.of("OBJECT类型查询结果超过一条，请检查SQL");
            }
            return list.get(0);
        } else if ("LIST".equals(resultType)) {
            return selectList(sql, params, toCamel);
        } else if ("PAGE".equals(resultType)) {
            return selectPage(sql, params, countSql, current, size, toCamel);
        }
        throw ValidationException.of("不支持的返回值类型: " + resultType);
    }

    /**
     * 将结果列表的 key 从下划线转为驼峰
     */
    private List<LinkedHashMap<String, Object>> convertKeysToCamel(List<LinkedHashMap<String, Object>> list) {
        List<LinkedHashMap<String, Object>> result = new ArrayList<>(list.size());
        for (LinkedHashMap<String, Object> row : list) {
            LinkedHashMap<String, Object> newRow = new LinkedHashMap<>(row.size());
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                newRow.put(toCamelCase(entry.getKey()), entry.getValue());
            }
            result.add(newRow);
        }
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

    /**
     * 将 SQL 动态注册为 MyBatis MappedStatement
     * 使用 MD5 作为 namespace 和 statementId，避免重复注册
     */
    private String registerSql(String sql) {
        String md5 = md5(sql);
        String namespace = "report_dynamic_" + md5;
        String statementId = namespace + ".select";

        Configuration configuration = sqlSession.getConfiguration();
        if (configuration.hasStatement(statementId)) {
            return statementId;
        }

        // 构建 XML mapper
        String xml = buildMapperXml(namespace, sql);
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
            XMLMapperBuilder builder = new XMLMapperBuilder(inputStream, configuration, "report_dynamic_" + md5, configuration.getSqlFragments());
            builder.parse();
            log.info("动态注册SQL: {}", statementId);
        } catch (Exception e) {
            // 可能已被其他线程注册
            if (!configuration.hasStatement(statementId)) {
                log.error("注册动态SQL失败: {}", e.getMessage());
                throw new RuntimeException("注册动态SQL失败: " + e.getMessage(), e);
            }
        }
        return statementId;
    }

    private String buildMapperXml(String namespace, String sql) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n");
        sb.append("<mapper namespace=\"").append(namespace).append("\">\n");
        sb.append("  <select id=\"select\" resultType=\"java.util.LinkedHashMap\">\n");
        sb.append(sql);
        sb.append("\n  </select>\n");
        sb.append("</mapper>");
        return sb.toString();
    }

    /**
     * 清理 MyBatis 动态标签，用于 SQL 解析
     * 处理 XML 实体转义（&lt; &gt; &amp; &apos; &quot;）和 CDATA 包裹
     */
    private String cleanMyBatisTags(String sql) {
        String clean = sql;

        // 先处理 CDATA：提取 <![CDATA[...]]> 中的内容
        clean = clean.replaceAll("<!\\[CDATA\\[", "");
        clean = clean.replaceAll("]]>", "");

        // 还原 XML 实体转义，使 MyBatis 标签能被后续正则正确匹配
        clean = clean.replace("&lt;", "<");
        clean = clean.replace("&gt;", ">");
        clean = clean.replace("&amp;", "&");
        clean = clean.replace("&apos;", "'");
        clean = clean.replace("&quot;", "\"");

        // 移除 <if> 标签，保留内容
        clean = clean.replaceAll("<if\\s+test=\"[^\"]*\">", "");
        clean = clean.replaceAll("</if>", "");
        // 移除 <where> 标签
        clean = clean.replaceAll("<where>", "WHERE");
        clean = clean.replaceAll("</where>", "");
        // 移除 <set> 标签
        clean = clean.replaceAll("<set>", "SET");
        clean = clean.replaceAll("</set>", "");
        // 移除 <foreach> 标签，保留内容
        clean = clean.replaceAll("<foreach[^>]*>", "");
        clean = clean.replaceAll("</foreach>", "");
        // 移除 <choose>/<when>/<otherwise> 标签，保留内容
        clean = clean.replaceAll("<choose>", "");
        clean = clean.replaceAll("</choose>", "");
        clean = clean.replaceAll("<when\\s+test=\"[^\"]*\">", "");
        clean = clean.replaceAll("</when>", "");
        clean = clean.replaceAll("<otherwise>", "");
        clean = clean.replaceAll("</otherwise>", "");
        // 移除 <trim> 标签，保留内容
        clean = clean.replaceAll("<trim[^>]*>", "");
        clean = clean.replaceAll("</trim>", "");
        // 移除其他 XML 标签
        clean = clean.replaceAll("<[^>]+>", "");
        // 替换 #{} 参数为占位值
        clean = clean.replaceAll("#\\{[^}]+}", "'1'");
        return clean;
    }

    /**
     * 自动生成 COUNT SQL
     */
    private String generateCountSql(String sql) {
        try {
            String cleanSql = cleanMyBatisTags(sql);
            // 简单方式：用正则替换 SELECT ... FROM 为 SELECT COUNT(*) FROM
            String upperSql = cleanSql.toUpperCase().trim();
            int fromIndex = upperSql.indexOf(" FROM ");
            if (fromIndex < 0) {
                return null;
            }
            String countSql = "SELECT COUNT(*) " + cleanSql.substring(fromIndex);
            // 移除 ORDER BY
            int orderByIndex = countSql.toUpperCase().lastIndexOf(" ORDER BY ");
            if (orderByIndex > 0) {
                countSql = countSql.substring(0, orderByIndex);
            }
            return countSql;
        } catch (Exception e) {
            log.warn("自动生成COUNT SQL失败: {}", e.getMessage());
            return null;
        }
    }

    private String md5(String str) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(str.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            return String.valueOf(str.hashCode());
        }
    }

}
