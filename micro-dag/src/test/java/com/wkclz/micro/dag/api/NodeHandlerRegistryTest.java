package com.wkclz.micro.dag.api;

import com.wkclz.core.exception.ValidationException;
import org.junit.jupiter.api.Test;

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

    @Test
    void get_missingCode_throws() {
        NodeHandlerRegistry registry = new NodeHandlerRegistry(List.of(new DummyHandler("a")));
        assertThrows(ValidationException.class, () -> registry.get("not-exist"));
    }

    @Test
    void duplicateCode_rejected() {
        assertThrows(IllegalStateException.class,
            () -> new NodeHandlerRegistry(List.of(new DummyHandler("a"), new DummyHandler("a"))));
    }

    @Test
    void get_and_codes() {
        NodeHandlerRegistry registry = new NodeHandlerRegistry(List.of(new DummyHandler("a"), new DummyHandler("b")));
        assertEquals("a", registry.get("a").nodeCode());
        assertEquals(2, registry.codes().size());
    }
}
