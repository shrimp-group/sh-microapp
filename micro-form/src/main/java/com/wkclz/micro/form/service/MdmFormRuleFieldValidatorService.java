package com.wkclz.micro.form.service;

import com.wkclz.core.base.DbColumnEntity;
import com.wkclz.micro.form.cache.FormRuleCache;
import com.wkclz.micro.form.mapper.MdmFormRuleFieldMapper;
import com.wkclz.micro.form.mapper.MdmFormRuleFieldValidatorMapper;
import com.wkclz.micro.form.bean.dto.MdmFormRuleFieldValidatorDto;
import com.wkclz.micro.form.bean.entity.MdmFormRule;
import com.wkclz.micro.form.bean.entity.MdmFormRuleField;
import com.wkclz.micro.form.bean.entity.MdmFormRuleFieldValidator;
import com.wkclz.mybatis.service.BaseService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Description Create by sh-generator
 * @author shrimp
 * @table mdm_form_rule_field_validator (表单校验规则-验证器) 单表服务类，代码重新生成不覆盖. 只建议完成单表的逻辑，或主表为 mdm_form_rule_field_validator 的逻辑. 其他逻辑放 custom 中
 */
 
@Service
public class MdmFormRuleFieldValidatorService extends BaseService<MdmFormRuleFieldValidator, MdmFormRuleFieldValidatorMapper> {


    @Autowired
    private FormRuleCache formRuleCache;
    @Autowired
    private MdmFormRuleFieldMapper mdmFormRuleFieldMapper;


