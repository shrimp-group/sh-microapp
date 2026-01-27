package com.wkclz.micro.pdf.pojo.dto;

import com.wkclz.micro.pdf.pojo.entity.MdmPdfTemplate;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_pdf_template (PDF模板) 数据库实例扩展，代码重新生成不覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class MdmPdfTemplateDto extends MdmPdfTemplate {



    /**
     * entity 转 Dto
     * @param source
     * @return
     */
    public static MdmPdfTemplateDto copy(MdmPdfTemplate source) {
        MdmPdfTemplateDto target = new MdmPdfTemplateDto();
        MdmPdfTemplate.copy(source, target);
        return target;
    }
}

