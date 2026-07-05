package com.wkclz.micro.msg.service;

import com.wkclz.core.enums.ResultCode;
import com.wkclz.core.exception.UserException;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.msg.mapper.MsgTemplateMapper;
import com.wkclz.micro.msg.bean.entity.MsgTemplate;
import com.wkclz.mybatis.service.BaseService;
import com.wkclz.redis.helper.RedisIdGenerator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Description Create by sh-generator
 * @author shrimp
 * @table msg_template (消息模板) 单表服务类，代码重新生成不覆盖. 只建议完成单表的逻辑，或主表为 msg_template 的逻辑. 其他逻辑放 custom 中
 */
 
@Service
public class MsgTemplateService extends BaseService<MsgTemplate, MsgTemplateMapper> {

    @Autowired
    private RedisIdGenerator redisIdGenerator;

    // 示例方法，可删除
    public Long example() {
        return mapper.example();
    }

    public MsgTemplate create(MsgTemplate entity) {
        duplicateCheck(entity);
        if (StringUtils.isBlank(entity.getTemplateCode())) {
            entity.setTemplateCode(redisIdGenerator.generateIdWithPrefix("msg_"));
        }
        mapper.insert(entity);
        return entity;
    }

    public MsgTemplate update(MsgTemplate entity) {
        duplicateCheck(entity);
        MsgTemplate oldEntity = selectById(entity.getId());
        if (oldEntity == null) {
            throw ValidationException.of(ResultCode.RECORD_NOT_EXIST);
        }
        entity.setTemplateCode(null);
        MsgTemplate.copyIfNotNull(entity, oldEntity);
        updateByIdSelective(oldEntity);
        return oldEntity;
    }

    public MsgTemplate save(MsgTemplate entity) {
        return entity.getId() == null ? create(entity) : update(entity);
    }

    public MsgTemplate remove(MsgTemplate entity) {
        MsgTemplate oldEntity = selectById(entity.getId());
        if (oldEntity == null) {
            throw ValidationException.of(ResultCode.RECORD_NOT_EXIST);
        }
        deleteById(oldEntity);
        return oldEntity;
    }

    private void duplicateCheck(MsgTemplate entity) {
        // 唯一条件为空，直接通过
        if (true) {
            return;
        }
        
        // 唯一条件不为空，请设置唯一条件
        MsgTemplate param = new MsgTemplate();
        // 唯一条件
        param = selectOneByEntity(param);
        if (param == null) {
            return;
        }
        if (param.getId().equals(entity.getId())) {
            return;
        }
        // 查到有值，为新增或 id 不一样场景，为数据重复
        throw UserException.of(ResultCode.RECORD_DUPLICATE);
    }

}

