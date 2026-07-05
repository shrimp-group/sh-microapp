package com.wkclz.micro.report.cache;

import com.wkclz.micro.report.bean.entity.ReportDefinition;
import com.wkclz.micro.report.mapper.ReportDefinitionMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 报表定义缓存
 * 启动时加载所有启用的报表定义到内存，通过 Redis Pub/Sub 感知变更
 */
@Slf4j
@Component
public class ReportCache implements MessageListener {

    private static final String REPORT_CACHE_CHANNEL = "sh:micro:report:cache:refresh";

    private static volatile long CACHE_TIME = 0;
    private static volatile Map<String, ReportDefinition> CACHE_REPORT = new ConcurrentHashMap<>();

    @Autowired
    private ReportDefinitionMapper reportDefinitionMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedisMessageListenerContainer redisMessageListenerContainer;

    @PostConstruct
    public void init() {
        redisMessageListenerContainer.addMessageListener(this, ChannelTopic.of(REPORT_CACHE_CHANNEL));
        loadCache();
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        log.info("micro-report: 收到报表缓存刷新通知");
        loadCache();
    }

    public void clearCache() {
        stringRedisTemplate.convertAndSend(REPORT_CACHE_CHANNEL, String.valueOf(System.currentTimeMillis()));
        loadCache();
    }

    /**
     * 根据 reportCode 获取报表定义
     */
    public ReportDefinition get(String reportCode) {
        if (reportCode == null || reportCode.isEmpty()) {
            return null;
        }
        if (CACHE_REPORT.isEmpty()) {
            loadCache();
        }
        return CACHE_REPORT.get(reportCode);
    }

    /**
     * 获取所有报表定义
     */
    public Collection<ReportDefinition> getAll() {
        if (CACHE_REPORT.isEmpty()) {
            loadCache();
        }
        return CACHE_REPORT.values();
    }

    private synchronized void loadCache() {
        long now = System.currentTimeMillis();
        if (!CACHE_REPORT.isEmpty() && (now - CACHE_TIME < 3_000)) {
            return;
        }

        List<ReportDefinition> definitions = reportDefinitionMapper.definitions4Cache();
        Map<String, ReportDefinition> tmp = new ConcurrentHashMap<>();
        if (CollectionUtils.isNotEmpty(definitions)) {
            for (ReportDefinition def : definitions) {
                if (def.getReportCode() != null) {
                    tmp.put(def.getReportCode(), def);
                }
            }
        }
        CACHE_REPORT = tmp;
        CACHE_TIME = now;
        log.info("micro-report: 报表缓存成功 {} 项", tmp.size());
    }

}