    public List<MdmFormRuleFieldValidatorDto> getFormRuleFieldValidatorList(String formRuleCode) {
        return mapper.getFormRuleFieldValidatorList(formRuleCode);
    }
    public List<MdmFormRuleFieldValidatorDto> getFormRuleFieldValidatorList4Check(String method, String uri) {
        MdmFormRule rule = new MdmFormRule();
        rule.setApiMethod(method);
        rule.setApiUri(uri);
        return mapper.getFormRuleFieldValidatorList4Check(rule);
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer saveBatch(List<MdmFormRuleFieldValidatorDto> validatorDtos) {

        List<MdmFormRuleField> fields = fetchFields(validatorDtos);
        List<MdmFormRuleFieldValidator> validators = fetchValidators(validatorDtos);

        int rt = 0;
        rt += saveFields(fields);
        rt += saveFieldValidators(validators);
        if (rt > 0) {
            formRuleCache.clearCache();
        }
        return rt;
    }


    private static List<MdmFormRuleField> fetchFields(List<MdmFormRuleFieldValidatorDto> validatorDtos) {
        List<MdmFormRuleField> fields = new ArrayList<>();
        Set<String> set = new HashSet<>();
        for (MdmFormRuleFieldValidatorDto dto : validatorDtos) {
            String fieldCode = dto.getFieldCode();
            if (set.contains(fieldCode)) {
                continue;
            }
            MdmFormRuleField field = new MdmFormRuleField();
            field.setFormRuleCode(dto.getFormRuleCode());
            field.setFieldCode(dto.getFieldCode());
            field.setFieldName(dto.getFieldName());
            fields.add(field);
            set.add(fieldCode);
        }
        return fields;
    }

    private static List<MdmFormRuleFieldValidator> fetchValidators(List<MdmFormRuleFieldValidatorDto> validatorDtos) {
        List<MdmFormRuleFieldValidator> validators = new ArrayList<>();
        Set<String> set = new HashSet<>();
        for (MdmFormRuleFieldValidatorDto dto : validatorDtos) {
            MdmFormRuleFieldValidator validator = new MdmFormRuleFieldValidator();
            String hashKey = dto.getFormRuleCode() + ":" + dto.getFieldCode() + ":" + dto.getValidatorType();
            if (set.contains(hashKey)) {
                throw new RuntimeException("表单校验规则-验证器重复:" + hashKey);
            }
            validator.setFormRuleCode(dto.getFormRuleCode());
            validator.setFieldCode(dto.getFieldCode());
            validator.setValidatorType(dto.getValidatorType());
            validator.setValidatorPattern(dto.getValidatorPattern());
            validator.setValidatorFunction(dto.getValidatorFunction());
            validator.setTemplateCode(dto.getTemplateCode());
            validator.setMsgTemplate(dto.getMsgTemplate());
            validators.add(validator);
            set.add(hashKey);
        }
        return validators;
    }


    private Integer saveFields(List<MdmFormRuleField> fields) {
        if (CollectionUtils.isEmpty( fields)) {
            return 0;
        }

        String formRuleCode = fields.get(0).getFormRuleCode();
        MdmFormRuleField field = new MdmFormRuleField();
        field.setFormRuleCode(formRuleCode);
        List<MdmFormRuleField> savedFields = mdmFormRuleFieldMapper.selectByEntity(field);

        // 新增的 field
        List<String> oldFields = savedFields.stream().map(MdmFormRuleField::getFieldCode).toList();
        List<MdmFormRuleField> inserts = fields.stream().filter(t -> !oldFields.contains(t.getFieldCode())).toList();

        // 删除的 field
        List<String> newFields = fields.stream().map(MdmFormRuleField::getFieldCode).toList();
        List<MdmFormRuleField> deletes = savedFields.stream().filter(t -> !newFields.contains(t.getFieldCode())).toList();

        // 修改的 field
        List<MdmFormRuleField> updates = new ArrayList<>();
        for (MdmFormRuleField f : fields) {
            for (MdmFormRuleField s : savedFields) {
                if (f.getFieldCode().equals(s.getFieldCode())) {
                    boolean modify = false;

                    if (!Objects.equals(f.getSort(), s.getSort())) {
                        modify = true;
                        s.setSort(f.getSort());
                    }
                    if (!Objects.equals(f.getFieldName(), s.getFieldName())) {
                        modify = true;
                        s.setFieldName(f.getFieldName());
                    }
                    if (modify) {
                        updates.add(s);
                    }
                }
            }
        }

        int rt = 0;
        if (CollectionUtils.isNotEmpty(inserts)) {
            rt += mdmFormRuleFieldMapper.insertBatch(inserts);
        }
        if (CollectionUtils.isNotEmpty(deletes)) {
            List<Long> ids = deletes.stream().map(DbColumnEntity::getId).toList();
            rt += mdmFormRuleFieldMapper.deleteByIds(ids);
        }
        if (CollectionUtils.isNotEmpty(updates)) {
            for (MdmFormRuleField update : updates) {
                rt += mdmFormRuleFieldMapper.updateByIdSelective(update);
            }
        }
        return rt;
    }


    private Integer saveFieldValidators(List<MdmFormRuleFieldValidator> validators) {
        if (CollectionUtils.isEmpty( validators)) {
            return 0;
        }

        String formRuleCode = validators.get(0).getFormRuleCode();
        MdmFormRuleFieldValidator validator = new MdmFormRuleFieldValidator();
        validator.setFormRuleCode(formRuleCode);
        List<MdmFormRuleFieldValidator> savedFields = mapper.selectByEntity(validator);

        // 新增的 field
        List<String> oldValidators = savedFields.stream().map(t->t.getFieldCode() + ":" + t.getValidatorType()).toList();
        List<MdmFormRuleFieldValidator> inserts = savedFields.stream().filter(t -> !oldValidators.contains(t.getFieldCode() + ":" + t.getValidatorType())).toList();

        // 删除的 field
        List<String> newoldValidators = validators.stream().map(t->t.getFieldCode() + ":" + t.getValidatorType()).toList();
        List<MdmFormRuleFieldValidator> deletes = savedFields.stream().filter(t -> !newoldValidators.contains(t.getFieldCode() + ":" + t.getValidatorType())).toList();

        // 修改的 field
        List<MdmFormRuleFieldValidator> updates = new ArrayList<>();
        for (MdmFormRuleFieldValidator v : validators) {
            for (MdmFormRuleFieldValidator s : savedFields) {
                String key1 = v.getFieldCode() + ":" + v.getValidatorType();
                String key2 = s.getFieldCode() + ":" + s.getValidatorType();
                if (key1.equals(key2)) {
                    boolean modify = false;

                    if (!Objects.equals(v.getSort(), s.getSort())) {
                        modify = true;
                        s.setSort(v.getSort());
                    }
                    if (!Objects.equals(v.getValidatorPattern(), s.getValidatorPattern())) {
                        modify = true;
                        s.setValidatorPattern(v.getValidatorPattern());
                    }
                    if (!Objects.equals(v.getValidatorFunction(), s.getValidatorFunction())) {
                        modify = true;
                        s.setValidatorFunction(v.getValidatorFunction());
                    }

                    if (!Objects.equals(v.getTemplateCode(), s.getTemplateCode())) {
                        modify = true;
                        s.setTemplateCode(v.getTemplateCode());
                    }
                    if (!Objects.equals(v.getMsgTemplate(), s.getMsgTemplate())) {
                        modify = true;
                        s.setMsgTemplate(v.getMsgTemplate());
                    }
                    if (modify) {
                        updates.add(s);
                    }
                }
            }
        }

        int rt = 0;
        if (CollectionUtils.isNotEmpty(inserts)) {
            rt += mapper.insertBatch(inserts);
        }
        if (CollectionUtils.isNotEmpty(deletes)) {
            List<Long> ids = deletes.stream().map(DbColumnEntity::getId).toList();
            rt += mapper.deleteByIds(ids);
        }
        if (CollectionUtils.isNotEmpty(updates)) {
            for (MdmFormRuleFieldValidator update : updates) {
                rt += mapper.updateByIdSelective(update);
            }
        }
        return rt;
    }
}

