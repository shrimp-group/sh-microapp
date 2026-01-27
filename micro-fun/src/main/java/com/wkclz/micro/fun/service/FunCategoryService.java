package com.wkclz.micro.fun.service;

import com.wkclz.core.enums.ResultCode;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.fun.dao.FunCategoryMapper;
import com.wkclz.micro.fun.dao.FunFunctionMapper;
import com.wkclz.micro.fun.pojo.dto.FunCategoryDto;
import com.wkclz.micro.fun.pojo.entity.FunCategory;
import com.wkclz.micro.fun.pojo.entity.FunFunction;
import com.wkclz.mybatis.service.BaseService;
import com.wkclz.redis.helper.RedisIdGenerator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table fun_category (函数-分类) 单表服务类，代码重新生成不覆盖. 只建议完成单表的逻辑，或主表为 fun_category 的逻辑. 其他逻辑放 custom 中
 */
 
@Service
public class FunCategoryService extends BaseService<FunCategory, FunCategoryMapper> {

    @Autowired
    private RedisIdGenerator redisIdGenerator;
    @Autowired
    private FunFunctionMapper funFunctionMapper;

    public List<FunCategory> getFunCategoryList(FunCategory entity) {
        return mapper.getFunCategoryList(entity);
    }
    public List<FunCategoryDto> getFunCategoryTree(FunCategory entity) {
        List<FunCategory> list = mapper.getFunCategoryList(entity);
        List<FunCategoryDto> dtos = list.stream().map(FunCategoryDto::copy).collect(Collectors.toList());
        return makeTree(dtos);
    }

    public List<FunCategoryDto> getFunCategoryOptions(FunCategory entity) {
        List<FunCategory> list = mapper.getFunCategoryOptions(entity);
        List<FunCategoryDto> dtos = list.stream().map(FunCategoryDto::copy).collect(Collectors.toList());
        return makeTree(dtos);
    }

    @Transactional(rollbackFor = Exception.class)
    public FunCategory create(FunCategory entity) {
        duplicateCheck(entity);
        entity.setCategoryCode(redisIdGenerator.generateIdWithPrefix("ctg_"));
        mapper.insert(entity);
        return entity;
    }

    @Transactional(rollbackFor = Exception.class)
    public FunCategory update(FunCategory entity) {
        duplicateCheck(entity);
        Assert.notNull(entity.getId(), ResultCode.PARAM_NO_ID.getMessage());
        Assert.notNull(entity.getId(), ResultCode.UPDATE_NO_VERSION.getMessage());
        FunCategory oldEntity = selectById(entity.getId());
        if (oldEntity == null) {
            throw ValidationException.of(ResultCode.RECORD_NOT_EXIST);
        }

        // 检查，自身，和自身的子集，不能作为自己的父级
        if (entity.getPcode() != null && !entity.getCategoryCode().equals(entity.getPcode())) {
            List<FunCategory> orgs = mapper.getFunCategoryList(new FunCategory());
            checPcode(entity.getPcode(), entity.getCategoryCode(), orgs);
        }



        FunCategory.copyIfNotNull(entity, oldEntity);
        updateByIdSelective(oldEntity);
        return oldEntity;
    }


    @Transactional(rollbackFor = Exception.class)
    public Integer customDelete(FunCategory entity) {
        entity = mapper.selectById(entity.getId());
        if (entity == null) {
            throw ValidationException.of("数据不存在, 或无权操作此数据!");
        }
        // 检查关联关系
        FunFunction p = new FunFunction();
        p.setCategoryCode(entity.getCategoryCode());
        List<FunFunction> list = funFunctionMapper.selectByEntity(p);
        if (CollectionUtils.isNotEmpty(list)) {
            List<String> collect = list.stream().map(FunFunction::getFunName).toList();
            throw ValidationException.of("此W分类已被如下函数关联，请移除关联后再删除: {}", collect);
        }
        return deleteById(entity);
    }



    private void duplicateCheck(FunCategory entity) {
        // 唯一条件为空，直接通过
        if (StringUtils.isBlank(entity.getCategoryCode())) {
            return;
        }
        
        // 唯一条件不为空，请设置唯一条件
        FunCategory param = new FunCategory();
        param.setCategoryCode(entity.getCategoryCode());
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




    private static List<FunCategoryDto> makeTree(List<FunCategoryDto> dtos) {
        List<FunCategoryDto> tree = new ArrayList<>();
        for (FunCategoryDto d : dtos) {
            if ("0".equals(d.getPcode())) {
                tree.add(d);
            } else {
                for (FunCategoryDto p : dtos) {
                    if (p.getCategoryCode().equals(d.getPcode())) {
                        List<FunCategoryDto> children = p.getChildren();
                        if (children == null) {
                            children = new ArrayList<>();
                        }
                        children.add(d);
                        p.setChildren(children);
                    }
                }
            }
        }
        return tree;
    }

    private void checPcode(String pcode, String code,  List<FunCategory> datas) {
        for (FunCategory ctg : datas) {
            if (ctg.getCategoryCode().equals(pcode)) {
                // 找到父级节点，检查是否是自身
                if (ctg.getCategoryCode().equals(code)) {
                    // 发现父级节点是自身
                    throw ValidationException.of("请不要选择自身，或自身的子节点作为新的父节点！");
                } else {
                    // 如果不是，继续找父级节点。直到顶级节点
                    checPcode(ctg.getPcode(), code, datas);
                }
                break;
            }
        }
    }


}

