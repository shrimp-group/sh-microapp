package com.wkclz.micro.dict.cache;

import com.wkclz.micro.dict.dao.MdmDictItemMapper;
import com.wkclz.micro.dict.dao.MdmDictMapper;
import com.wkclz.micro.dict.pojo.entity.MdmDict;
import com.wkclz.micro.dict.pojo.entity.MdmDictItem;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class DictCache {

    private static final String DICT_CACHE_KEY = "shrimp:micro:dict:cache:time";

    private static Long CACHE_TIME = null;
    private static Map<String, Map<String, String>> CACHE_DICT = null;

    @Autowired
    private MdmDictMapper mdmDictMapper;
    @Autowired
    private MdmDictItemMapper mdmDictItemMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public void clearCache() {
        long now = System.currentTimeMillis();
        BoundValueOperations<String, String> ops = stringRedisTemplate.boundValueOps(DICT_CACHE_KEY);
        ops.set(now + "");
        ops.expire(1, TimeUnit.MINUTES);

        CACHE_TIME = null;
        init();
    }

    @Scheduled(fixedDelay = 12_000, initialDelay = 30_000)
    public void autoReflash() {
        if (CACHE_TIME == null || CACHE_DICT == null) {
            init();
            return;
        }
        String changeTime = stringRedisTemplate.boundValueOps(DICT_CACHE_KEY).get();
        if (changeTime == null) {
            return;
        }

        long now = System.currentTimeMillis();
        Long redisTime = Long.valueOf(changeTime);

        // 本地缓存时间更大，不处理
        if (CACHE_TIME - redisTime > 1_000) {
            return;
        }

        // 超过1分钟不处理
        if (now - redisTime > 60_000) {
            return;
        }

        // 更新缓存
        init();
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
            init();
        }
        if (CACHE_DICT == null) {
            return Collections.emptyMap();
        }
        return CACHE_DICT.get(dictType);
    }

    private synchronized void init() {
        long l = System.currentTimeMillis();
        if (CACHE_DICT != null && CACHE_TIME != null && (l - CACHE_TIME < 5_000)) {
            return;
        }

        Map<String, Map<String, String>> tmp = new HashMap<>();
        List<MdmDict> dicts = mdmDictMapper.dicts4Cache();
        if (CollectionUtils.isEmpty(dicts)) {
            CACHE_DICT = tmp;
            return;
        }

        List<MdmDictItem> allitems = mdmDictItemMapper.dictItems4Cache();
        for (MdmDict dict : dicts) {
            List<MdmDictItem> items = allitems.stream().filter(t -> t.getDictType().equals(dict.getDictType())).collect(Collectors.toList());
            Map<String, String> itemMap = new HashMap<>();

            if (CollectionUtils.isEmpty(items)) {
                tmp.put(dict.getDictType(), itemMap);
                continue;
            }
            for (MdmDictItem item : items) {
                itemMap.put(item.getDictValue(), item.getDictLabel());
            }
            tmp.put(dict.getDictType(), itemMap);
        }
        CACHE_DICT = tmp;
        CACHE_TIME = l;
        log.info("micro-dict: 字典更新成功 {} 项", dicts.size());
    }

}
