package com.wkclz.micro.pay.cache;

import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.constant.WxPayConstants;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.service.impl.WxPayServiceImpl;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.iam.sdk.helper.SessionHelper;
import com.wkclz.micro.pay.dao.PayWxpayConfigMapper;
import com.wkclz.micro.pay.pojo.entity.PayWxpayConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class WxpayClientCache {

    private static final String ALIPAY_CACHE_KEY = "shrimp:micro:wxpay:cache:time";
    private static Long CACHE_TIME = null;
    private static Map<String, WxPayService> CACHE_WXPAY = new HashMap<>();
    private static Map<String, PayWxpayConfig> CACHE_CONFIG = new HashMap<>();

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private PayWxpayConfigMapper payWxpayConfigMapper;

    public void clearCache() {
        long now = System.currentTimeMillis();
        BoundValueOperations<String, String> ops = stringRedisTemplate.boundValueOps(ALIPAY_CACHE_KEY);
        ops.set(now + "");
        ops.expire(1, TimeUnit.MINUTES);

        CACHE_TIME = null;
    }

    @Scheduled(fixedDelay = 12_000, initialDelay = 32_000)
    public void autoClear() {
        if (CACHE_TIME == null || CACHE_WXPAY == null) {
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

    public synchronized PayWxpayConfig getConfig() {
        String tenantCode = SessionHelper.getTenantCode();
        return getConfig(tenantCode);
    }
    public synchronized PayWxpayConfig getConfig(String tenantCode) {
        if (StringUtils.isBlank(tenantCode)) {
            throw ValidationException.of("tenantCode can not be null");
        }
        if (CACHE_CONFIG.containsKey(tenantCode)) {
            return CACHE_CONFIG.get(tenantCode);
        }
        init(tenantCode);
        PayWxpayConfig config = CACHE_CONFIG.get(tenantCode);
        if (config == null) {
            throw ValidationException.of("alipay config is not exist!");
        }
        return config;
    }
    public synchronized WxPayService getClient() {
        String tenantCode = SessionHelper.getTenantCode();
        return getClient(tenantCode);
    }
    public synchronized WxPayService getClient(String tenantCode) {
        if (StringUtils.isBlank(tenantCode)) {
            throw ValidationException.of("tenantCode can not be null");
        }
        if (CACHE_WXPAY.containsKey(tenantCode)) {
            return CACHE_WXPAY.get(tenantCode);
        }
        init(tenantCode);
        WxPayService client = CACHE_WXPAY.get(tenantCode);
        if (client == null) {
            throw ValidationException.of("wxpay client is not exist!");
        }
        return client;
    }

    private void init(String tenantCode) {
        PayWxpayConfig config = new PayWxpayConfig();
        config.setTenantCode(tenantCode);
        config = payWxpayConfigMapper.selectOneByEntity(config);
        if (config == null) {
            throw ValidationException.of("没有找到微信支付配置：tenantCode: {}", tenantCode);
        }
        check(config);

        WxPayConfig payConfig = new WxPayConfig();
        payConfig.setUseSandboxEnv(false);
        payConfig.setAppId(StringUtils.trimToNull(config.getAppId()));
        payConfig.setMchId(StringUtils.trimToNull(config.getMchId()));
        payConfig.setMchKey(StringUtils.trimToNull(config.getMchV3Key()));
        payConfig.setApiV3Key(StringUtils.trimToNull(config.getMchV3Key()));
        payConfig.setCertSerialNo(config.getMchCertSerialNo());


        // 用数据库存储 pem 的场景【WxJava 只支持 p12 证书，路径或二进制，暂不支持 pem 的任何形式】
        if (StringUtils.isNotBlank(config.getApiclientKey()) && StringUtils.isNotBlank(config.getApiclientCert())) {
            payConfig.setPrivateKeyString(config.getApiclientKey());
            payConfig.setPrivateKeyContent(config.getApiclientKey().getBytes(StandardCharsets.UTF_8));
            payConfig.setPrivateCertString(config.getApiclientCert());
            payConfig.setPrivateCertContent(config.getApiclientCert().getBytes(StandardCharsets.UTF_8));
        }

        payConfig.setNotifyUrl(StringUtils.trimToNull(config.getNotifyUrl()));
        payConfig.setSignType(WxPayConstants.SignType.MD5);

        WxPayService wxPayService = new WxPayServiceImpl();
        wxPayService.setConfig(payConfig);

        CACHE_WXPAY.put(tenantCode, wxPayService);
        CACHE_CONFIG.put(tenantCode, config);
    }


    private static void check(PayWxpayConfig config) {
        if (config == null) {
            throw new NullPointerException("alipay can not be null");
        }
        if (StringUtils.isBlank(config.getAppId())) {
            throw new NullPointerException("appId can not be null");
        }
        if (StringUtils.isBlank(config.getMchId())) {
            throw new NullPointerException("mchId can not be null");
        }
        if (StringUtils.isBlank(config.getMchV3Key())) {
            throw new NullPointerException("mchV3Key can not be null");
        }
        if (StringUtils.isBlank(config.getNotifyUrl())) {
            throw new NullPointerException("appPublicKey can not be null");
        }
        if (StringUtils.isBlank(config.getReturnUrl())) {
            throw new NullPointerException("returnUrl can not be null");
        }
    }


}
