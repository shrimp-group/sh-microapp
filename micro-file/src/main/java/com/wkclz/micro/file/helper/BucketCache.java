package com.wkclz.micro.file.helper;

import com.wkclz.micro.file.dao.MdmFsBucketMapper;
import com.wkclz.micro.file.pojo.entity.MdmFsBucket;
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
public class BucketCache {

    private static final String BUCKET_CACHE_KEY = "shrimp:micro:bucket:cache:time";

    private static Long CACHE_TIME = null;
    private static Map<String, MdmFsBucket> CACHE_BUCKET = null;
    private static MdmFsBucket CACHE_BUCKET_DEFAULT = null;

    @Autowired
    private MdmFsBucketMapper mdmFsBucketMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public void clearCache() {
        long now = System.currentTimeMillis();
        BoundValueOperations<String, String> ops = stringRedisTemplate.boundValueOps(BUCKET_CACHE_KEY);
        ops.set(now + "");
        ops.expire(1, TimeUnit.MINUTES);

        CACHE_TIME = null;
        init();
    }

    @Scheduled(fixedDelay = 12_000)
    public void autoReflash() {
        if (CACHE_TIME == null || CACHE_BUCKET == null) {
            init();
            return;
        }
        String changeTime = stringRedisTemplate.boundValueOps(BUCKET_CACHE_KEY).get();
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
     * 获取 bucket 配置
     */
    public MdmFsBucket get() {
        if (CACHE_BUCKET_DEFAULT == null) {
            init();
        }
        if (CACHE_BUCKET_DEFAULT == null) {
            return null;
        }
        if (CACHE_BUCKET_DEFAULT.getBucket() == null) {
            return null;
        }
        return CACHE_BUCKET_DEFAULT;
    }

    /**
     * 获取 bucket 配置
     */
    public MdmFsBucket get(String bucket) {
        if (StringUtils.isBlank(bucket)) {
            return null;
        }
        if (CACHE_BUCKET == null) {
            init();
        }
        if (CACHE_BUCKET == null) {
            return null;
        }
        return CACHE_BUCKET.get(bucket);
    }

    private synchronized void init() {
        long l = System.currentTimeMillis();
        if (CACHE_BUCKET != null && CACHE_TIME != null && (l - CACHE_TIME < 5_000)) {
            return;
        }

        Map<String, MdmFsBucket> tmp = new HashMap<>();
        MdmFsBucket param = new MdmFsBucket();
        List<MdmFsBucket> buckets = mdmFsBucketMapper.selectByEntity(param);
        if (CollectionUtils.isEmpty(buckets)) {
            CACHE_BUCKET = tmp;
            CACHE_BUCKET_DEFAULT = new MdmFsBucket();
            return;
        }

        for (MdmFsBucket bucket : buckets) {
            tmp.put(bucket.getBucket(), bucket);
            if (bucket.getDefaultFlag() == 1) {
                CACHE_BUCKET_DEFAULT = bucket;
            }
        }

        CACHE_BUCKET = tmp;
        if (CACHE_BUCKET_DEFAULT == null) {
            CACHE_BUCKET_DEFAULT = buckets.get(0);
        }

        CACHE_TIME = l;
        log.info("micro-fs: bucket更新成功 {} 项", buckets.size());
    }

}
