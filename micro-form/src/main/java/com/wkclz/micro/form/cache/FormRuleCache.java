package com.wkclz.micro.form.cache;

import com.wkclz.micro.form.mapper.MdmFormRuleFieldMapper;
import com.wkclz.micro.form.mapper.MdmFormRuleFieldValidatorMapper;
import com.wkclz.micro.form.mapper.MdmFormRuleMapper;
import com.wkclz.micro.form.bean.dto.MdmFormRuleDto;
import com.wkclz.micro.form.bean.dto.MdmFormRuleFieldDto;
import com.wkclz.micro.form.bean.dto.MdmFormRuleFieldValidatorDto;
import com.wkclz.micro.form.bean.entity.MdmFormRuleField;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class FormRuleCache {

    private static final String CACHE_KEY = "sh:micro:formRule:cache:time";

    private static Long CACHE_TIME = null;
    private static Map<String, MdmFormRuleDto> CACHE_ITEM = null;

    @Resource
    private MdmFormRuleMapper mdmFormRuleMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private MdmFormRuleFieldMapper mdmFormRuleFieldMapper;
    @Resource
    private MdmFormRuleFieldValidatorMapper mdmFormRuleFieldValidatorMapper;

    public void clearCache() {
        long now = System.currentTimeMillis();
        BoundValueOperations<String, String> ops = stringRedisTemplate.boundValueOps(CACHE_KEY);
        ops.set(now + "");
        ops.expire(1, TimeUnit.MINUTES);

        CACHE_TIME = null;
        // 当前实例，立即生效。其他实例，依赖于 redis 标识
        init();
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
        long redisTime = Long.parseLong(changeTime);

        // 本地缓存时间更大，不处理
        if (CACHE_TIME != null && CACHE_TIME - redisTime > 1_000) {
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
    public synchronized MdmFormRuleDto getFormRule(String method, String uri) {
        if (StringUtils.isBlank(method) || StringUtils.isBlank(uri)) {
            return null;
        }
        String key = method + ":" + uri;
        if (CACHE_ITEM == null) {
            init();
        }

        return CACHE_ITEM.get(key);
    }

    private synchronized void init() {
        long l = System.currentTimeMillis();
        if (CACHE_ITEM != null && CACHE_TIME != null && (l - CACHE_TIME < 5_000)) {
            return;
        }

        Map<String, MdmFormRuleDto> tmp = new HashMap<>();
        List<MdmFormRuleDto> rules = mdmFormRuleMapper.get4Cache();
        if (CollectionUtils.isEmpty(rules)) {
            CACHE_ITEM = tmp;
            return;
        }
        List<MdmFormRuleFieldDto> fields = mdmFormRuleFieldMapper.get4Cache();
        Map<String, List<MdmFormRuleFieldDto>> paramMap = fields.stream().collect(Collectors.groupingBy(MdmFormRuleField::getFormRuleCode));
        for (MdmFormRuleDto rule : rules) {
            String key = rule.getApiMethod() + ":" + rule.getApiUri();
            rule.setFields(paramMap.get(rule.getFormRuleCode()));
            tmp.put(key, rule);
        }

        List<MdmFormRuleFieldValidatorDto> validators = mdmFormRuleFieldValidatorMapper.get4Cache();
        Map<String, List<MdmFormRuleFieldValidatorDto>> validatorMap = validators.stream().collect(Collectors.groupingBy(t -> t.getFormRuleCode() + ":" + t.getFieldCode()));

        for (MdmFormRuleFieldDto field : fields) {
            List<MdmFormRuleFieldValidatorDto> vs = validatorMap.get(field.getFormRuleCode() + ":" + field.getFieldCode());
            field.setValidators(vs);
        }

        CACHE_ITEM = tmp;
        CACHE_TIME = l;
        log.info("micro-form: 表单校验规则更新成功 {} 项", rules.size());
    }

}
