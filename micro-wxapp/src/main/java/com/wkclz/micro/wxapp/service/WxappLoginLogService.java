package com.wkclz.micro.wxapp.service;

import com.wkclz.common.emuns.ResultStatus;
import com.wkclz.common.exception.BizException;
import com.wkclz.common.utils.AssertUtil;
import com.wkclz.micro.wxapp.dao.WxappLoginLogMapper;
import com.wkclz.micro.wxapp.bean.entity.WxappLoginLog;
import com.wkclz.mybatis.base.BaseService;
import org.springframework.stereotype.Service;

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
        AssertUtil.notNull(entity.getId(), "请求错误！参数[id]不能为空");
        AssertUtil.notNull(entity.getVersion(), "请求错误！参数[version]不能为空");
        WxappLoginLog oldEntity = get(entity.getId());
        if (oldEntity == null) {
            throw BizException.error(ResultStatus.RECORD_NOT_EXIST);
        }
        WxappLoginLog.copyIfNotNull(entity, oldEntity);
        updateSelective(oldEntity);
        return oldEntity;
    }

    private void duplicateCheck(WxappLoginLog entity) {
        AssertUtil.notNull(entity);
        // 唯一条件为空，直接通过
        if (true) {
            return;
        }
        
        // 唯一条件不为空，请设置唯一条件
        WxappLoginLog param = new WxappLoginLog();
        // 唯一条件
        param = get(param);
        if (param == null) {
            return;
        }
        if (param.getId().equals(entity.getId())) {
            return;
        }
        // 查到有值，为新增或 id 不一样场景，为数据重复
        throw BizException.error(ResultStatus.RECORD_DUPLICATE);
    }

}

