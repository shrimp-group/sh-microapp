package com.wkclz.micro.msg.service;

import com.wkclz.iam.sdk.helper.SessionHelper;
import com.wkclz.micro.msg.dao.MsgUserSettingsMapper;
import com.wkclz.micro.msg.pojo.entity.MsgUserSettings;
import com.wkclz.mybatis.service.BaseService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table msg_user_settings (用户消息设置) 单表服务类，代码重新生成不覆盖. 只建议完成单表的逻辑，或主表为 msg_user_settings 的逻辑. 其他逻辑放 custom 中
 */
 
@Service
public class MsgUserSettingsService extends BaseService<MsgUserSettings, MsgUserSettingsMapper> {


    @Transactional(rollbackFor = Exception.class)
    public MsgUserSettings getUserSettings() {
        return getUserSettings(null);
    }

    @Transactional(rollbackFor = Exception.class)
    public MsgUserSettings getUserSettings(String userCode) {
        if (StringUtils.isBlank(userCode)) {
            userCode = SessionHelper.getUserCode();
        }
        MsgUserSettings settings = new MsgUserSettings();
        settings.setUserCode(userCode);
        settings = selectOneByEntity(settings);
        if (settings != null) {
            return settings;
        }
        settings = new MsgUserSettings();
        settings.setUserCode(userCode);
        settings.setNotifyEvent("{}");
        settings.setNotifySystem("{}");
        insert(settings);
        return selectById(settings.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer setUserSettings(MsgUserSettings settings) {
        MsgUserSettings userSettings = getUserSettings();
        userSettings.setNotifySystem(settings.getNotifySystem());
        userSettings.setNotifyEvent(settings.getNotifyEvent());
        return updateById(userSettings);
    }


}

