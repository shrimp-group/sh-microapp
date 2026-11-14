package com.wkclz.micro.dag.cache;

import com.wkclz.micro.dag.config.DagProperties;
import com.wkclz.micro.dag.scheduler.DagScheduler;
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
 * 调度唤醒监听：跨实例节点完成事件 → 本机立即执行一轮调度（DB 轮询仍兜底）
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "dag", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DagWakeupListener implements MessageListener {

    @Autowired
    private DagScheduler scheduler;
    @Autowired
    private RedisMessageListenerContainer redisMessageListenerContainer;
    @Autowired
    private DagProperties properties;

    @PostConstruct
    public void init() {
        if ("db".equalsIgnoreCase(properties.getChannel())) {
            log.info("micro-dag: dag.channel=db，唤醒广播监听未启用");
            return;
        }
        redisMessageListenerContainer.addMessageListener(this, ChannelTopic.of(DagChannel.WAKEUP));
        log.info("micro-dag: 唤醒广播监听已注册");
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        scheduler.wakeUp();
    }
}
