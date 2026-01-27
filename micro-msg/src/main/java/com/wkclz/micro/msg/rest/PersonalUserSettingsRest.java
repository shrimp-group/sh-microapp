package com.wkclz.micro.msg.rest;

import com.wkclz.core.base.R;
import com.wkclz.micro.msg.pojo.entity.MsgUserSettings;
import com.wkclz.micro.msg.service.MsgUserSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table msg_user_settings (用户消息设置) 示例rest 接口，代码重新生成会覆盖
 */
@RestController
public class PersonalUserSettingsRest {

    @Autowired
    private MsgUserSettingsService msgUserSettingsService;



    /**
     * @api {get} /personal/msg/settings 21. 个人消息获取配置
     * @apiGroup MSG
     *
     * @apiVersion 0.0.1
     * @apiDescription 个人消息获取配置
     *
     * @apiSuccess {String} [notifyEvent] 事件消息配置(JSON)
     * @apiSuccess {String} [notifySystem] 系统消息配置(JSON)
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": {
     *          "notifyEvent": "notifyEvent",
     *          "notifySystem": "notifySystem",
     *     }
     * }
     *
     */
    @GetMapping(Route.PERSONAL_MSG_SETTINGS)
    public R personalMsgSettings() {
        MsgUserSettings settings = msgUserSettingsService.getUserSettings();
        MsgUserSettings target = new MsgUserSettings();
        target.setNotifySystem(settings.getNotifySystem());
        target.setNotifyEvent(settings.getNotifyEvent());
        return R.ok(target);
    }



    /**
     * @api {post} /personal/msg/settings/save 22. 个人消息保存配置
     * @apiGroup MSG
     *
     * @apiVersion 0.0.1
     * @apiDescription 个人消息保存配置
     *
     * @apiParam {String} [notifyEvent] <code>body</code>事件消息配置(JSON字符串)
     * @apiParam {String} [notifySystem] <code>body</code>系统消息配置(JSON字符串)
     *
     * @apiParamExample {json} 请求样例:
     * {
     *      "notifyEvent": {
     *          "aa": "bb",
     *          "cc": "dd"
     *      },
     *      "notifySystem": {}
     * }
     *
     * @apiSuccessExample {json} 返回样例:
     * {
     *     "code": 1,
     *     "data": ObjectEntity
     * }
     *
     */
    @PostMapping(Route.PERSONAL_MSG_SETTINGS_SAVE)
    public R personalMsgSettingsSave(@RequestBody MsgUserSettings entity) {
        Integer i = msgUserSettingsService.setUserSettings(entity);
        return R.ok(i);
    }

}

