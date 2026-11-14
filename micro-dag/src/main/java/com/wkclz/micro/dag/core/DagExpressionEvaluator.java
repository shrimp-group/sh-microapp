package com.wkclz.micro.dag.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 分支表达式引擎（无第三方依赖）
 * 语法：${ctx.key} / ${out.node.key} 占位符；== != > >= < <=；and or；括号；'str' "str" 数字 true false null
 * 求值语义：两边均可转数字则按数值比较；否则按字符串比较；null 只与 null 相等；单值表达式按真值(null=false)
 */
@Slf4j
@Component
public class DagExpressionEvaluator {

    /** 表达式语法错误 */
    public static class DagExpressionException extends RuntimeException {
        public DagExpressionException(String message) { super(message); }
    }

    public boolean evaluate(String expr, Map<String, Object> ctx, Map<String, Map<String, Object>> nodeOutputs) {
        log.info("DagExpressionEvaluator.evaluate expr={}", expr);
        List<Token> tokens = tokenize(expr);
        Parser parser = new Parser(tokens, ctx, nodeOutputs, expr);
        boolean result = parser.parseOr();
        parser.expectEnd();
        return result;
    }

    // ---------- 词法 ----------

    private record Token(TokenType type, String text) {}

    private enum TokenType { PLACEHOLDER, STRING, NUMBER, BOOLEAN, NULL, OP, LPAREN, RPAREN }

    private List<Token> tokenize(String expr) {
        List<Token> tokens = new ArrayList<>();
        int i = 0;
        int n = expr.length();
        while (i < n) {
            char c = expr.charAt(i);
            if (Character.isWhitespace(c)) { i++; continue; }
            if (c == '(') { tokens.add(new Token(TokenType.LPAREN, "(")); i++; continue; }
            if (c == ')') { tokens.add(new Token(TokenType.RPAREN, ")")); i++; continue; }
            if (c == '\'' || c == '"') {
                int end = expr.indexOf(c, i + 1);
                if (end < 0) { throw err(expr, i, "字符串未闭合"); }
                tokens.add(new Token(TokenType.STRING, expr.substring(i + 1, end)));
                i = end + 1;
                continue;
            }
            if (c == '$' && i + 1 < n && expr.charAt(i + 1) == '{') {
                int end = expr.indexOf('}', i + 2);
                if (end < 0) { throw err(expr, i, "占位符未闭合"); }
                tokens.add(new Token(TokenType.PLACEHOLDER, expr.substring(i + 2, end)));
                i = end + 1;
                continue;
            }
            if (Character.isDigit(c)) {
                int j = i;
                while (j < n && (Character.isDigit(expr.charAt(j)) || expr.charAt(j) == '.')) { j++; }
                tokens.add(new Token(TokenType.NUMBER, expr.substring(i, j)));
                i = j;
                continue;
            }
            // 多字符运算符优先匹配
            String rest = expr.substring(i);
            String[] words = {">=", "<=", "==", "!=", "and", "or", ">", "<", "true", "false", "null"};
            boolean matched = false;
            for (String w : words) {
                if (rest.startsWith(w)
                    && (w.length() > 1 || !isWordChar(i + 1, expr))) { // 单字符 > < 需防误切 and/or 内部字母场景由顺序保证
                    tokens.add(new Token(tokenTypeOf(w), w));
                    i += w.length();
                    matched = true;
                    break;
                }
            }
            if (matched) { continue; }
            throw err(expr, i, "无法识别的字符 '" + c + "'");
        }
        return tokens;
    }

    private boolean isWordChar(int pos, String expr) {
        return pos < expr.length() && Character.isLetter(expr.charAt(pos));
    }

    private TokenType tokenTypeOf(String w) {
        return switch (w) {
            case "and", "or", ">=", "<=", "==", "!=", ">", "<" -> TokenType.OP;
            case "true", "false" -> TokenType.BOOLEAN;
            case "null" -> TokenType.NULL;
            default -> throw new IllegalStateException(w);
        };
    }

    private static DagExpressionException err(String expr, int pos, String msg) {
        DagExpressionException ex = new DagExpressionException("表达式语法错误，位置 " + pos + "：" + msg + "，表达式：" + expr);
        log.error("DagExpressionEvaluator 语法错误: {}", ex.getMessage());
        return ex;
    }

    // ---------- 语法（递归下降）----------

    private static class Parser {
        private final List<Token> tokens;
        private final Map<String, Object> ctx;
        private final Map<String, Map<String, Object>> nodeOutputs;
        private final String expr;
        private int pos;

        Parser(List<Token> tokens, Map<String, Object> ctx,
               Map<String, Map<String, Object>> nodeOutputs, String expr) {
            this.tokens = tokens;
            this.ctx = ctx == null ? Map.of() : ctx;
            this.nodeOutputs = nodeOutputs == null ? Map.of() : nodeOutputs;
            this.expr = expr;
        }

        /** or 表达式（优先级最低） */
        boolean parseOr() {
            boolean left = parseAnd();
            while (matchOp("or")) {
                boolean right = parseAnd();
                left = left || right;
            }
            return left;
        }

