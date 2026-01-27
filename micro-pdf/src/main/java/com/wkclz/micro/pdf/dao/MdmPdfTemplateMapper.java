package com.wkclz.micro.pdf.dao;

import com.wkclz.micro.pdf.pojo.entity.MdmPdfTemplate;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_pdf_template (PDF模板) DAO 接口，代码重新生成不覆盖
 */

@Mapper
public interface MdmPdfTemplateMapper extends BaseMapper<MdmPdfTemplate> {

    List<MdmPdfTemplate> getPdfTemplateList(MdmPdfTemplate entity);

    List<MdmPdfTemplate> get4Cache();



}

