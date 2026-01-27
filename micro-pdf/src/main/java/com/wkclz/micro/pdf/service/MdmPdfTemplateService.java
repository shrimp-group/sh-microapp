package com.wkclz.micro.pdf.service;

import com.wkclz.core.base.PageData;
import com.wkclz.core.enums.ResultCode;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.pdf.dao.MdmPdfTemplateMapper;
import com.wkclz.micro.pdf.pojo.entity.MdmPdfTemplate;
import com.wkclz.mybatis.helper.PageQuery;
import com.wkclz.mybatis.service.BaseService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_pdf_template (PDF模板) 单表服务类，代码重新生成不覆盖. 只建议完成单表的逻辑，或主表为 mdm_pdf_template 的逻辑. 其他逻辑放 custom 中
 */
 
@Service
public class MdmPdfTemplateService extends BaseService<MdmPdfTemplate, MdmPdfTemplateMapper> {

    public PageData<MdmPdfTemplate> getPdfTemplatePage(MdmPdfTemplate entity) {
        return PageQuery.page(entity, mapper::getPdfTemplateList);
    }

    public MdmPdfTemplate create(MdmPdfTemplate entity) {
        duplicateCheck(entity);
        mapper.insert(entity);
        return entity;
    }

    public MdmPdfTemplate update(MdmPdfTemplate entity) {
        duplicateCheck(entity);
        Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        Assert.notNull(entity.getId(), ResultCode.UPDATE_NO_VERSION.getMessage());
        MdmPdfTemplate oldEntity = selectById(entity.getId());
        if (oldEntity == null) {
            throw ValidationException.of(ResultCode.RECORD_NOT_EXIST);
        }
        MdmPdfTemplate.copyIfNotNull(entity, oldEntity);
        updateByIdSelective(oldEntity);
        return oldEntity;
    }

    private void duplicateCheck(MdmPdfTemplate entity) {
        // 唯一条件为空，直接通过
        if (StringUtils.isBlank(entity.getTemplateCode())) {
            return;
        }
        
        // 唯一条件不为空，请设置唯一条件
        MdmPdfTemplate param = new MdmPdfTemplate();
        // 唯一条件
        param.setTemplateCode(entity.getTemplateCode());
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

