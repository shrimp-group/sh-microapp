package com.wkclz.micro.msg.pojo.entity;

import com.wkclz.core.annotation.Desc;
import com.wkclz.core.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;




/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table msg_user_settings (用户消息设置) 重新生成代码会覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class MsgUserSettings extends BaseEntity {

    /**
     * 用户名
     */
    @Desc("用户名")
    private String userCode;

    /**
     * 事件消息配置(JSON)
     */
    @Desc("事件消息配置(JSON)")
    private String notifyEvent;

    /**
     * 系统消息配置(JSON)
     */
    @Desc("系统消息配置(JSON)")
    private String notifySystem;


    public static MsgUserSettings copy(MsgUserSettings source, MsgUserSettings target) {
        if (target == null ) { target = new MsgUserSettings();}
        if (source == null) { return target; }
        target.setId(source.getId());
        target.setUserCode(source.getUserCode());
        target.setNotifyEvent(source.getNotifyEvent());
        target.setNotifySystem(source.getNotifySystem());
        target.setSort(source.getSort());
        target.setCreateTime(source.getCreateTime());
        target.setCreateBy(source.getCreateBy());
        target.setUpdateTime(source.getUpdateTime());
        target.setUpdateBy(source.getUpdateBy());
        target.setRemark(source.getRemark());
        target.setVersion(source.getVersion());
        return target;
    }

    public static MsgUserSettings copyIfNotNull(MsgUserSettings source, MsgUserSettings target) {
        if (target == null ) { target = new MsgUserSettings();}
        if (source == null) { return target; }
        if (source.getId() != null) { target.setId(source.getId()); }
        if (source.getUserCode() != null) { target.setUserCode(source.getUserCode()); }
        if (source.getNotifyEvent() != null) { target.setNotifyEvent(source.getNotifyEvent()); }
        if (source.getNotifySystem() != null) { target.setNotifySystem(source.getNotifySystem()); }
        if (source.getSort() != null) { target.setSort(source.getSort()); }
        if (source.getCreateTime() != null) { target.setCreateTime(source.getCreateTime()); }
        if (source.getCreateBy() != null) { target.setCreateBy(source.getCreateBy()); }
        if (source.getUpdateTime() != null) { target.setUpdateTime(source.getUpdateTime()); }
        if (source.getUpdateBy() != null) { target.setUpdateBy(source.getUpdateBy()); }
        if (source.getRemark() != null) { target.setRemark(source.getRemark()); }
        if (source.getVersion() != null) { target.setVersion(source.getVersion()); }
        return target;
    }

}

