package com.wkclz.micro.file.helper;

import com.wkclz.micro.file.mapper.MdmFileBucketMapper;
import com.wkclz.micro.file.bean.entity.MdmFileBucket;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class BucketCache implements MessageListener {

    private static final String BUCKET_CACHE_CHANNEL = "shrimp:micro:bucket:cache:refresh";

    private static volatile Map<String, MdmFileBucket> CACHE_BUCKET = null;
    private static volatile MdmFileBucket CACHE_BUCKET_DEFAULT = null;
    private static volatile long CACHE_TIME = 0;

    @Autowired
    private MdmFileBucketMapper mdmFileBucketMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedisMessageListenerContainer redisMessageListenerContainer;

    @PostConstruct
    public void init() {
        redisMessageListenerContainer.addMessageListener(this, ChannelTopic.of(BUCKET_CACHE_CHANNEL));
        loadCache();
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        log.info("micro-file: 收到 bucket 缓存刷新通知");
        loadCache();
    }

    public void clearCache() {
        stringRedisTemplate.convertAndSend(BUCKET_CACHE_CHANNEL, String.valueOf(System.currentTimeMillis()));
        loadCache();
    }

    public MdmFileBucket get() {
        if (CACHE_BUCKET_DEFAULT == null) {
            loadCache();
        }
        if (CACHE_BUCKET_DEFAULT == null) {
            return null;
        }
        if (CACHE_BUCKET_DEFAULT.getBucket() == null) {
            return null;
        }
        return CACHE_BUCKET_DEFAULT;
    }

    public MdmFileBucket get(String bucket) {
        if (StringUtils.isBlank(bucket)) {
            return null;
        }
        if (CACHE_BUCKET == null) {
            loadCache();
        }
        if (CACHE_BUCKET == null) {
            return null;
        }
        return CACHE_BUCKET.get(bucket);
    }

    private synchronized void loadCache() {
        long now = System.currentTimeMillis();
        if (CACHE_BUCKET != null && (now - CACHE_TIME < 3_000)) {
            return;
        }

        Map<String, MdmFileBucket> tmp = new HashMap<>();
        MdmFileBucket param = new MdmFileBucket();
        List<MdmFileBucket> buckets = mdmFileBucketMapper.selectByEntity(param);
        if (CollectionUtils.isEmpty(buckets)) {
            CACHE_BUCKET = tmp;
            CACHE_BUCKET_DEFAULT = new MdmFileBucket();
            CACHE_TIME = now;
            return;
        }

        MdmFileBucket defaultBucket = null;
        for (MdmFileBucket bucket : buckets) {
            tmp.put(bucket.getBucket(), bucket);
            if (bucket.getDefaultFlag() == 1) {
                defaultBucket = bucket;
            }
        }

        CACHE_BUCKET = tmp;
        CACHE_BUCKET_DEFAULT = defaultBucket != null ? defaultBucket : buckets.get(0);
        CACHE_TIME = now;
        log.info("micro-file: bucket更新成功 {} 项", buckets.size());
    }

}
