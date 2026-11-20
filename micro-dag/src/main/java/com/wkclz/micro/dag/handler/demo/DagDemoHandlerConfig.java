package com.wkclz.micro.dag.handler.demo;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * demo handler 条件注册：dag.demo-enabled=true 时生效，默认关闭不注册
 * 已注册：log-print/delay/transform/http-call/script-exec/demo-fail
 */
@Configuration
@ConditionalOnProperty(prefix = "dag", name = "demo-enabled", havingValue = "true")
public class DagDemoHandlerConfig {

    @Bean
    public LogPrintHandler logPrintHandler() {
        return new LogPrintHandler();
    }

    @Bean
    public DelayHandler delayHandler() {
        return new DelayHandler();
    }

    @Bean
    public TransformHandler transformHandler() {
        return new TransformHandler();
    }

    @Bean
    public HttpCallHandler httpCallHandler() {
        return new HttpCallHandler();
    }

    @Bean
    public DemoFailHandler demoFailHandler() {
        return new DemoFailHandler();
    }

    /** GraalJS 在 classpath 时才注册 script handler（依赖为 optional） */
    @Configuration
    @ConditionalOnClass(org.graalvm.polyglot.Context.class)
    static class ScriptHandlerConfig {

        @Bean
        public ScriptExecHandler scriptExecHandler() {
            return new ScriptExecHandler();
        }
    }
}
