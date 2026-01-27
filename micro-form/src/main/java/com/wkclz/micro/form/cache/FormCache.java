package com.wkclz.micro.form.cache;

import com.wkclz.micro.form.dao.MdmFormItemMapper;
import com.wkclz.micro.form.dao.MdmFormMapper;
import com.wkclz.micro.form.pojo.dto.MdmFormDto;
import com.wkclz.micro.form.pojo.entity.MdmFormItem;
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
import java.util.stream.Collectors;

@Slf4j
@Component
public class FormCache {

    private static final String CACHE_KEY = "shrimp:micro:form:cache:time";

    private static Long CACHE_TIME = null;
    private static Map<String, MdmFormDto> CACHE_FORM = null;

    @Autowired
    private MdmFormMapper mdmFormMapper;
    @Autowired
    private MdmFormItemMapper mdmFormItemMapper;
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

    @Scheduled(fixedDelay = 12_000, initialDelay = 32_000)
    public void autoReflash() {
        if (CACHE_TIME == null || CACHE_FORM == null) {
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
     * 获取表单 value
     */
    public MdmFormDto getForm(String formCode) {
        if (StringUtils.isBlank(formCode)) {
            return null;
        }
        if (CACHE_FORM == null) {
            init();
        }
        return CACHE_FORM.get(formCode);
    }

    private synchronized void init() {
        long l = System.currentTimeMillis();
        if (CACHE_FORM != null && CACHE_TIME != null && (l - CACHE_TIME < 5_000)) {
            return;
        }

        Map<String, MdmFormDto> tmp = new HashMap<>();

        List<MdmFormDto> forms = mdmFormMapper.getForm4Cache();
        if (CollectionUtils.isEmpty(forms)) {
            CACHE_FORM = tmp;
            CACHE_TIME = l;
            log.info("micro-form: 动态表单则更新成功 {} 项", forms.size());
            return;
        }

        List<MdmFormItem> formItem = mdmFormItemMapper.getFormItem4Cache();

        Map<String, List<MdmFormItem>> itemMap = formItem.stream().collect(Collectors.groupingBy(MdmFormItem::getFormCode));
        for (MdmFormDto form : forms) {
            form.setItems(itemMap.get(form.getFormCode()));
        }

        for (MdmFormDto form : forms) {
            tmp.put(form.getFormCode(), form);
        }
        CACHE_FORM = tmp;
        CACHE_TIME = l;
        log.info("micro-form: 动态表单则更新成功 {} 项", forms.size());
    }

}
