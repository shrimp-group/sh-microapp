package com.wkclz.micro.rmcheck.dao;

import com.wkclz.micro.rmcheck.pojo.dto.RmCheckRuleDto;
import com.wkclz.micro.rmcheck.pojo.entity.RmCheckRule;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table rm_check_rule (删除检查规则) DAO 接口，代码重新生成不覆盖
 */

@Mapper
public interface RmCheckRuleMapper extends BaseMapper<RmCheckRule> {

    List<RmCheckRuleDto> getRmCheckRuleList(RmCheckRuleDto dto);

    List<RmCheckRule> getRmCheckRules4Check(RmCheckRule entity);

}

