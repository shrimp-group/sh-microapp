package com.wkclz.micro.rmcheck.service;

import com.wkclz.core.base.PageData;
import com.wkclz.core.enums.ResultCode;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.rmcheck.api.RmCheckApi;
import com.wkclz.micro.rmcheck.dao.RmCheckRuleMapper;
import com.wkclz.micro.rmcheck.pojo.dto.RmCheckRuleDto;
import com.wkclz.micro.rmcheck.pojo.entity.RmCheckRule;
import com.wkclz.mybatis.bean.TableInfo;
import com.wkclz.mybatis.config.ShMyBatisConfig;
import com.wkclz.mybatis.helper.PageQuery;
import com.wkclz.mybatis.service.BaseService;
import com.wkclz.mybatis.service.TableInfoService;
import com.wkclz.redis.helper.RedisIdGenerator;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table rm_check_rule (删除检查规则) 单表服务类，代码重新生成不覆盖. 只建议完成单表的逻辑，或主表为 rm_check_rule 的逻辑. 其他逻辑放 custom 中
 */
 
@Service
public class RmCheckRuleService extends BaseService<RmCheckRule, RmCheckRuleMapper> {



    @Autowired
    private RmCheckApi rmCheckApi;
    @Autowired
    private ShMyBatisConfig config;
    @Autowired
    private RedisIdGenerator redisIdGenerator;
    @Autowired
    private TableInfoService tableInfoService;

    public PageData<RmCheckRuleDto> getRmCheckRulePage(RmCheckRuleDto dto) {
        dto.setTableSchema(config.getTableSchema());
        return PageQuery.page(dto, mapper::getRmCheckRuleList);
    }

    public RmCheckRule create(RmCheckRule entity) {
        duplicateCheck(entity);
        tableCheck(entity);
        entity.setRuleCode(redisIdGenerator.generateIdWithPrefix("rm_rule_"));
        mapper.insert(entity);
        return entity;
    }

    public RmCheckRule update(RmCheckRule entity) {
        duplicateCheck(entity);
        tableCheck(entity);
        Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        Assert.notNull(entity.getId(), ResultCode.UPDATE_NO_VERSION.getMessage());
        RmCheckRule oldEntity = selectById(entity.getId());
        if (oldEntity == null) {
            throw ValidationException.of(ResultCode.RECORD_NOT_EXIST);
        }
        entity.setRuleCode(null);
        RmCheckRule.copyIfNotNull(entity, oldEntity);
        updateByIdSelective(oldEntity);
        return oldEntity;
    }


    public Integer customRemove(RmCheckRule rmCheckRule) {
        if (rmCheckRule == null || rmCheckRule.getId() == null) {
            throw ValidationException.of(ResultCode.PARAM_NO_ID);
        }
        RmCheckRule rule = mapper.selectById(rmCheckRule.getId());
        if(rule == null) {
            throw ValidationException.of(ResultCode.RECORD_NOT_EXIST);
        }

        rmCheckApi.check(RmCheckRule.class, rule);
        return deleteById(rmCheckRule);
    }




    private void duplicateCheck(RmCheckRule entity) {
        Assert.notNull(entity.getTableName(), "tableName 不能为空!");

        // 唯一条件不为空，请设置唯一条件
        RmCheckRule param = new RmCheckRule();
        // 唯一条件
        param.setTableName(entity.getTableName());
        param.setColumnName(entity.getColumnName());
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


    private void tableCheck(RmCheckRule entity) {
        // 检查是否存在
        TableInfo infoParam = new TableInfo();
        infoParam.setTableName(entity.getTableName());
        List<TableInfo> tables = tableInfoService.getTables(infoParam);
        if (CollectionUtils.isEmpty(tables)) {
            throw ValidationException.of("表不存在！");
        }
    }

}

