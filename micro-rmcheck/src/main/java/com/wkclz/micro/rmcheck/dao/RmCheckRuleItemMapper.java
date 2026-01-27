package com.wkclz.micro.rmcheck.dao;

import com.wkclz.micro.rmcheck.pojo.dto.RmCheckRuleItemDto;
import com.wkclz.micro.rmcheck.pojo.entity.RmCheckRuleItem;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table rm_check_rule_item (删除检查规则-检查项) DAO 接口，代码重新生成不覆盖
 */

@Mapper
public interface RmCheckRuleItemMapper extends BaseMapper<RmCheckRuleItem> {


    List<RmCheckRuleItemDto> getRmCheckRuleItem(RmCheckRuleItemDto dto);

    List<RmCheckRuleItem> getRmCheckRuleItem4Check(RmCheckRuleItem entity);


}

