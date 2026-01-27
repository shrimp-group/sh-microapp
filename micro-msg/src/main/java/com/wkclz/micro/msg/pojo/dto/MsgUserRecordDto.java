package com.wkclz.micro.msg.pojo.dto;

import com.wkclz.micro.msg.pojo.entity.MsgUserRecord;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table msg_user_record (用户消息记录) 数据库实例扩展，代码重新生成不覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class MsgUserRecordDto extends MsgUserRecord {

    private String title;
    private String sender;
    private String content;
    private String extUrl;


    /**
     * entity 转 Dto
     * @param source
     * @return
     */
    public static MsgUserRecordDto copy(MsgUserRecord source) {
        MsgUserRecordDto target = new MsgUserRecordDto();
        MsgUserRecord.copy(source, target);
        return target;
    }
}

