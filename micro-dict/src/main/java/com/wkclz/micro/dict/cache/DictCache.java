package com.wkclz.micro.dict.cache;

import com.wkclz.micro.dict.bean.entity.MdmDict;
import com.wkclz.micro.dict.bean.entity.MdmDictItem;
import com.wkclz.micro.dict.mapper.MdmDictItemMapper;
import com.wkclz.micro.dict.mapper.MdmDictMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class DictCache implements MessageListener {

    private static final String DICT_CACHE_CHANNEL = "shrimp:micro:dict:cache:refresh";

    private static volatile long CACHE_TIME = 0;
    private static volatile Map<String, Map<String, String>> CACHE_DICT = null;

    @Autowired
    private MdmDictMapper mdmDictMapper;
    @Autowired
    private MdmDictItemMapper mdmDictItemMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedisMessageListenerContainer redisMessageListenerContainer;

    @PostConstruct
    public void init() {
        redisMessageListenerContainer.addMessageListener(this, ChannelTopic.of(DICT_CACHE_CHANNEL));
        loadCache();
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        log.info("micro-dict: 收到字典缓存刷新通知");
        loadCache();
    }

    public void clearCache() {
        stringRedisTemplate.convertAndSend(DICT_CACHE_CHANNEL, String.valueOf(System.currentTimeMillis()));
        loadCache();
    }

    /**
     * 获取字典 value
     */
    public String get(String dictType, String dictKey) {
        if (StringUtils.isBlank(dictKey)) {
            return null;
        }
        Map<String, String> itemMap = get(dictType);
        if (itemMap == null) {
            return null;
        }
        return itemMap.get(dictKey);
    }

    /**
     * 获取字典 map
     */
    public Map<String, String> get(String dictType) {
        if (StringUtils.isBlank(dictType)) {
            return Collections.emptyMap();
        }
        if (CACHE_DICT == null) {
            loadCache();
        }
        if (CACHE_DICT == null) {
            return Collections.emptyMap();
        }
        return CACHE_DICT.get(dictType);
    }

    private synchronized void loadCache() {
        long now = System.currentTimeMillis();
        if (CACHE_DICT != null && (now - CACHE_TIME < 3_000)) {
            return;
        }

        List<MdmDict> dicts = mdmDictMapper.dicts4Cache();
        Map<String, Map<String, String>> tmp = new HashMap<>();
        if (CollectionUtils.isEmpty(dicts)) {
            CACHE_DICT = tmp;
            CACHE_TIME = now;
            return;
        }

        List<MdmDictItem> allItems = mdmDictItemMapper.dictItems4Cache();
        Map<String, List<MdmDictItem>> itemsByType = allItems.stream()
                .filter(item -> item.getDictType() != null)
                .collect(Collectors.groupingBy(MdmDictItem::getDictType));

        for (MdmDict dict : dicts) {
            if (dict.getDictType() == null) {
                continue;
            }
            List<MdmDictItem> items = itemsByType.get(dict.getDictType());
            Map<String, String> itemMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(items)) {
                for (MdmDictItem item : items) {
                    if (item.getDictValue() != null) {
                        itemMap.put(item.getDictValue(), item.getDictLabel());
                    }
                }
            }
            tmp.put(dict.getDictType(), itemMap);
        }
        CACHE_DICT = tmp;
        CACHE_TIME = now;
        log.info("micro-dict: 字典缓存成功 {} 项", dicts.size());
    }

}
