package com.wkclz.micro.msg.mapper;

import com.wkclz.micro.msg.bean.dto.MsgNotificationDto;
import com.wkclz.mybatis.mapper.BaseMapper;
import com.wkclz.micro.msg.bean.entity.MsgNotification;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Description Create by sh-generator
 * @author shrimp
 * @table msg_notification (消息通知) Mapper 接口，代码重新生成不覆盖
 */

@Mapper
public interface MsgNotificationMapper extends BaseMapper<MsgNotification> {

    List<MsgNotificationDto> getNotificationList(MsgNotificationDto dto);

}

