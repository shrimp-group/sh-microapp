package com.wkclz.micro.dag.cache;

import com.wkclz.micro.dag.helper.DagKeys;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DagRedisPublisher {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /** 广播中断请求到全部实例 */
    public void publishInterrupt(String instanceCode, String nodeCode) {
        log.info("micro-dag: 广播中断 instanceCode={} nodeCode={}", instanceCode, nodeCode);
        stringRedisTemplate.convertAndSend(DagChannel.INTERRUPT, DagKeys.instanceNodeKey(instanceCode, nodeCode));
    }

    /** 广播调度唤醒到全部实例 */
    public void wakeUp() {
        stringRedisTemplate.convertAndSend(DagChannel.WAKEUP, String.valueOf(System.currentTimeMillis()));
    }
}
