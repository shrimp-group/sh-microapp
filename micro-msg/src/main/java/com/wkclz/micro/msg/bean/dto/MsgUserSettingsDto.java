package com.wkclz.micro.msg.bean.dto;

import com.wkclz.micro.msg.bean.entity.MsgUserSettings;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Description Create by sh-generator
 * @author shrimp
 * @table MsgUserSettings (用户消息设置) 数据库实例扩展，代码重新生成不覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class MsgUserSettingsDto extends MsgUserSettings {




    /**
     * entity 转 Dto
     * @param source
     * @return
     */
    public static MsgUserSettingsDto copy(MsgUserSettings source) {
        MsgUserSettingsDto target = new MsgUserSettingsDto();
        MsgUserSettings.copy(source, target);
        return target;
    }
}

