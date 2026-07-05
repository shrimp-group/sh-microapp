package com.wkclz.micro.form.service;

import com.wkclz.core.base.DbColumnEntity;
import com.wkclz.core.base.PageData;
import com.wkclz.core.enums.ResultCode;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.form.mapper.MdmFormRuleFieldMapper;
import com.wkclz.micro.form.mapper.MdmFormRuleFieldValidatorMapper;
import com.wkclz.micro.form.mapper.MdmFormRuleMapper;
import com.wkclz.micro.form.bean.dto.MdmFormRuleDto;
import com.wkclz.micro.form.bean.entity.MdmFormRule;
import com.wkclz.micro.form.bean.entity.MdmFormRuleField;
import com.wkclz.micro.form.bean.entity.MdmFormRuleFieldValidator;
import com.wkclz.mybatis.helper.PageQuery;
import com.wkclz.mybatis.service.BaseService;
import com.wkclz.redis.helper.RedisIdGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


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
    private MdmFormRuleFieldMapper mdmFormRuleFieldMapper;
    @Resource
    private MdmFormRuleFieldValidatorMapper mdmFormRuleFieldValidatorMapper;

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
        MdmFormRule oldEntity = selectById(entity.getId());
        if (oldEntity == null) {
            throw ValidationException.of(ResultCode.RECORD_NOT_EXIST);
        }
        MdmFormRule.copyIfNotNull(entity, oldEntity);
        updateByIdSelective(oldEntity);
        return oldEntity;
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer customRemove(MdmFormRule entity) {
        entity = selectById(entity.getId());
        if (entity == null) {
            throw ValidationException.of(ResultCode.RECORD_NOT_EXIST);
        }
        // check
        MdmFormRuleField field = new MdmFormRuleField();
        field.setFormRuleCode(entity.getFormRuleCode());
        List<MdmFormRuleField> fields = mdmFormRuleFieldMapper.selectByEntity(field);
        if (CollectionUtils.isNotEmpty(fields)) {
            List<Long> ids = fields.stream().map(DbColumnEntity::getId).toList();
            mdmFormRuleFieldMapper.deleteByIds(ids);
        }
        // check
        MdmFormRuleFieldValidator fieldValidator = new MdmFormRuleFieldValidator();
        fieldValidator.setFormRuleCode(entity.getFormRuleCode());
        List<MdmFormRuleFieldValidator> fieldValidators = mdmFormRuleFieldValidatorMapper.selectByEntity(fieldValidator);
        if (CollectionUtils.isNotEmpty(fieldValidators)) {
            List<Long> ids = fieldValidators.stream().map(DbColumnEntity::getId).toList();
            mdmFormRuleFieldValidatorMapper.deleteByIds(ids);
        }
        return mapper.deleteById(entity.getId());
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


}

