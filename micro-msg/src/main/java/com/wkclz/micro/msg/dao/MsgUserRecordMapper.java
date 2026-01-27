package com.wkclz.micro.msg.dao;

import com.wkclz.micro.msg.pojo.dto.MsgUserRecordDto;
import com.wkclz.micro.msg.pojo.entity.MsgUserRecord;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table msg_user_record (用户消息记录) DAO 接口，代码重新生成不覆盖
 */

@Mapper
public interface MsgUserRecordMapper extends BaseMapper<MsgUserRecord> {

    List<MsgUserRecordDto> getPersonalRecordList(MsgUserRecordDto dto);

    List<MsgUserRecordDto> getPersonalRecordList4Page(MsgUserRecordDto dto);

    MsgUserRecordDto getNoticeInfo(MsgUserRecordDto dto);

    Integer updateShowTimes(@Param("id") Long id);

    Integer markRecodeAsReaded(MsgUserRecord entity);


}

