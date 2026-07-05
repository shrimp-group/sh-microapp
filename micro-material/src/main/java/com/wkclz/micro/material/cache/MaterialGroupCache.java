package com.wkclz.micro.material.cache;

import com.wkclz.micro.material.mapper.MdmMaterialGroupMapper;
import com.wkclz.micro.material.bean.entity.MdmMaterialGroup;
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

@Slf4j
@Component
public class MaterialGroupCache implements MessageListener {

    private static final String GROUP_CACHE_CHANNEL = "sh:micro:material:group:cache:refresh";

    private static volatile long CACHE_TIME = 0;
    private static volatile Map<String, List<MdmMaterialGroup>> CACHE_GROUPS = null;

    @Autowired
    private MdmMaterialGroupMapper mdmMaterialGroupMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedisMessageListenerContainer redisMessageListenerContainer;

    @PostConstruct
    public void init() {
        redisMessageListenerContainer.addMessageListener(this, ChannelTopic.of(GROUP_CACHE_CHANNEL));
        loadCache();
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        log.info("micro-material: 收到分组缓存刷新通知");
        loadCache();
    }

    public void clearCache() {
        stringRedisTemplate.convertAndSend(GROUP_CACHE_CHANNEL, String.valueOf(System.currentTimeMillis()));
        loadCache();
    }

    public List<MdmMaterialGroup> get(String tenantCode, String userCode) {
        if (CACHE_GROUPS == null) {
            loadCache();
        }
        if (CACHE_GROUPS == null) {
            return Collections.emptyList();
        }
        String key = tenantCode + ":" + userCode;
        List<MdmMaterialGroup> personal = CACHE_GROUPS.get(key);
        String systemKey = tenantCode + ":SYSTEM";
        List<MdmMaterialGroup> system = CACHE_GROUPS.get(systemKey);
        List<MdmMaterialGroup> result = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(system)) {
            result.addAll(system);
        }
        if (personal != null) {
            result.addAll(personal);
        }
        return result;
    }

    private synchronized void loadCache() {
        long now = System.currentTimeMillis();
        if (CACHE_GROUPS != null && (now - CACHE_TIME < 3_000)) {
            return;
        }

        List<MdmMaterialGroup> groups = mdmMaterialGroupMapper.getGroups4Cache();
        Map<String, List<MdmMaterialGroup>> tmp = new HashMap<>();
        if (CollectionUtils.isEmpty(groups)) {
            CACHE_GROUPS = tmp;
            CACHE_TIME = now;
            return;
        }

        for (MdmMaterialGroup group : groups) {
            String tenantCode = group.getTenantCode();
            String userCode = group.getUserCode();
            String groupType = group.getGroupType();

            String key;
            if ("SYSTEM".equals(groupType)) {
                key = tenantCode + ":SYSTEM";
            } else {
                key = tenantCode + ":" + userCode;
            }

            tmp.computeIfAbsent(key, k -> new ArrayList<>()).add(group);
        }

        CACHE_GROUPS = tmp;
        CACHE_TIME = now;
        log.info("micro-material: 分组缓存成功 {} 项", groups.size());
    }
}
