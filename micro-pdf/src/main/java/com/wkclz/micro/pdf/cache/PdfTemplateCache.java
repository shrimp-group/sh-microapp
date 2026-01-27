package com.wkclz.micro.pdf.cache;

import com.wkclz.micro.pdf.dao.MdmPdfTemplateMapper;
import com.wkclz.micro.pdf.pojo.entity.MdmPdfTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class PdfTemplateCache {

    private static final String CACHE_KEY = "shrimp:micro:pdf:cache:time";

    private static Long CACHE_TIME = null;
    private static Boolean CLEAR_FLAG = false;
    private static Map<String, MdmPdfTemplate> CACHE_ITEM = null;

    @Autowired
    private MdmPdfTemplateMapper mdmPdfTemplateMapper;
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

    @Scheduled(fixedDelay = 12_000, initialDelay = 40_000)
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
     * 获取API value
     */
    public MdmPdfTemplate getPdfTemplate(String templateCode) {
        if (StringUtils.isBlank(templateCode)) {
            return null;
        }
        if (CACHE_ITEM == null) {
            CACHE_ITEM = new HashMap<>();
        }

        MdmPdfTemplate dto = CACHE_ITEM.get(templateCode);
        if (dto != null) {
            return dto.getTemplateCode() == null ? null : dto;
        }
        init();
        dto = CACHE_ITEM.get(templateCode);
        if (dto != null && dto.getTemplateCode() != null) {
            return dto;
        }
        CACHE_ITEM.put(templateCode, new MdmPdfTemplate());
        return null;
    }

    private synchronized void init() {
        long l = System.currentTimeMillis();
        if (CACHE_ITEM != null && CACHE_TIME != null && (l - CACHE_TIME < 5_000)) {
            return;
        }

        Map<String, MdmPdfTemplate> tmp = new HashMap<>();
        List<MdmPdfTemplate> templates = mdmPdfTemplateMapper.get4Cache();
        if (CollectionUtils.isEmpty(templates)) {
            CACHE_ITEM = tmp;
            return;
        }
        for (MdmPdfTemplate template : templates) {
            tmp.put(template.getTemplateCode(), template);
        }
        CACHE_ITEM = tmp;
        CACHE_TIME = l;
        CLEAR_FLAG = true;
        log.info("micro-pdf: PDF模板更新成功 {} 项", templates.size());
    }

}
