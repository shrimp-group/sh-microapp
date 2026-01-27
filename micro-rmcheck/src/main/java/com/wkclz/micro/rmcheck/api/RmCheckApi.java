package com.wkclz.micro.rmcheck.api;

import com.wkclz.core.base.BaseEntity;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.rmcheck.dao.CheckMapper;
import com.wkclz.micro.rmcheck.dao.RmCheckRuleItemMapper;
import com.wkclz.micro.rmcheck.dao.RmCheckRuleMapper;
import com.wkclz.micro.rmcheck.pojo.dto.RmCheckRuleItemDto;
import com.wkclz.micro.rmcheck.pojo.entity.RmCheckRule;
import com.wkclz.micro.rmcheck.pojo.entity.RmCheckRuleItem;
import com.wkclz.tool.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author shrimp
 */
@Slf4j
@Component
public class RmCheckApi {

    @Autowired
    private CheckMapper checkMapper;
    @Autowired
    private RmCheckRuleMapper rmCheckRulemMapper;
    @Autowired
    private RmCheckRuleItemMapper rmCheckRuleItemMapper;


    public <P extends BaseEntity> void check(Class<P> clazz, P data) {
        if (clazz == null || data == null) {
            return;
        }
        String simpleName = clazz.getSimpleName();
        String tableName = StringUtil.camelToUnderline(simpleName);


        RmCheckRule ruleParam = new RmCheckRule();
        ruleParam.setTableName(tableName);
        List<RmCheckRule> rules = rmCheckRulemMapper.getRmCheckRules4Check(ruleParam);
        if (CollectionUtils.isEmpty(rules)) {
            return;
        }

        for (RmCheckRule rule : rules) {
            String columnName = rule.getColumnName();
            if (StringUtils.isBlank(columnName)) {
                continue;
            }

            // 获取需要核验的值
            Object value;
            try {
                String getterName = StringUtil.underlineToCamel(columnName);
                getterName = "get" + StringUtil.firstChatToUpperCase(getterName);
                Method getter = clazz.getDeclaredMethod(getterName);
                value = getter.invoke(data);
            } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                log.error("所需删除校验的字段配置异常，无法获取字段值");
                throw new RuntimeException(e);
            }

            // 使用核验规则项，到其他表去检查内容是否存在
            RmCheckRuleItem itemParam = new RmCheckRuleItem();
            itemParam.setRuleCode(rule.getRuleCode());
            List<RmCheckRuleItem> items = rmCheckRuleItemMapper.getRmCheckRuleItem4Check(itemParam);
            if (CollectionUtils.isEmpty(items)) {
                return;
            }

            for (RmCheckRuleItem item : items) {
                String checkTableName = item.getCheckTableName();
                String checkColumnName = item.getCheckColumnName();

                if (StringUtils.isBlank(checkTableName)) {
                    continue;
                }
                if (StringUtils.isBlank(checkColumnName)) {
                    continue;
                }
                if (tableName.equalsIgnoreCase(checkTableName)) {
                    continue;
                }
                // 检查相关表的数据

                // 查找表，字段，数据是否存在
                RmCheckRuleItemDto dto = new RmCheckRuleItemDto();
                dto.setCheckTableName(checkTableName);
                dto.setCheckColumnName(checkColumnName);
                dto.setCheckValue(value);
                Long l = checkMapper.rmCheck(dto);
                if (l != null) {
                    String noticeMessage = item.getNoticeMessage();
                    if (StringUtils.isBlank(noticeMessage)) {
                        noticeMessage = "您存在删除的内容仍然被依赖，请处理完成后再删除!";
                    }
                    throw ValidationException.of(noticeMessage);
                }
            }
        }


    }




}
