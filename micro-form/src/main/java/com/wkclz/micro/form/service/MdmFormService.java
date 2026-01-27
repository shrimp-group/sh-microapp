package com.wkclz.micro.form.service;

import com.wkclz.core.base.BaseEntity;
import com.wkclz.core.base.PageData;
import com.wkclz.core.enums.ResultCode;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.form.dao.MdmFormItemMapper;
import com.wkclz.micro.form.dao.MdmFormMapper;
import com.wkclz.micro.form.pojo.dto.MdmFormDto;
import com.wkclz.micro.form.pojo.entity.MdmForm;
import com.wkclz.micro.form.pojo.entity.MdmFormItem;
import com.wkclz.mybatis.helper.PageQuery;
import com.wkclz.mybatis.service.BaseService;
import com.wkclz.redis.helper.RedisIdGenerator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_form (表单) 单表服务类，代码重新生成不覆盖. 只建议完成单表的逻辑，或主表为 mdm_form 的逻辑. 其他逻辑放 custom 中
 */
 
@Service
public class MdmFormService extends BaseService<MdmForm, MdmFormMapper> {

    @Autowired
    private RedisIdGenerator redisIdGenerator;
    @Autowired
    private MdmFormMapper mdmFormMapper;
    @Autowired
    private MdmFormItemMapper mdmFormItemMapper;

    public PageData<MdmFormDto> getFormPage(MdmFormDto dto) {
        return PageQuery.page(dto, mapper::getFormList);
    }

    public MdmFormDto getFormInfo(MdmForm entity) {
        Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        entity = selectById(entity.getId());
        if (entity == null) {
            throw ValidationException.of(ResultCode.RECORD_NOT_EXIST);
        }
        MdmFormDto dto = MdmFormDto.copy(entity);
        List<MdmFormItem> items = getItems(entity.getFormCode());
        dto.setItems(items);
        return dto;
    }

    public MdmForm create(MdmFormDto dto) {
        duplicateCheck(dto);
        if (StringUtils.isBlank(dto.getFormCode())) {
            dto.setFormCode(redisIdGenerator.generateIdWithPrefix("form_"));
        }
        MdmForm entity = new MdmForm();
        MdmFormDto.copy(dto, entity);
        mapper.insert(entity);

        List<MdmFormItem> items = dto.getItems();
        if (CollectionUtils.isEmpty(items)) {
            return entity;
        }
        for (MdmFormItem item : items) {
            item.setItemCode(redisIdGenerator.generateIdWithPrefix("form_item_"));
        }
        mdmFormItemMapper.insertBatch(items);
        return entity;
    }

