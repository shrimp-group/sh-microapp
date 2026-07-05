package com.wkclz.micro.wxmp.service;

import com.wkclz.core.enums.ResultCode;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.wxmp.mapper.WxmpLoginLogMapper;
import com.wkclz.micro.wxmp.bean.entity.WxmpLoginLog;
import com.wkclz.mybatis.service.BaseService;
import org.springframework.stereotype.Service;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table wxmp_login_log (微信用户登录日志) 单表服务类，代码重新生成不覆盖. 只建议完成单表的逻辑，或主表为 wxmp_login_log 的逻辑. 其他逻辑放 custom 中
 */
 
@Service
public class WxmpLoginLogService extends BaseService<WxmpLoginLog, WxmpLoginLogMapper> {

    // 示例方法，可删除
    public Long example() {
        return mapper.example();
    }

    public WxmpLoginLog create(WxmpLoginLog entity) {
        duplicateCheck(entity);
        mapper.insert(entity);
        return entity;
    }

    public WxmpLoginLog update(WxmpLoginLog entity) {
        duplicateCheck(entity);
        WxmpLoginLog oldEntity = selectById(entity.getId());
        if (oldEntity == null) {
            throw ValidationException.of(ResultCode.RECORD_NOT_EXIST);
        }
        WxmpLoginLog.copyIfNotNull(entity, oldEntity);
        updateByIdSelective(oldEntity);
        return oldEntity;
    }

    private void duplicateCheck(WxmpLoginLog entity) {
        // 唯一条件为空，直接通过
        if (true) {
            return;
        }
        
        // 唯一条件不为空，请设置唯一条件
        WxmpLoginLog param = new WxmpLoginLog();
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

