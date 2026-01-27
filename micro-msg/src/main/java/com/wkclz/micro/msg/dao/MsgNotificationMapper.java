package com.wkclz.micro.msg.dao;

import com.wkclz.micro.msg.pojo.dto.MsgNotificationDto;
import com.wkclz.micro.msg.pojo.entity.MsgNotification;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table msg_notification (消息通知) DAO 接口，代码重新生成不覆盖
 */

@Mapper
public interface MsgNotificationMapper extends BaseMapper<MsgNotification> {

    List<MsgNotificationDto> getNotificationList(MsgNotificationDto dto);

}

