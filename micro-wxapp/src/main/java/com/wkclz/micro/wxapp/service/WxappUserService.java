package com.wkclz.micro.wxapp.service;

import com.wkclz.common.emuns.ResultStatus;
import com.wkclz.common.exception.BizException;
import com.wkclz.common.utils.AssertUtil;
import com.wkclz.micro.wxapp.dao.WxappUserMapper;
import com.wkclz.micro.wxapp.bean.entity.WxappUser;
import com.wkclz.mybatis.base.BaseService;
import org.springframework.stereotype.Service;

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
        AssertUtil.notNull(entity.getId(), "请求错误！参数[id]不能为空");
        AssertUtil.notNull(entity.getVersion(), "请求错误！参数[version]不能为空");
        WxappUser oldEntity = get(entity.getId());
        if (oldEntity == null) {
            throw BizException.error(ResultStatus.RECORD_NOT_EXIST);
        }
        WxappUser.copyIfNotNull(entity, oldEntity);
        updateSelective(oldEntity);
        return oldEntity;
    }

    private void duplicateCheck(WxappUser entity) {
        AssertUtil.notNull(entity);
        // 唯一条件为空，直接通过
        if (true) {
            return;
        }
        
        // 唯一条件不为空，请设置唯一条件
        WxappUser param = new WxappUser();
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

