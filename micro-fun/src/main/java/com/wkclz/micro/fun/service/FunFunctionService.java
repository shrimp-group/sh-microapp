package com.wkclz.micro.fun.service;

import com.wkclz.core.base.PageData;
import com.wkclz.core.enums.ResultCode;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.fun.dao.FunFunctionMapper;
import com.wkclz.micro.fun.pojo.dto.FunFunctionDto;
import com.wkclz.micro.fun.pojo.entity.FunFunction;
import com.wkclz.mybatis.helper.PageQuery;
import com.wkclz.mybatis.service.BaseService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table fun_function (函数-函数体) 单表服务类，代码重新生成不覆盖. 只建议完成单表的逻辑，或主表为 fun_function 的逻辑. 其他逻辑放 custom 中
 */
 
@Service
public class FunFunctionService extends BaseService<FunFunction, FunFunctionMapper> {

    public PageData<FunFunctionDto> getFunctionPage(FunFunctionDto dto) {
        return PageQuery.page(dto, mapper::getFunctionList);
    }

    public FunFunction getFunction(String funCode) {
        if (StringUtils.isBlank(funCode)) {
            return null;
        }
        FunFunction fun = new FunFunction();
        fun.setFunCode(funCode);
        fun = selectOneByEntity(fun);
        if (fun == null) {
            return null;
        }
        return fun;
    }


    public FunFunction create(FunFunction entity) {
        duplicateCheck(entity);
        mapper.insert(entity);
        return entity;
    }

    public FunFunction update(FunFunction entity) {
        duplicateCheck(entity);
        Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        Assert.notNull(entity.getId(), ResultCode.UPDATE_NO_VERSION.getMessage());
        FunFunction oldEntity = selectById(entity.getId());
        if (oldEntity == null) {
            throw ValidationException.of(ResultCode.RECORD_NOT_EXIST);
        }
        FunFunction.copyIfNotNull(entity, oldEntity);
        updateByIdSelective(oldEntity);
        return oldEntity;
    }


    public List<FunFunctionDto> getFunctionOption(FunFunctionDto dto) {
        return mapper.getFunctionOption(dto);
    }


    private void duplicateCheck(FunFunction entity) {
        // 唯一条件为空，直接通过

        // 唯一条件不为空，请设置唯一条件
        FunFunction param = new FunFunction();
        param.setFunCode(entity.getFunCode());
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

