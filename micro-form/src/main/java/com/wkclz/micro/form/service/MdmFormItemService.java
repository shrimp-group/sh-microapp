package com.wkclz.micro.form.service;

import com.wkclz.core.enums.ResultCode;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.form.dao.MdmFormItemMapper;
import com.wkclz.micro.form.pojo.entity.MdmFormItem;
import com.wkclz.mybatis.service.BaseService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_form_item (表单-输入项) 单表服务类，代码重新生成不覆盖. 只建议完成单表的逻辑，或主表为 mdm_form_item 的逻辑. 其他逻辑放 custom 中
 */
 
@Service
public class MdmFormItemService extends BaseService<MdmFormItem, MdmFormItemMapper> {


    public MdmFormItem create(MdmFormItem entity) {
        duplicateCheck(entity);
        mapper.insert(entity);
        return entity;
    }

    public MdmFormItem update(MdmFormItem entity) {
        duplicateCheck(entity);
        Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        Assert.notNull(entity.getId(), ResultCode.UPDATE_NO_VERSION.getMessage());
        MdmFormItem oldEntity = selectById(entity.getId());
        if (oldEntity == null) {
            throw ValidationException.of(ResultCode.RECORD_NOT_EXIST);
        }
        MdmFormItem.copyIfNotNull(entity, oldEntity);
        updateByIdSelective(oldEntity);
        return oldEntity;
    }

    private void duplicateCheck(MdmFormItem entity) {
        // 唯一条件为空，直接通过
        if (StringUtils.isBlank(entity.getFormCode()) || StringUtils.isBlank(entity.getItemCode())) {
            return;
        }

        // 唯一条件不为空，请设置唯一条件
        MdmFormItem param = new MdmFormItem();
        param.setFormCode(entity.getFormCode());
        param.setItemCode(entity.getItemCode());
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

