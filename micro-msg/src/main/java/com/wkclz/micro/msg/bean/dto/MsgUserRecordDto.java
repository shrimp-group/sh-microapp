package com.wkclz.micro.msg.bean.dto;

import com.wkclz.micro.msg.bean.entity.MsgUserRecord;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Description Create by sh-generator
 * @author shrimp
 * @table MsgUserRecord (用户消息记录) 数据库实例扩展，代码重新生成不覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class MsgUserRecordDto extends MsgUserRecord {


    private String title;
    private String sender;
    private String content;
    private String extUrl;

}
