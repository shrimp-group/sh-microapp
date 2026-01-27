package com.wkclz.micro.liteflow.service;

import com.wkclz.core.base.PageData;
import com.wkclz.core.enums.ResultCode;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.liteflow.dao.LiteflowScriptMapper;
import com.wkclz.micro.liteflow.pojo.entity.LiteflowScript;
import com.wkclz.mybatis.helper.PageQuery;
import com.wkclz.mybatis.service.BaseService;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table liteflow_script (liteflow-脚本) 单表服务类，代码重新生成不覆盖. 只建议完成单表的逻辑，或主表为 liteflow_script 的逻辑. 其他逻辑放 custom 中
 */
 
@Service
public class LiteflowScriptService extends BaseService<LiteflowScript, LiteflowScriptMapper> {

    public PageData<LiteflowScript> getLiteflowScriptPage(LiteflowScript entity) {
        return PageQuery.page(entity, mapper::getLiteflowScriptList4Page);
    }

    public LiteflowScript create(LiteflowScript entity) {
        duplicateCheck(entity);
        mapper.insert(entity);
        return entity;
    }

    public LiteflowScript update(LiteflowScript entity) {
        duplicateCheck(entity);
        Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        Assert.notNull(entity.getId(), ResultCode.UPDATE_NO_VERSION.getMessage());
        LiteflowScript oldEntity = selectById(entity.getId());
        if (oldEntity == null) {
            throw ValidationException.of(ResultCode.RECORD_NOT_EXIST);
        }
        LiteflowScript.copyIfNotNull(entity, oldEntity);
        updateByIdSelective(oldEntity);
        return oldEntity;
    }

    private void duplicateCheck(LiteflowScript entity) {
        // 唯一条件为空，直接通过
        if (true) {
            return;
        }
        
        // 唯一条件不为空，请设置唯一条件
        LiteflowScript param = new LiteflowScript();
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

