package com.wkclz.micro.dag.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DagRedisPublisher {

    private final StringRedisTemplate stringRedisTemplate;

    public DagRedisPublisher(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /** 广播中断请求到全部实例 */
    public void publishInterrupt(String instanceCode, String nodeCode) {
        log.info("micro-dag: 广播中断 instanceCode={} nodeCode={}", instanceCode, nodeCode);
        stringRedisTemplate.convertAndSend(DagChannel.INTERRUPT, instanceCode + ":" + nodeCode);
    }

    /** 广播调度唤醒到全部实例 */
    public void wakeUp() {
        stringRedisTemplate.convertAndSend(DagChannel.WAKEUP, String.valueOf(System.currentTimeMillis()));
    }
}
