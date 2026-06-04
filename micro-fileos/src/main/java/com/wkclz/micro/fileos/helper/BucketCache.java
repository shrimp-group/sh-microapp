package com.wkclz.micro.fileos.helper;

import com.wkclz.micro.fileos.bean.entity.MdmFileosBucket;
import com.wkclz.micro.fileos.mapper.MdmFileosBucketMapper;
import jakarta.annotation.PostConstruct;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class BucketCache implements MessageListener {

    private static final String BUCKET_CACHE_CHANNEL = "sh:micro:fileos:bucket:cache:refresh";

    private static volatile Map<String, MdmFileosBucket> CACHE_BUCKET = null;
    private static volatile MdmFileosBucket CACHE_BUCKET_DEFAULT = null;
    private static volatile long CACHE_TIME = 0;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private MdmFileosBucketMapper mdmFileosBucketMapper;
    @Autowired
    private RedisMessageListenerContainer redisMessageListenerContainer;

    @PostConstruct
    public void init() {
        redisMessageListenerContainer.addMessageListener(this, new ChannelTopic(BUCKET_CACHE_CHANNEL));
        loadCache();
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        log.info("micro-fileos: 收到 bucket 缓存刷新通知");
        loadCache();
    }

    public void clearCache() {
        stringRedisTemplate.convertAndSend(BUCKET_CACHE_CHANNEL, String.valueOf(System.currentTimeMillis()));
        loadCache();
    }

    public MdmFileosBucket get() {
        if (CACHE_BUCKET_DEFAULT == null) {
            loadCache();
        }
        if (CACHE_BUCKET_DEFAULT == null) {
            return null;
        }
        if (CACHE_BUCKET_DEFAULT.getBucketName() == null) {
            return null;
        }
        return CACHE_BUCKET_DEFAULT;
    }

    public MdmFileosBucket get(String bucketName) {
        if (StringUtils.isBlank(bucketName)) {
            return null;
        }
        if (CACHE_BUCKET == null) {
            loadCache();
        }
        if (CACHE_BUCKET == null) {
            return null;
        }
        return CACHE_BUCKET.get(bucketName);
    }

    private synchronized void loadCache() {
        long now = System.currentTimeMillis();
        if (CACHE_BUCKET != null && (now - CACHE_TIME < 3_000)) {
            return;
        }

        Map<String, MdmFileosBucket> tmp = new HashMap<>();
        MdmFileosBucket param = new MdmFileosBucket();
        List<MdmFileosBucket> buckets = mdmFileosBucketMapper.selectByEntity(param);
        if (CollectionUtils.isEmpty(buckets)) {
            CACHE_BUCKET = tmp;
            CACHE_BUCKET_DEFAULT = new MdmFileosBucket();
            CACHE_TIME = now;
            return;
        }

        MdmFileosBucket defaultBucket = null;
        for (MdmFileosBucket bucket : buckets) {
            tmp.put(bucket.getBucketName(), bucket);
            if (bucket.getDefaultFlag() != null && bucket.getDefaultFlag() == 1) {
                defaultBucket = bucket;
            }
        }

        CACHE_BUCKET = tmp;
        CACHE_BUCKET_DEFAULT = defaultBucket != null ? defaultBucket : buckets.get(0);
        CACHE_TIME = now;
        log.info("micro-fileos: bucket更新成功 {} 项", buckets.size());
    }

}
