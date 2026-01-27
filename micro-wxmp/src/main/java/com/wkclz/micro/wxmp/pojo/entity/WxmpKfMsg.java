package com.wkclz.micro.wxmp.pojo.entity;

import com.wkclz.core.annotation.Desc;
import com.wkclz.core.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;


import java.util.Date;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table wxmp_kf_msg (公众号-客服消息) 重新生成代码会覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class WxmpKfMsg extends BaseEntity {

    /**
     * 租户编码
     */
    @Desc("租户编码")
    private String tenantCode;

    /**
     * 公众号appid
     */
    @Desc("公众号appid")
    private String appId;

    /**
     * 消息类型
     */
    @Desc("消息类型")
    private String msgType;

    /**
     * 发送方
     */
    @Desc("发送方")
    private String fromUser;

    /**
     * 接收方
     */
    @Desc("接收方")
    private String toUser;

    /**
     * 消息内容
     */
    @Desc("消息内容")
    private String content;

    /**
     * 消息ID
     */
    @Desc("消息ID")
    private Long msgId;

    /**
     * 消息时间
     */
    @Desc("消息时间")
    private Date msgTime;


    public static WxmpKfMsg copy(WxmpKfMsg source, WxmpKfMsg target) {
        if (target == null ) { target = new WxmpKfMsg();}
        if (source == null) { return target; }
        target.setId(source.getId());
        target.setTenantCode(source.getTenantCode());
        target.setAppId(source.getAppId());
        target.setMsgType(source.getMsgType());
        target.setFromUser(source.getFromUser());
        target.setToUser(source.getToUser());
        target.setContent(source.getContent());
        target.setMsgId(source.getMsgId());
        target.setMsgTime(source.getMsgTime());
        target.setSort(source.getSort());
        target.setCreateTime(source.getCreateTime());
        target.setCreateBy(source.getCreateBy());
        target.setUpdateTime(source.getUpdateTime());
        target.setUpdateBy(source.getUpdateBy());
        target.setRemark(source.getRemark());
        target.setVersion(source.getVersion());
        return target;
    }

    public static WxmpKfMsg copyIfNotNull(WxmpKfMsg source, WxmpKfMsg target) {
        if (target == null ) { target = new WxmpKfMsg();}
        if (source == null) { return target; }
        if (source.getId() != null) { target.setId(source.getId()); }
        if (source.getTenantCode() != null) { target.setTenantCode(source.getTenantCode()); }
        if (source.getAppId() != null) { target.setAppId(source.getAppId()); }
        if (source.getMsgType() != null) { target.setMsgType(source.getMsgType()); }
        if (source.getFromUser() != null) { target.setFromUser(source.getFromUser()); }
        if (source.getToUser() != null) { target.setToUser(source.getToUser()); }
        if (source.getContent() != null) { target.setContent(source.getContent()); }
        if (source.getMsgId() != null) { target.setMsgId(source.getMsgId()); }
        if (source.getMsgTime() != null) { target.setMsgTime(source.getMsgTime()); }
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

