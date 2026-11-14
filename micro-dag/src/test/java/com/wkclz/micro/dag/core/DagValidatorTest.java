package com.wkclz.micro.dag.core;

import com.wkclz.micro.dag.bean.dto.DagDefModel;
import com.wkclz.micro.dag.bean.dto.EdgeDef;
import com.wkclz.micro.dag.bean.dto.NodeDef;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 定义校验器单测：环检测 / 悬空依赖 / 条件边缺 when / nodeCode 格式 / 重复编码
 */
class DagValidatorTest {

    private final DagValidator validator = new DagValidator();

    private NodeDef node(String code) {
        NodeDef n = new NodeDef();
        n.setCode(code);
        n.setName(code);
        n.setHandler("h-" + code);
        return n;
    }

    private EdgeDef edge(String fromCode, String toCode) {
        EdgeDef e = new EdgeDef();
        e.setEdgeCode(fromCode + "->" + toCode);
        e.setFromCode(fromCode);
        e.setToCode(toCode);
        return e;
    }

    @Test
    void shouldPassValidDag() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        def.setNodes(List.of(node("a"), node("b")));
        def.setEdges(List.of(edge("a", "b")));
        assertTrue(validator.validate(def, Set.of("h-a", "h-b")).isEmpty());
    }

    @Test
    void shouldDetectCycle() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        def.setNodes(List.of(node("a"), node("b"), node("c")));
        def.setEdges(List.of(edge("a", "b"), edge("b", "c"), edge("c", "a")));
        List<String> errors = validator.validate(def, Set.of("h-a", "h-b", "h-c"));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("环"));
    }

    @Test
    void shouldDetectDanglingDependency() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        def.setNodes(List.of(node("a")));
        def.setEdges(List.of(edge("ghost", "a")));
        List<String> errors = validator.validate(def, Set.of("h-a"));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("ghost"));
    }

    @Test
    void shouldDetectConditionEdgeWithoutWhen() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        def.setNodes(List.of(node("a"), node("b")));
        EdgeDef e = edge("a", "b");
        e.setType(EdgeDef.TYPE_CONDITION);
        def.setEdges(List.of(e));
        List<String> errors = validator.validate(def, Set.of("h-a", "h-b"));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("when"));
    }

    @Test
    void shouldDetectUnknownHandler() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        def.setNodes(List.of(node("a")));
        def.setEdges(List.of());
        List<String> errors = validator.validate(def, Set.of("h-other"));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("h-a"));
    }

    @Test
    void shouldDetectDuplicateAndBadCode() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        def.setNodes(List.of(node("a"), node("A")));   // 唯一化按小写比较，A 与 a 冲突
        def.setEdges(List.of());
        List<String> errors = validator.validate(def, Set.of("h-a"));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("重复"));
    }

    @Test
    void shouldDetectBadNodeCodeFormat() {
        DagDefModel def = new DagDefModel();
        def.setDagCode("d1");
        def.setDagVersion(1);
        NodeDef n = node("Bad_Code");                  // 只允许 [a-z0-9-]
        def.setNodes(List.of(n));
        def.setEdges(List.of());
        List<String> errors = validator.validate(def, Set.of("h-Bad_Code"));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("Bad_Code"));
    }
}
