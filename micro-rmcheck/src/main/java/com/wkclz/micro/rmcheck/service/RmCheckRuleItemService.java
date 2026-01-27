package com.wkclz.micro.rmcheck.service;

import com.wkclz.core.enums.ResultCode;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.rmcheck.dao.RmCheckRuleItemMapper;
import com.wkclz.micro.rmcheck.pojo.dto.RmCheckRuleItemDto;
import com.wkclz.micro.rmcheck.pojo.entity.RmCheckRuleItem;
import com.wkclz.mybatis.bean.ColumnQuery;
import com.wkclz.mybatis.config.ShMyBatisConfig;
import com.wkclz.mybatis.service.BaseService;
import com.wkclz.mybatis.service.TableInfoService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table rm_check_rule_item (删除检查规则-检查项) 单表服务类，代码重新生成不覆盖. 只建议完成单表的逻辑，或主表为 rm_check_rule_item 的逻辑. 其他逻辑放 custom 中
 */
 
@Service
public class RmCheckRuleItemService extends BaseService<RmCheckRuleItem, RmCheckRuleItemMapper> {

    @Autowired
    private ShMyBatisConfig config;
    @Autowired
    private TableInfoService tableInfoService;

    public List<RmCheckRuleItemDto> getRmCheckRuleItemList(RmCheckRuleItemDto dto) {
        dto.setTableSchema(config.getTableSchema());
        return mapper.getRmCheckRuleItem(dto);
    }

    public RmCheckRuleItem create(RmCheckRuleItem entity) {
        duplicateCheck(entity);
        columnCheck(entity);
        mapper.insert(entity);
        return entity;
    }

    public RmCheckRuleItem update(RmCheckRuleItem entity) {
        duplicateCheck(entity);
        columnCheck(entity);
        Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        Assert.notNull(entity.getId(), ResultCode.UPDATE_NO_VERSION.getMessage());
        RmCheckRuleItem oldEntity = selectById(entity.getId());
        if (oldEntity == null) {
            throw ValidationException.of(ResultCode.RECORD_NOT_EXIST);
        }
        RmCheckRuleItem.copyIfNotNull(entity, oldEntity);
        updateByIdSelective(oldEntity);
        return oldEntity;
    }

    private void duplicateCheck(RmCheckRuleItem entity) {
        Assert.notNull(entity.getRuleCode(), "ruleCode 不能为空!");
        Assert.notNull(entity.getCheckTableName(), "checkTableName 不能为空!");
        Assert.notNull(entity.getCheckColumnName(), "checkColumnName 不能为空!");

        // 唯一条件不为空，请设置唯一条件
        RmCheckRuleItem param = new RmCheckRuleItem();
        // 唯一条件
        param.setRuleCode(entity.getRuleCode());
        param.setCheckTableName(entity.getCheckTableName());
        param.setCheckColumnName(entity.getCheckColumnName());
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



    private void columnCheck(RmCheckRuleItem entity) {
        // 检查是否存在
        ColumnQuery infoParam = new ColumnQuery();
        infoParam.setTableName(entity.getCheckTableName());
        infoParam.setColumnName(entity.getCheckColumnName());
        List<ColumnQuery> columns = tableInfoService.getColumnInfos4Options(infoParam);
        if (CollectionUtils.isEmpty(columns)) {
            throw ValidationException.of("字段不存在！");
        }
    }

}

