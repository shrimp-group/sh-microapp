package com.wkclz.micro.rmcheck.dao;

import com.wkclz.micro.rmcheck.pojo.dto.RmCheckRuleItemDto;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author shrimp
 */
@Mapper
public interface CheckMapper {

    Long rmCheck(RmCheckRuleItemDto dto);

}
