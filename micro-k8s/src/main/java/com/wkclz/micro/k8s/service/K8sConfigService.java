package com.wkclz.micro.k8s.service;

import com.wkclz.core.enums.ResultCode;
import com.wkclz.core.exception.UserException;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.k8s.mapper.K8sConfigMapper;
import com.wkclz.micro.k8s.bean.entity.K8sConfig;
import com.wkclz.mybatis.service.BaseService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

/**
 * Description Create by sh-generator
 * @author shrimp
 * @table k8s_config (k8s配置) 单表服务类，代码重新生成不覆盖. 只建议完成单表的逻辑，或主表为 k8s_config 的逻辑. 其他逻辑放 custom 中
 */
 
@Service
public class K8sConfigService extends BaseService<K8sConfig, K8sConfigMapper> {

    public List<String> getClusterOptions(){
        return mapper.getClusterOptions();
    }

    public K8sConfig create(K8sConfig entity) {
        duplicateCheck(entity);
        mapper.insert(entity);
        return entity;
    }

    public K8sConfig update(K8sConfig entity) {
        duplicateCheck(entity);
        Assert.notNull(entity.getId(), "请求错误！参数[id]不能为空");
        Assert.notNull(entity.getVersion(), "请求错误！参数[version]不能为空");
        K8sConfig oldEntity = selectById(entity.getId());
        if (oldEntity == null) {
            throw ValidationException.of(ResultCode.RECORD_NOT_EXIST);
        }
        K8sConfig.copyIfNotNull(entity, oldEntity);
        updateByIdSelective(oldEntity);
        return oldEntity;
    }

    public K8sConfig save(K8sConfig entity) {
        return entity.getId() == null ? create(entity) : update(entity);
    }

    public K8sConfig remove(K8sConfig entity) {
        Assert.notNull(entity.getId(), "请求错误！参数[id]不能为空");
        K8sConfig oldEntity = selectById(entity.getId());
        if (oldEntity == null) {
            throw ValidationException.of(ResultCode.RECORD_NOT_EXIST);
        }
        deleteById(oldEntity);
        return oldEntity;
    }

    private void duplicateCheck(K8sConfig entity) {
        // 唯一条件为空，直接通过
        if (StringUtils.isBlank(entity.getClusterName())) {
            return;
        }
        
        // 唯一条件不为空，请设置唯一条件
        K8sConfig param = new K8sConfig();
        param.setClusterName(entity.getClusterName());
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

