package com.wkclz.micro.msg.dao;

import com.wkclz.micro.msg.pojo.entity.MsgUserSettings;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table msg_user_settings (用户消息设置) DAO 接口，代码重新生成不覆盖
 */

@Mapper
public interface MsgUserSettingsMapper extends BaseMapper<MsgUserSettings> {

    // 示例查询,可删除
    Long example();

}

