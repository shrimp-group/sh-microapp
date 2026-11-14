package com.wkclz.micro.dag.core;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 表达式引擎单测：占位符解析 / 比较运算 / and or 括号 / 语法错误 / null 语义
 */
class DagExpressionEvaluatorTest {

    private final DagExpressionEvaluator evaluator = new DagExpressionEvaluator();

    private Map<String, Object> ctx() {
        Map<String, Object> ctx = new HashMap<>();
        ctx.put("sourceType", "api");
        ctx.put("count", 3);
        ctx.put("flag", true);
        ctx.put("empty", null);
        return ctx;
    }

    private Map<String, Map<String, Object>> outs() {
        Map<String, Map<String, Object>> outs = new HashMap<>();
        Map<String, Object> fetch = new HashMap<>();
        fetch.put("rows", 100);
        outs.put("fetch", fetch);
        return outs;
    }

    @Test
    void shouldEvaluateStringEquality() {
        assertTrue(evaluator.evaluate("${ctx.sourceType} == 'api'", ctx(), outs()));
        assertTrue(evaluator.evaluate("${ctx.sourceType} != 'file'", ctx(), outs()));
    }

    @Test
    void shouldEvaluateNumericComparison() {
        assertTrue(evaluator.evaluate("${ctx.count} >= 3", ctx(), outs()));
        assertTrue(evaluator.evaluate("${ctx.count} < 5 and ${out.fetch.rows} == 100", ctx(), outs()));
    }

    @Test
    void shouldEvaluateOrAndParentheses() {
        assertTrue(evaluator.evaluate("(${ctx.sourceType} == 'file' or ${ctx.count} == 3) and ${ctx.flag} == true", ctx(), outs()));
    }

    @Test
    void shouldTreatNullAsNull() {
        assertTrue(evaluator.evaluate("${ctx.empty} == null", ctx(), outs()));
        assertTrue(evaluator.evaluate("${ctx.empty} != 'x'", ctx(), outs()));
    }

    @Test
    void shouldThrowOnSyntaxError() {
        DagExpressionEvaluator.DagExpressionException ex = assertThrows(
            DagExpressionEvaluator.DagExpressionException.class,
            () -> evaluator.evaluate("${ctx.sourceType} === 'api'", ctx(), outs()));
        assertTrue(ex.getMessage().contains("位置"));
    }

    @Test
    void shouldThrowOnUnclosedPlaceholder() {
        assertThrows(DagExpressionEvaluator.DagExpressionException.class,
            () -> evaluator.evaluate("${ctx.sourceType == 'api'", ctx(), outs()));
    }

    @Test
    void shouldResolveUnknownPlaceholderAsNull() {
        assertTrue(evaluator.evaluate("${ctx.notExist} == null", ctx(), outs()));
    }

    @Test
    void shouldReturnFalseForNonBooleanExpression() {
        // 单一占位符求值为非 null 值时按真值处理，null 按假值处理
        assertTrue(evaluator.evaluate("${ctx.flag}", ctx(), outs()));
        assertEquals(false, evaluator.evaluate("${ctx.empty}", ctx(), outs()));
    }
}
