package com.wkclz.micro.dag.api;

import com.wkclz.core.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 收集 Spring 容器内全部 NodeHandler，按 nodeCode 索引
 */
@Slf4j
@Component
public class NodeHandlerRegistry {

    private final Map<String, NodeHandler> handlers = new HashMap<>();

    public NodeHandlerRegistry(List<NodeHandler> handlerList) {
        for (NodeHandler handler : handlerList) {
            NodeHandler prev = handlers.put(handler.nodeCode(), handler);
            if (prev != null) {
                throw new IllegalStateException("重复的 NodeHandler nodeCode: " + handler.nodeCode());
            }
        }
        log.info("micro-dag: 已注册 NodeHandler {} 个: {}", handlers.size(), handlers.keySet());
    }

    public NodeHandler get(String code) {
        NodeHandler handler = handlers.get(code);
        if (handler == null) {
            throw ValidationException.of("handler 未注册: " + code);
        }
        return handler;
    }

    public Set<String> codes() {
        return handlers.keySet();
    }

    public List<NodeHandler> all() {
        return List.copyOf(handlers.values());
    }
}
