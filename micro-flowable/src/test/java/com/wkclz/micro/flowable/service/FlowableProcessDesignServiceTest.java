package com.wkclz.micro.flowable.service;

import com.wkclz.core.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * BPMN 分类归一化单元测试（纯 JUnit，不依赖 Spring 上下文）
 * 验证部署前 targetNamespace 改写：引擎 ProcessDefinition.category 取自 definitions targetNamespace，
 * 改写后流程定义查询/分类过滤与设计分类一致
 */
class FlowableProcessDesignServiceTest {

    private final FlowableProcessDesignService service = new FlowableProcessDesignService();

    /** bpmn-js 默认导出格式（默认命名空间，多行） */
    private static final String XML_DEFAULT_NS = """
            <?xml version="1.0" encoding="UTF-8"?>
            <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:flowable="http://flowable.org/bpmn" targetNamespace="http://bpmn.io/schema/bpmn">
              <process id="flow_123" name="请假流程" isExecutable="true">
                <startEvent id="start_1" />
              </process>
            </definitions>
            """;

    /** 带命名空间前缀且无 targetNamespace 属性的格式 */
    private static final String XML_NO_TARGET_NS = """
            <?xml version="1.0" encoding="UTF-8"?>
            <bpmn2:definitions xmlns:bpmn2="http://www.omg.org/spec/BPMN/20100524/MODEL" id="defs_1">
              <bpmn2:process id="flow_123" name="请假流程" isExecutable="true" />
            </bpmn2:definitions>
            """;

    @Test
    void shouldRewriteTargetNamespaceToCategory() {
        String result = service.normalizeCategory(XML_DEFAULT_NS, "CAT_LEAVE");
        // targetNamespace 改写为分类编码，process 与其余内容不变
        assertTrue(result.contains("targetNamespace=\"CAT_LEAVE\""));
        assertFalse(result.contains("http://bpmn.io/schema/bpmn"));
        assertTrue(result.contains("<process id=\"flow_123\" name=\"请假流程\" isExecutable=\"true\">"));
        assertTrue(result.contains("xmlns:flowable=\"http://flowable.org/bpmn\""));
    }

    @Test
    void shouldInsertTargetNamespaceWhenMissing() {
        String result = service.normalizeCategory(XML_NO_TARGET_NS, "CAT_LEAVE");
        // 无 targetNamespace 属性时补插在 definitions 标签名后
        assertTrue(result.contains("<bpmn2:definitions targetNamespace=\"CAT_LEAVE\" xmlns:bpmn2=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" id=\"defs_1\">"));
    }

    @Test
    void shouldBeIdempotent() {
        String once = service.normalizeCategory(XML_DEFAULT_NS, "CAT_LEAVE");
        String twice = service.normalizeCategory(once, "CAT_LEAVE");
        assertEquals(once, twice);
    }

    @Test
    void shouldReturnOriginalWhenCategoryBlank() {
        assertEquals(XML_DEFAULT_NS, service.normalizeCategory(XML_DEFAULT_NS, null));
        assertEquals(XML_DEFAULT_NS, service.normalizeCategory(XML_DEFAULT_NS, ""));
    }

    @Test
    void shouldThrowWhenDefinitionsMissing() {
        assertThrows(ValidationException.class, () -> service.normalizeCategory("<process id=\"p1\" />", "CAT_LEAVE"));
    }
}
