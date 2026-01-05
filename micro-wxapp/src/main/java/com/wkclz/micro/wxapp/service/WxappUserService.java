package com.wkclz.micro.wxapp.service;

import com.wkclz.core.enums.ResultCode;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.wxapp.bean.entity.WxappUser;
import com.wkclz.micro.wxapp.dao.WxappUserMapper;
import com.wkclz.mybatis.service.BaseService;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table wxapp_user (小程序用户) 单表服务类，代码重新生成不覆盖. 只建议完成单表的逻辑，或主表为 wxapp_user 的逻辑. 其他逻辑放 custom 中
 */
 
@Service
public class WxappUserService extends BaseService<WxappUser, WxappUserMapper> {


    public WxappUser create(WxappUser entity) {
        duplicateCheck(entity);
        mapper.insert(entity);
        return entity;
    }

    public WxappUser update(WxappUser entity) {
        duplicateCheck(entity);
        Assert.notNull(entity.getId(), "请求错误！参数[id]不能为空");
        Assert.notNull(entity.getVersion(), "请求错误！参数[version]不能为空");
        WxappUser oldEntity = selectById(entity.getId());
        if (oldEntity == null) {
            throw ValidationException.of(ResultCode.NOT_FOUND);
        }
        WxappUser.copyIfNotNull(entity, oldEntity);
        updateByIdSelective(oldEntity);
        return oldEntity;
    }

    private void duplicateCheck(WxappUser entity) {
        // 唯一条件为空，直接通过
        if (true) {
            return;
        }
        
        // 唯一条件不为空，请设置唯一条件
        WxappUser param = new WxappUser();
        // 唯一条件
        param = selectOneByEntity(param);
        if (param == null) {
            return;
        }
        if (param.getId().equals(entity.getId())) {
            return;
        }
        // 查到有值，为新增或 id 不一样场景，为数据重复
        throw ValidationException.of(ResultCode.RECORD_DUPLICATE);
    }

}

