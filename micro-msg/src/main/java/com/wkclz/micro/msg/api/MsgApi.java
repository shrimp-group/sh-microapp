package com.wkclz.micro.msg.api;

import com.wkclz.iam.sdk.helper.SessionHelper;
import com.wkclz.micro.msg.pojo.dto.MsgNotificationDto;
import com.wkclz.micro.msg.service.MsgNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class MsgApi {

    @Autowired
    private MsgNotificationService msgNotificationService;

    /**
     * 发送消息
     *
     * @param title      消息标题
     * @param content    消息内容
     * @param toUserCode 发送目标用户名
     */
    public Integer sentMsg(String title, String content, String toUserCode) {
        return sentMsg(title, content, null, Collections.singletonList(toUserCode));
    }

    /**
     * 发送消息
     *
     * @param title       消息标题
     * @param content     消息内容
     * @param toUserCodes 发送目标用户名列表
     */
    public Integer sentMsg(String title, String content, List<String> toUserCodes) {
        return sentMsg(title, content, null, toUserCodes);
    }


    /**
     * 发送消息
     *
     * @param title      消息标题
     * @param content    消息内容
     * @param extUrl     跳转Url
     * @param toUserCode 发送目标用户名
     */
    public Integer sentMsg(String title, String content, String extUrl, String toUserCode) {
        return sentMsg(title, content, extUrl, Collections.singletonList(toUserCode));
    }

    /**
     * 发送消息
     *
     * @param title       消息标题
     * @param content     消息内容
     * @param extUrl      跳转Url
     * @param toUserCodes 发送目标用户名列表
     */
    public Integer sentMsg(String title, String content, String extUrl, List<String> toUserCodes) {
        MsgNotificationDto notice = new MsgNotificationDto();
        notice.setTitle(title);
        notice.setContent(content);
        notice.setExtUrl(extUrl);
        notice.setUserCode(SessionHelper.getUserCode());
        notice.setSentToUsers(toUserCodes);
        return msgNotificationService.createNotification(notice);
    }

}
