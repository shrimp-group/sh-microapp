package com.wkclz.micro.mask.dao;

import com.wkclz.micro.mask.pojo.entity.MdmMaskRule;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_mask_rule (脱敏规则) DAO 接口，代码重新生成不覆盖
 */

@Mapper
public interface MdmMaskRuleMapper extends BaseMapper<MdmMaskRule> {

    List<MdmMaskRule> getMaskRuleList(MdmMaskRule entity);
    List<MdmMaskRule> rules4Cache();



}

