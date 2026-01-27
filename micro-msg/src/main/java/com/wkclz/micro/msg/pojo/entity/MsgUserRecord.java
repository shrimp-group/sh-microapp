package com.wkclz.micro.msg.pojo.entity;

import com.wkclz.core.annotation.Desc;
import com.wkclz.core.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;


import java.util.Date;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table msg_user_record (用户消息记录) 重新生成代码会覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class MsgUserRecord extends BaseEntity {

    /**
     * 用户名
     */
    @Desc("用户名")
    private String userCode;

    /**
     * 消息编码
     */
    @Desc("消息编码")
    private String noticeNo;

    /**
     * 阅读状态
     */
    @Desc("阅读状态")
    private Integer readStatus;

    /**
     * 阅读时间
     */
    @Desc("阅读时间")
    private Date readTime;

    /**
     * 消息展示次数
     */
    @Desc("消息展示次数")
    private Integer showTimes;


    public static MsgUserRecord copy(MsgUserRecord source, MsgUserRecord target) {
        if (target == null ) { target = new MsgUserRecord();}
        if (source == null) { return target; }
        target.setId(source.getId());
        target.setUserCode(source.getUserCode());
        target.setNoticeNo(source.getNoticeNo());
        target.setReadStatus(source.getReadStatus());
        target.setReadTime(source.getReadTime());
        target.setShowTimes(source.getShowTimes());
        target.setSort(source.getSort());
        target.setCreateTime(source.getCreateTime());
        target.setCreateBy(source.getCreateBy());
        target.setUpdateTime(source.getUpdateTime());
        target.setUpdateBy(source.getUpdateBy());
        target.setRemark(source.getRemark());
        target.setVersion(source.getVersion());
        return target;
    }

    public static MsgUserRecord copyIfNotNull(MsgUserRecord source, MsgUserRecord target) {
        if (target == null ) { target = new MsgUserRecord();}
        if (source == null) { return target; }
        if (source.getId() != null) { target.setId(source.getId()); }
        if (source.getUserCode() != null) { target.setUserCode(source.getUserCode()); }
        if (source.getNoticeNo() != null) { target.setNoticeNo(source.getNoticeNo()); }
        if (source.getReadStatus() != null) { target.setReadStatus(source.getReadStatus()); }
        if (source.getReadTime() != null) { target.setReadTime(source.getReadTime()); }
        if (source.getShowTimes() != null) { target.setShowTimes(source.getShowTimes()); }
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

