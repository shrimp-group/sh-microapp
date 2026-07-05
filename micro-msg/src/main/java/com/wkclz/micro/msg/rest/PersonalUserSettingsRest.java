package com.wkclz.micro.msg.rest;

import com.wkclz.core.base.R;
import com.wkclz.micro.msg.bean.entity.MsgUserSettings;
import com.wkclz.micro.msg.bean.req.MsgUserSettingsSaveReq;
import com.wkclz.micro.msg.bean.resp.MsgUserSettingsResp;
import com.wkclz.micro.msg.service.MsgUserSettingsService;
import com.wkclz.tool.utils.BeanUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table msg_user_settings (用户消息设置) 示例rest 接口，代码重新生成会覆盖
 */
@Tag(name = "4.个人消息设置", description = "个人消息设置管理接口")
@RestController
@RequestMapping(Route.PREFIX)
public class PersonalUserSettingsRest {

    @Autowired
    private MsgUserSettingsService msgUserSettingsService;

    @Operation(summary = "1.个人消息设置-获取配置", description = "获取当前用户的消息配置")
    @GetMapping(Route.PERSONAL_MSG_SETTINGS)
    public R<MsgUserSettingsResp> personalMsgSettings() {
        MsgUserSettings settings = msgUserSettingsService.getUserSettings();
        MsgUserSettingsResp resp = BeanUtil.cp(settings, MsgUserSettingsResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "2.个人消息设置-保存配置", description = "保存当前用户的消息配置")
    @PostMapping(Route.PERSONAL_MSG_SETTINGS_SAVE)
    public R<Integer> personalMsgSettingsSave(@RequestBody MsgUserSettingsSaveReq req) {
        MsgUserSettings entity = BeanUtil.cp(req, MsgUserSettings.class);
        Integer i = msgUserSettingsService.setUserSettings(entity);
        return R.ok(i);
    }

}
