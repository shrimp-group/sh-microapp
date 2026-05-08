package com.wkclz.micro.msg.mapper;

import com.wkclz.mybatis.mapper.BaseMapper;
import com.wkclz.micro.msg.bean.entity.MsgUserSettings;
import org.apache.ibatis.annotations.Mapper;

/**
 * Description Create by sh-generator
 * @author shrimp
 * @table msg_user_settings (用户消息设置) DAO 接口，代码重新生成不覆盖
 */

@Mapper
public interface MsgUserSettingsMapper extends BaseMapper<MsgUserSettings> {

    // 示例查询,可删除
    Long example();

}

