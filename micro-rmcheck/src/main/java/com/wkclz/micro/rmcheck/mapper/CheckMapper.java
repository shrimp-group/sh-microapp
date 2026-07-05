package com.wkclz.micro.rmcheck.mapper;

import com.wkclz.micro.rmcheck.bean.dto.RmCheckRuleItemDto;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author shrimp
 */
@Mapper
public interface CheckMapper {

    Long rmCheck(RmCheckRuleItemDto dto);

}
