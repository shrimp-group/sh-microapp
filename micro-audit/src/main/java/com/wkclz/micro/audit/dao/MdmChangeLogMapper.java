package com.wkclz.micro.audit.dao;

import com.wkclz.micro.audit.pojo.entity.MdmChangeLog;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_change_log (变更记录) DAO 接口，代码重新生成不覆盖
 */

@Mapper
public interface MdmChangeLogMapper extends BaseMapper<MdmChangeLog> {

    List<MdmChangeLog> getChangeLogList(MdmChangeLog dto);

}

