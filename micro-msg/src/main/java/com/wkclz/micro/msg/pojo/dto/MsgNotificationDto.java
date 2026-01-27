package com.wkclz.micro.msg.pojo.dto;

import com.wkclz.micro.msg.pojo.entity.MsgNotification;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table msg_notification (消息通知) 数据库实例扩展，代码重新生成不覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class MsgNotificationDto extends MsgNotification {

    private Integer recordCount;
    private Integer readCount;

    private String sentToUser;
    private List<String> sentToUsers;


    /**
     * entity 转 Dto
     * @param source
     * @return
     */
    public static MsgNotificationDto copy(MsgNotification source) {
        MsgNotificationDto target = new MsgNotificationDto();
        MsgNotification.copy(source, target);
        return target;
    }
}

