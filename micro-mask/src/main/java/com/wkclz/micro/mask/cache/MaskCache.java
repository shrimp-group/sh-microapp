package com.wkclz.micro.mask.cache;

import com.wkclz.micro.mask.dao.MdmMaskRuleMapper;
import com.wkclz.micro.mask.pojo.entity.MdmMaskRule;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
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

@Slf4j
@Component
public class MaskCache {

    private static final String CACHE_KEY = "shrimp:micro:mask:cache:time";

    private static Long CACHE_TIME = null;
    private static Map<String, MdmMaskRule> CACHE_ITEM = null;
    private static Boolean CLEAR_FLAG = false;

    @Autowired
    private MdmMaskRuleMapper mdmMaskRuleMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public void clearCache() {
        long now = System.currentTimeMillis();
        BoundValueOperations<String, String> ops = stringRedisTemplate.boundValueOps(CACHE_KEY);
        ops.set(now + "");
        ops.expire(1, TimeUnit.MINUTES);

        CACHE_TIME = null;
        init();
    }

    // 读取缓存更新状态。
    public static boolean getClearFlag() {
        if (CLEAR_FLAG) {
            CLEAR_FLAG = false;
            return true;
        }
        return false;
    }

    @Scheduled(fixedDelay = 12_000, initialDelay = 32_000)
    public void autoReflash() {
        if (CACHE_TIME == null || CACHE_ITEM == null) {
            init();
            return;
        }
        String changeTime = stringRedisTemplate.boundValueOps(CACHE_KEY).get();
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
     * 获取规则 value
     */
    public List<MdmMaskRule> getMasks() {
        if (CACHE_ITEM == null) {
            init();
        }
        return CACHE_ITEM.values().stream().toList();
    }

    private synchronized void init() {
        long l = System.currentTimeMillis();
        if (CACHE_ITEM != null && CACHE_TIME != null && (l - CACHE_TIME < 5_000)) {
            return;
        }

        Map<String, MdmMaskRule> tmp = new HashMap<>();
        List<MdmMaskRule> rules = mdmMaskRuleMapper.rules4Cache();
        if (CollectionUtils.isEmpty(rules)) {
            CACHE_ITEM = tmp;
            return;
        }

        for (MdmMaskRule rule : rules) {
            tmp.put(rule.getMaskRuleCode(), rule);
        }
        CACHE_ITEM = tmp;
        CACHE_TIME = l;
        CLEAR_FLAG = true;
        log.info("micro-mask: 脱敏规则更新成功 {} 项", rules.size());
    }

}
