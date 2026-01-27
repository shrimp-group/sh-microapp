package com.wkclz.micro.audit.service;

import com.wkclz.core.base.PageData;
import com.wkclz.core.enums.ResultCode;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.audit.dao.MdmChangeLogMapper;
import com.wkclz.micro.audit.pojo.entity.MdmChangeLog;
import com.wkclz.mybatis.helper.PageQuery;
import com.wkclz.mybatis.service.BaseService;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_change_log (变更记录) 单表服务类，代码重新生成不覆盖. 只建议完成单表的逻辑，或主表为 mdm_change_log 的逻辑. 其他逻辑放 custom 中
 */
 
@Service
public class MdmChangeLogService extends BaseService<MdmChangeLog, MdmChangeLogMapper> {

    public PageData<MdmChangeLog> getChangeLogPage(MdmChangeLog emtity) {
        return PageQuery.page(emtity, mapper::getChangeLogList);
    }

    public MdmChangeLog create(MdmChangeLog entity) {
        duplicateCheck(entity);
        mapper.insert(entity);
        return entity;
    }

    public MdmChangeLog update(MdmChangeLog entity) {
        duplicateCheck(entity);
        Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        Assert.notNull(entity.getId(), ResultCode.UPDATE_NO_VERSION.getMessage());
        MdmChangeLog oldEntity = selectById(entity.getId());
        if (oldEntity == null) {
            throw ValidationException.of(ResultCode.RECORD_NOT_EXIST);
        }
        MdmChangeLog.copyIfNotNull(entity, oldEntity);
        updateByIdSelective(oldEntity);
        return oldEntity;
    }

    private void duplicateCheck(MdmChangeLog entity) {
        // 唯一条件为空，直接通过
        if (true) {
            return;
        }
        
        // 唯一条件不为空，请设置唯一条件
        MdmChangeLog param = new MdmChangeLog();
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

