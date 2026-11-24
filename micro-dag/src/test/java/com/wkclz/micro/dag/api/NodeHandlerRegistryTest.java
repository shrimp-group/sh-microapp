package com.wkclz.micro.dag.api;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.dag.bean.enums.NodeCategory;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NodeHandlerRegistryTest {

    private static class DummyHandler implements NodeHandler {
        private final String code;
        DummyHandler(String code) { this.code = code; }
        public String nodeCode() { return code; }
        public NodeResult execute(NodeContext ctx) { return NodeResult.ok(); }
    }

    /** @Resource 字段注入的手工装配：new 后 setField 并触发 @PostConstruct 注册 */
    private static NodeHandlerRegistry registryOf(List<NodeHandler> handlers) {
        NodeHandlerRegistry registry = new NodeHandlerRegistry();
        ReflectionTestUtils.setField(registry, "handlerList", handlers);
        registry.init();
        return registry;
    }

    @Test
    void get_missingCode_throws() {
        NodeHandlerRegistry registry = registryOf(List.of(new DummyHandler("a")));
        assertThrows(ValidationException.class, () -> registry.get("not-exist"));
    }

    @Test
    void duplicateCode_rejected() {
        assertThrows(IllegalStateException.class,
            () -> registryOf(List.of(new DummyHandler("a"), new DummyHandler("a"))));
    }

    @Test
    void get_and_codes() {
        NodeHandlerRegistry registry = registryOf(List.of(new DummyHandler("a"), new DummyHandler("b")));
        assertEquals("a", registry.get("a").nodeCode());
        assertEquals(2, registry.codes().size());
    }

    @Test
    void category_defaultsToBusiness() {
        assertEquals(NodeCategory.BUSINESS, new DummyHandler("a").category());
    }
}
