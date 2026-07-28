package com.wkclz.micro.flowable;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * micro-flowable 自动配置。
 *
 * <p>本模块作为 sh-flowable-server 的对接壳：
 * <ul>
 *   <li>sh-flowable-client 自带 {@code FlowableClientAutoConfig}（通过其 META-INF/spring imports 自动注册），
 *       提供 ProcessDefinitionClient / ProcessDeployClient / ProcessInstanceClient / TaskClient / HistoryClient 等 HttpExchange 客户端；</li>
 *   <li>本类仅负责扫描 micro-flowable 自身包下的 Bean。</li>
 * </ul>
 *
 * <p>待规划内部功能、引入 Mapper 时，追加：
 * <pre>{@code
 * @MapperScan({"com.wkclz.micro.flowable.mapper"})
 * }</pre>
 * 并在 {@code com.wkclz.micro.flowable.mapper} 下创建 Mapper 接口，避免空包告警。
 */
@Configuration
@ComponentScan(basePackages = {"com.wkclz.micro.flowable"})
public class FlowableAutoConfig {
}
