package com.wkclz.micro.pay.cache;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConfig;
import com.alipay.api.DefaultAlipayClient;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.iam.sdk.helper.SessionHelper;
import com.wkclz.micro.pay.dao.PayAlipayConfigMapper;
import com.wkclz.micro.pay.pojo.entity.PayAlipayConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class AlipayClientCache {

    private static final String ALIPAY_DEV = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";
    private static final String ALIPAY_PRD = "https://openapi.alipay.com/gateway.do";

    private static final String ALIPAY_CACHE_KEY = "shrimp:micro:alipay:cache:time";
    private static Long CACHE_TIME = null;
    private static Map<String, AlipayClient> CACHE_ALIPAY = new HashMap<>();
    private static Map<String, PayAlipayConfig> CACHE_CONFIG = new HashMap<>();

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private PayAlipayConfigMapper payAlipayConfigMapper;

    public void clearCache() {
        long now = System.currentTimeMillis();
        BoundValueOperations<String, String> ops = stringRedisTemplate.boundValueOps(ALIPAY_CACHE_KEY);
        ops.set(now + "");
        ops.expire(1, TimeUnit.MINUTES);

        CACHE_TIME = null;
    }

    @Scheduled(fixedDelay = 12_000, initialDelay = 30_000)
    public void autoClear() {
        if (CACHE_TIME == null || CACHE_ALIPAY == null) {
            return;
        }
        String changeTime = stringRedisTemplate.boundValueOps(ALIPAY_CACHE_KEY).get();
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

        CACHE_TIME = null;
    }

    public synchronized PayAlipayConfig getConfig() {
        String tenantCode = SessionHelper.getTenantCode();
        return getConfig(tenantCode);
    }
    public synchronized PayAlipayConfig getConfig(String tenantCode) {
        if (StringUtils.isBlank(tenantCode)) {
            throw ValidationException.of("tenantCode can not be null");
        }
        if (CACHE_CONFIG.containsKey(tenantCode)) {
            return CACHE_CONFIG.get(tenantCode);
        }

        init(tenantCode);
        PayAlipayConfig config = CACHE_CONFIG.get(tenantCode);
        if (config == null) {
            throw ValidationException.of("alipay config is not exist!");
        }
        return config;
    }

    public synchronized AlipayClient getClient() {
        String tenantCode = SessionHelper.getTenantCode();
        return getClient(tenantCode);
    }
    public synchronized AlipayClient getClient(String tenantCode) {
        if (StringUtils.isBlank(tenantCode)) {
            throw ValidationException.of("tenantCode can not be null");
        }
        if (CACHE_ALIPAY.containsKey(tenantCode)) {
            return CACHE_ALIPAY.get(tenantCode);
        }

        init(tenantCode);
        AlipayClient client = CACHE_ALIPAY.get(tenantCode);
        if (client == null) {
            throw ValidationException.of("alipay client is not exist!");
        }
        return client;
    }

    private void init(String tenantCode) {
        PayAlipayConfig config = new PayAlipayConfig();
        config.setTenantCode(tenantCode);
        config = payAlipayConfigMapper.selectOneByEntity(config);
        if (config == null) {
            throw ValidationException.of("没有找到支付宝配置：tenantCode: {}", tenantCode);
        }
        check(config);


        AlipayConfig ac = new AlipayConfig();
        //设置网关地址
        ac.setServerUrl(config.getIsProd() == 1 ? ALIPAY_PRD : ALIPAY_DEV);
        //设置应用APPID
        ac.setAppId(config.getAppId());
        //设置应用私钥
        ac.setPrivateKey(config.getMerchantPrivateKey());
        //设置请求格式，固定值json
        ac.setFormat("json");
        //设置字符集
        ac.setCharset(config.getCharset());
        //设置支付宝公钥
        ac.setAlipayPublicKey(config.getAlipayPublicKey());
        //设置签名类型
        ac.setSignType(config.getSignType());
        //构造client
        try {
            AlipayClient client = new DefaultAlipayClient(ac);
            CACHE_ALIPAY.put(tenantCode, client);
            CACHE_CONFIG.put(tenantCode, config);
        } catch (AlipayApiException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }


    private static void check(PayAlipayConfig config) {
        if (config == null) {
            throw new NullPointerException("alipay can not be null");
        }
        if (StringUtils.isBlank(config.getAppId())) {
            throw new NullPointerException("appId can not be null");
        }
        if (StringUtils.isBlank(config.getMerchantPrivateKey())) {
            throw new NullPointerException("merchantPrivateKey can not be null");
        }
        if (StringUtils.isBlank(config.getAlipayPublicKey())) {
            throw new NullPointerException("alipayPublicKey can not be null");
        }
        if (StringUtils.isBlank(config.getAppPublicKey())) {
            throw new NullPointerException("appPublicKey can not be null");
        }
        if (StringUtils.isBlank(config.getNotifyUrl())) {
            throw new NullPointerException("notifyUrl can not be null");
        }
        if (StringUtils.isBlank(config.getReturnUrl())) {
            throw new NullPointerException("returnUrl can not be null");
        }

        // 默认值处理
        if (StringUtils.isBlank(config.getSignType())) {
            config.setSignType("RSA2");
        }
        if (StringUtils.isBlank(config.getCharset())) {
            config.setCharset("UTF-8");
        }
        if (config.getIsProd() == null) {
            config.setIsProd(1);
        }
    }


}