        boolean parseAnd() {
            boolean left = parseCompare();
            while (matchOp("and")) {
                boolean right = parseCompare();
                left = left && right;
            }
            return left;
        }

        boolean parseCompare() {
            if (peekType() == TokenType.LPAREN) {
                consume();
                boolean v = parseOr();
                expect(TokenType.RPAREN, "缺少右括号");
                return v;
            }
            // 单值真值表达式（无比较运算符）
            Object left = parsePrimary();
            String op = peekOp();
            if (op == null) {
                return truthy(left);
            }
            if (!List.of("==", "!=", ">", ">=", "<", "<=").contains(op)) {
                throw err(expr, pos, "不支持的运算符 '" + op + "'");
            }
            consume();
            Object right = parsePrimary();
            return compare(left, op, right);
        }

        Object parsePrimary() {
            Token t = peek();
            if (t == null) { throw err(expr, pos, "表达式意外结束"); }
            return switch (t.type()) {
                case PLACEHOLDER -> { consume(); yield resolve(t.text()); }
                case STRING -> { consume(); yield t.text(); }
                case NUMBER -> { consume(); yield new BigDecimal(t.text()); }
                case BOOLEAN -> { consume(); yield Boolean.parseBoolean(t.text()); }
                case NULL -> { consume(); yield null; }
                default -> throw err(expr, pos, "意外的记号 '" + t.text() + "'");
            };
        }

        /** 解析占位符：ctx.key 或 out.node.key */
        Object resolve(String placeholder) {
            String[] parts = placeholder.split("\\.", 3);
            if (parts.length < 2) {
                throw err(expr, pos, "占位符格式非法：" + placeholder);
            }
            if ("ctx".equals(parts[0])) {
                return ctx.get(parts[1]);
            }
            if ("out".equals(parts[0])) {
                Map<String, Object> out = nodeOutputs.get(parts[1]);
                return out == null ? null : out.get(parts[2]);
            }
            throw err(expr, pos, "占位符仅支持 ${ctx.*} 与 ${out.<nodeCode>.*}：" + placeholder);
        }

        boolean compare(Object left, String op, Object right) {
            boolean eq = Objects.equals(asString(left), asString(right)) && numericEqualsAware(left, right);
            return switch (op) {
                case "==" -> eq;
                case "!=" -> !eq;
                case ">", ">=", "<", "<=" -> compareOrdered(left, op, right);
                default -> throw new IllegalStateException(op);
            };
        }

        /** 数值优先比较：两边均可转数值按数值，否则按字符串字典序 */
        private boolean compareOrdered(Object left, String op, Object right) {
            if (left == null || right == null) {
                return false;
            }
            BigDecimal ln = toNumber(left);
            BigDecimal rn = toNumber(right);
            int cmp = (ln != null && rn != null)
                ? ln.compareTo(rn)
                : asString(left).compareTo(asString(right));
            return switch (op) {
                case ">" -> cmp > 0;
                case ">=" -> cmp >= 0;
                case "<" -> cmp < 0;
                case "<=" -> cmp <= 0;
                default -> throw new IllegalStateException(op);
            };
        }

        /** == / != 数值语义：'3' == 3.0 视为相等 */
        private boolean numericEqualsAware(Object left, Object right) {
            BigDecimal ln = toNumber(left);
            BigDecimal rn = toNumber(right);
            if (ln != null && rn != null) {
                return ln.compareTo(rn) == 0;
            }
            return true; // 已由字符串相等判定
        }

        private boolean truthy(Object v) {
            return v != null && !"false".equals(asString(v));
        }

        private BigDecimal toNumber(Object v) {
            if (v instanceof BigDecimal bd) { return bd; }
            if (v instanceof Number num) { return new BigDecimal(num.toString()); }
            if (v instanceof String s) {
                try { return new BigDecimal(s.trim()); } catch (NumberFormatException e) { return null; }
            }
            return null;
        }

        private String asString(Object v) {
            return v == null ? null : (v instanceof BigDecimal bd ? bd.stripTrailingZeros().toPlainString() : v.toString());
        }

        private String peekOp() {
            Token t = peek();
            return t != null && t.type() == TokenType.OP ? t.text() : null;
        }

        private boolean matchOp(String text) {
            Token t = peek();
            if (t != null && t.type() == TokenType.OP && t.text().equals(text)) {
                consume();
                return true;
            }
            return false;
        }

        private Token peek() { return pos < tokens.size() ? tokens.get(pos) : null; }
        private TokenType peekType() { Token t = peek(); return t == null ? null : t.type(); }
        private void consume() { pos++; }

        private void expect(TokenType type, String msg) {
            Token t = peek();
            if (t == null || t.type() != type) { throw err(expr, pos, msg); }
            consume();
        }

        void expectEnd() {
            if (pos < tokens.size()) {
                throw err(expr, pos, "存在未消费的记号 '" + tokens.get(pos).text() + "'");
            }
        }
    }
}
