package com.wkclz.micro.form.service;

import com.wkclz.core.base.PageData;
import com.wkclz.core.enums.ResultCode;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.form.dao.MdmFormRuleItemMapper;
import com.wkclz.micro.form.dao.MdmFormRuleMapper;
import com.wkclz.micro.form.pojo.dto.MdmFormRuleDto;
import com.wkclz.micro.form.pojo.entity.MdmFormRule;
import com.wkclz.micro.form.pojo.entity.MdmFormRuleItem;
import com.wkclz.micro.form.pojo.vo.FormRuleItemVo;
import com.wkclz.mybatis.helper.PageQuery;
import com.wkclz.mybatis.service.BaseService;
import com.wkclz.redis.helper.RedisIdGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_form_rule (表单校验规则) 单表服务类，代码重新生成不覆盖. 只建议完成单表的逻辑，或主表为 mdm_form_rule 的逻辑. 其他逻辑放 custom 中
 */
 
@Service
public class MdmFormRuleService extends BaseService<MdmFormRule, MdmFormRuleMapper> {

    @Resource
    private RedisIdGenerator redisIdGenerator;
    @Resource
    private MdmFormRuleItemMapper mdmFormRuleItemMapper;

    public PageData<MdmFormRuleDto> customPage(MdmFormRuleDto dto) {
        return PageQuery.page(dto, mapper::getFormRuleList);
    }

    public MdmFormRule create(MdmFormRule entity) {
        duplicateCheck(entity);
        if (StringUtils.isBlank(entity.getFormRuleCode())) {
            entity.setFormRuleCode(redisIdGenerator.generateIdWithPrefix("form_rule_"));
        }
        mapper.insert(entity);
        return entity;
    }

    public MdmFormRule update(MdmFormRule entity) {
        duplicateCheck(entity);
        Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        Assert.notNull(entity.getId(), ResultCode.UPDATE_NO_VERSION.getMessage());
        MdmFormRule oldEntity = selectById(entity.getId());
        if (oldEntity == null) {
            throw ValidationException.of(ResultCode.RECORD_NOT_EXIST);
        }
        MdmFormRule.copyIfNotNull(entity, oldEntity);
        updateByIdSelective(oldEntity);
        return oldEntity;
    }

    public Integer customRemove(MdmFormRule entity) {
        Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        entity = selectById(entity.getId());
        if (entity == null) {
            throw ValidationException.of(ResultCode.RECORD_NOT_EXIST);
        }
        // check
        MdmFormRuleItem itemParam = new MdmFormRuleItem();
        itemParam.setFormRuleCode(entity.getFormRuleCode());
        Long count = mdmFormRuleItemMapper.selectCountByEntity(itemParam);
        if (count > 0) {
            throw ValidationException.of("当前表单检查规则存在规则项，请先删除规则项");
        }
        return mapper.deleteById(entity);
    }

    private void duplicateCheck(MdmFormRule entity) {
        // 唯一条件为空，直接通过
        if (StringUtils.isBlank(entity.getFormRuleCode())) {
            return;
        }
        
        // 唯一条件不为空，请设置唯一条件
        MdmFormRule param = new MdmFormRule();
        param.setFormRuleCode(entity.getFormRuleCode());
        // 唯一条件
        param = selectOneByEntity(param);
        if (param == null) {
            return;
        }
        if (param.getId().equals(entity.getId())) {
            return;
        }
        // 查到有值，为新增或 id 不一样场景，为数据重复
        throw ValidationException.of(ResultCode.RECORD_DUPLICATE);
    }



    public Map<String, List<FormRuleItemVo>> getFormRuleInfo(String method, String uri) {
        if (StringUtils.isBlank(method) || StringUtils.isBlank(uri)) {
            return null;
        }

        MdmFormRule rule = new MdmFormRule();
        rule.setApiMethod(method);
        rule.setApiUri(uri);
        List<MdmFormRuleItem> items = mdmFormRuleItemMapper.getFormRule4Check(rule);

        Map<String, List<FormRuleItemVo>> rt = new HashMap<>();

        for (MdmFormRuleItem item : items) {
            List<FormRuleItemVo> rules = rt.computeIfAbsent(item.getFieldName(), k -> new ArrayList<>());
            FormRuleItemVo vo = new FormRuleItemVo();
            vo.setRequired(item.getRequired() == 1);
            vo.setType(item.getFieldType());
            vo.setMin(item.getDataMin());
            vo.setMax(item.getDataMax());
            vo.setMinLength(item.getMinLength());
            vo.setMaxLength(item.getMaxLength());
            vo.setPattern(item.getPattern());
            vo.setValidator(item.getValidator());
            vo.setTrigger(item.getRuleTrigger());
            vo.setMessage(item.getMessage());
            rules.add(vo);
        }
        return rt;
    }

}