    public MdmForm update(MdmFormDto dto) {
        duplicateCheck(dto);
        Assert.notNull(dto.getId(), "请求错误！参数[id]不能为空");
        Assert.notNull(dto.getVersion(), "请求错误！参数[version]不能为空");
        MdmForm oldEntity = selectById(dto.getId());
        if (oldEntity == null) {
            throw ValidationException.of(ResultCode.RECORD_NOT_EXIST);
        }
        MdmForm.copyIfNotNull(dto, oldEntity);
        updateByIdSelective(oldEntity);

        // items
        List<MdmFormItem> newItems = dto.getItems();
        List<MdmFormItem> oldItems = getItems(oldEntity.getFormCode());

        // 新增的
        List<MdmFormItem> insertItems = newItems.stream().filter(t -> StringUtils.isBlank(t.getItemCode())).collect(Collectors.toList());

        // 删除的
        List<String> newItemCodes = newItems.stream().map(MdmFormItem::getItemCode).filter(StringUtils::isNotBlank).toList();
        List<Long> deleteItemIds = oldItems.stream().filter(t -> !newItemCodes.contains(t.getItemCode())).map(BaseEntity::getId).collect(Collectors.toList());

        // 修改的
        Map<String, MdmFormItem> newItemMap = newItems.stream().collect(Collectors.toMap(MdmFormItem::getItemCode, Function.identity()));
        List<MdmFormItem> existItems = oldItems.stream().filter(t -> newItemCodes.contains(t.getItemCode())).toList();
        List<MdmFormItem> updateItems = existItems.stream().filter(t -> {
            MdmFormItem newItem = newItemMap.get(t.getItemCode());
            // 判断是否修改
            boolean modify = false;
            if (newItem.getItemGroup() == null || !newItem.getItemGroup().equals(t.getItemGroup())) {
                t.setItemGroup(newItem.getItemGroup());
                modify = true;
            }
            if (newItem.getInputType() == null || !newItem.getInputType().equals(t.getInputType())) {
                t.setInputType(newItem.getInputType());
                modify = true;
            }
            if (newItem.getFieldType() == null || !newItem.getFieldType().equals(t.getFieldType())) {
                t.setFieldType(newItem.getFieldType());
                modify = true;
            }
            if (newItem.getItemName() == null || !newItem.getItemName().equals(t.getItemName())) {
                t.setItemName(newItem.getItemName());
                modify = true;
            }
            if (newItem.getLabel() == null || !newItem.getLabel().equals(t.getLabel())) {
                t.setLabel(newItem.getLabel());
                modify = true;
            }
            if (newItem.getMin() == null || !newItem.getMin().equals(t.getMin())) {
                t.setMin(newItem.getMin());
                modify = true;
            }
            if (newItem.getMax() == null || !newItem.getMax().equals(t.getMax())) {
                t.setMax(newItem.getMax());
                modify = true;
            }
            if (newItem.getMinLength() == null || !newItem.getMinLength().equals(t.getMinLength())) {
                t.setMinLength(newItem.getMinLength());
                modify = true;
            }
            if (newItem.getMaxLength() == null || !newItem.getMaxLength().equals(t.getMaxLength())) {
                t.setMaxLength(newItem.getMaxLength());
                modify = true;
            }
            if (newItem.getPlaceholder() == null || !newItem.getPlaceholder().equals(t.getPlaceholder())) {
                t.setPlaceholder(newItem.getPlaceholder());
                modify = true;
            }
            if (newItem.getRequired() == null || !newItem.getRequired().equals(t.getRequired())) {
                t.setRequired(newItem.getRequired());
                modify = true;
            }
            if (newItem.getDefaultValue() == null || !newItem.getDefaultValue().equals(t.getDefaultValue())) {
                t.setDefaultValue(newItem.getDefaultValue());
                modify = true;
            }
            if (newItem.getRules() == null || !newItem.getRules().equals(t.getRules())) {
                t.setRules(newItem.getRules());
                modify = true;
            }
            if (newItem.getClearable() == null || !newItem.getClearable().equals(t.getClearable())) {
                t.setClearable(newItem.getClearable());
                modify = true;
            }
            if (newItem.getSort() == null || !newItem.getSort().equals(t.getSort())) {
                t.setSort(newItem.getSort());
                modify = true;
            }
            if (newItem.getRemark() == null || !newItem.getRemark().equals(t.getRemark())) {
                t.setRemark(newItem.getRemark());
                modify = true;
            }
            return modify;
        }).collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(insertItems)) {
            for (MdmFormItem item : insertItems) {
                item.setItemCode(redisIdGenerator.generateIdWithPrefix("form_item_"));
            }
            mdmFormItemMapper.insertBatch(insertItems);
        }
        if (!CollectionUtils.isEmpty(updateItems)) {
            for (MdmFormItem updateItem : updateItems) {
                mdmFormItemMapper.updateById(updateItem);
            }
        }
        if (!CollectionUtils.isEmpty(deleteItemIds)) {
            MdmFormItem param = new MdmFormItem();
            param.setIds(deleteItemIds);
            mdmFormItemMapper.deleteByIds(param);
        }
        return oldEntity;
    }

    public Integer customRemove(MdmForm entity) {
        Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        entity = selectById(entity.getId());
        if (entity == null) {
            throw ValidationException.of("数据不存在，或无权限操作!");
        }
        // 移除 items
        List<MdmFormItem> items = getItems(entity.getFormCode());
        List<Long> ids = items.stream().map(BaseEntity::getId).collect(Collectors.toList());
        MdmFormItem param = new MdmFormItem();
        param.setIds(ids);
        mdmFormItemMapper.deleteByIds(param);

        return mapper.deleteById(entity);
    }

    public List<MdmForm> getFormOptions() {
        return mapper.getFormOptions();
    }

    public MdmFormDto getCustomFormDetail(MdmForm entity) {
        Assert.notNull(entity.getFormCode(), "请求错误！参数[formCode]不能为空");
        MdmFormDto dto = mdmFormMapper.getCustomFormInfo(entity.getFormCode());
        if (dto == null) {
            throw ValidationException.of(ResultCode.RECORD_NOT_EXIST);
        }
        List<MdmFormItem> items = mdmFormItemMapper.getCustomFormItemList(entity.getFormCode());
        dto.setItems(items);
        return dto;
    }

    private void duplicateCheck(MdmForm entity) {
        // 唯一条件为空，直接通过
        if (StringUtils.isBlank(entity.getFormCode())) {
            return;
        }
        
        // 唯一条件不为空，请设置唯一条件
        MdmForm param = new MdmForm();
        param.setFormCode(entity.getFormCode());
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


    private List<MdmFormItem> getItems(String formCode) {
        MdmFormItem param = new MdmFormItem();
        param.setFormCode(formCode);
        param.setOrderBy("sort ASC, id ASC");
        return mdmFormItemMapper.selectByEntity(param);
    }

}

