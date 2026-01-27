package com.wkclz.micro.pdf.pojo.entity;

import com.wkclz.core.annotation.Desc;
import com.wkclz.core.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;




/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_pdf_template (PDF模板) 重新生成代码会覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class MdmPdfTemplate extends BaseEntity {

    /**
     * 模板编码
     */
    @Desc("模板编码")
    private String templateCode;

    /**
     * 模板名称
     */
    @Desc("模板名称")
    private String templateName;

    /**
     * 模板内容
     */
    @Desc("模板内容")
    private String templateContext;

    /**
     * 模拟数据
     */
    @Desc("模拟数据")
    private String mockData;


    public static MdmPdfTemplate copy(MdmPdfTemplate source, MdmPdfTemplate target) {
        if (target == null ) { target = new MdmPdfTemplate();}
        if (source == null) { return target; }
        target.setId(source.getId());
        target.setTemplateCode(source.getTemplateCode());
        target.setTemplateName(source.getTemplateName());
        target.setTemplateContext(source.getTemplateContext());
        target.setMockData(source.getMockData());
        target.setSort(source.getSort());
        target.setCreateTime(source.getCreateTime());
        target.setCreateBy(source.getCreateBy());
        target.setUpdateTime(source.getUpdateTime());
        target.setUpdateBy(source.getUpdateBy());
        target.setRemark(source.getRemark());
        target.setVersion(source.getVersion());
        return target;
    }

    public static MdmPdfTemplate copyIfNotNull(MdmPdfTemplate source, MdmPdfTemplate target) {
        if (target == null ) { target = new MdmPdfTemplate();}
        if (source == null) { return target; }
        if (source.getId() != null) { target.setId(source.getId()); }
        if (source.getTemplateCode() != null) { target.setTemplateCode(source.getTemplateCode()); }
        if (source.getTemplateName() != null) { target.setTemplateName(source.getTemplateName()); }
        if (source.getTemplateContext() != null) { target.setTemplateContext(source.getTemplateContext()); }
        if (source.getMockData() != null) { target.setMockData(source.getMockData()); }
        if (source.getSort() != null) { target.setSort(source.getSort()); }
        if (source.getCreateTime() != null) { target.setCreateTime(source.getCreateTime()); }
        if (source.getCreateBy() != null) { target.setCreateBy(source.getCreateBy()); }
        if (source.getUpdateTime() != null) { target.setUpdateTime(source.getUpdateTime()); }
        if (source.getUpdateBy() != null) { target.setUpdateBy(source.getUpdateBy()); }
        if (source.getRemark() != null) { target.setRemark(source.getRemark()); }
        if (source.getVersion() != null) { target.setVersion(source.getVersion()); }
        return target;
    }

}

