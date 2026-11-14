package com.wkclz.micro.dag.cache;

import com.wkclz.micro.dag.config.DagProperties;
import com.wkclz.micro.dag.worker.DagWorkerExecutor;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

/**
 * 中断广播监听：收到后检查本机是否持有该节点任务，持有则 cancel 工作任务
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "dag", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DagInterruptListener implements MessageListener {

    @Autowired
    private DagWorkerExecutor worker;
    @Autowired
    private RedisMessageListenerContainer redisMessageListenerContainer;
    @Autowired
    private DagProperties properties;

    @PostConstruct
    public void init() {
        if ("db".equalsIgnoreCase(properties.getChannel())) {
            log.info("micro-dag: dag.channel=db，中断广播监听未启用");
            return;
        }
        redisMessageListenerContainer.addMessageListener(this, ChannelTopic.of(DagChannel.INTERRUPT));
        log.info("micro-dag: 中断广播监听已注册");
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        // payload = instanceCode:nodeCode（由 DagRedisPublisher.publishInterrupt 广播）
        String payload = new String(message.getBody());
        String[] parts = payload.split(":", 2);
        if (parts.length != 2) {
            log.error("micro-dag: 中断广播 payload 非法: {}", payload);
            return;
        }
        String instanceCode = parts[0];
        String nodeCode = parts[1];
        log.info("micro-dag: 收到中断广播 instanceCode={} nodeCode={}", instanceCode, nodeCode);
        if (worker.isRunningHere(instanceCode, nodeCode)) {
            worker.interrupt(instanceCode, nodeCode);
        }
    }
}
