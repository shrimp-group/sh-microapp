package com.wkclz.micro.wxapp.service;

import com.wkclz.core.enums.ResultCode;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.wxapp.bean.entity.WxappLoginLog;
import com.wkclz.micro.wxapp.mapper.WxappLoginLogMapper;
import com.wkclz.mybatis.service.BaseService;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table wxapp_login_log (小程序用户登录日志) 单表服务类，代码重新生成不覆盖. 只建议完成单表的逻辑，或主表为 wxapp_login_log 的逻辑. 其他逻辑放 custom 中
 */
 
@Service
public class WxappLoginLogService extends BaseService<WxappLoginLog, WxappLoginLogMapper> {

    // 示例方法，可删除
    public Long example() {
        return mapper.example();
    }

    public WxappLoginLog create(WxappLoginLog entity) {
        duplicateCheck(entity);
        mapper.insert(entity);
        return entity;
    }

    public WxappLoginLog update(WxappLoginLog entity) {
        duplicateCheck(entity);
        Assert.notNull(entity.getId(), "请求错误！参数[id]不能为空");
        Assert.notNull(entity.getVersion(), "请求错误！参数[version]不能为空");
        WxappLoginLog oldEntity = selectById(entity.getId());
        if (oldEntity == null) {
            throw ValidationException.of(ResultCode.RECORD_NOT_EXIST);
        }
        WxappLoginLog.copyIfNotNull(entity, oldEntity);
        updateByIdSelective(oldEntity);
        return oldEntity;
    }

    private void duplicateCheck(WxappLoginLog entity) {
        // 唯一条件为空，直接通过
        if (true) {
            return;
        }
        
        // 唯一条件不为空，请设置唯一条件
        WxappLoginLog param = new WxappLoginLog();
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

