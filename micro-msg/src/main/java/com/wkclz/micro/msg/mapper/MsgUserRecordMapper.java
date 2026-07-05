package com.wkclz.micro.msg.mapper;

import com.wkclz.micro.msg.bean.dto.MsgUserRecordDto;
import com.wkclz.mybatis.mapper.BaseMapper;
import com.wkclz.micro.msg.bean.entity.MsgUserRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Description Create by sh-generator
 * @author shrimp
 * @table msg_user_record (用户消息记录) Mapper 接口，代码重新生成不覆盖
 */

@Mapper
public interface MsgUserRecordMapper extends BaseMapper<MsgUserRecord> {

    List<MsgUserRecordDto> getPersonalRecordList(MsgUserRecordDto dto);

    List<MsgUserRecordDto> getPersonalRecordList4Page(MsgUserRecordDto dto);

    MsgUserRecordDto getNoticeInfo(MsgUserRecordDto dto);

    MsgUserRecordDto getNoticeInfoById(MsgUserRecordDto dto);

    Integer updateShowTimes(@Param("id") Long id);

    Integer markRecodeAsReaded(MsgUserRecord entity);

}

