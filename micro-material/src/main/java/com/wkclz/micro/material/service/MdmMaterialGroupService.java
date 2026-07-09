package com.wkclz.micro.material.service;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.iam.contract.context.PrincipalContext;
import com.wkclz.micro.material.cache.MaterialGroupCache;
import com.wkclz.micro.material.mapper.MdmMaterialGroupMapper;
import com.wkclz.micro.material.mapper.MdmMaterialMapper;
import com.wkclz.micro.material.bean.entity.MdmMaterial;
import com.wkclz.micro.material.bean.entity.MdmMaterialGroup;
import com.wkclz.mybatis.service.BaseService;
import com.wkclz.redis.helper.RedisIdGenerator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MdmMaterialGroupService extends BaseService<MdmMaterialGroup, MdmMaterialGroupMapper> {

    private static final int MAX_DEPTH = 5;

    @Autowired
    private MdmMaterialGroupMapper mapper;
    @Autowired
    private MdmMaterialMapper materialMapper;
    @Autowired
    private MaterialGroupCache groupCache;
    @Autowired
    private RedisIdGenerator redisIdGenerator;

    public List<MdmMaterialGroup> getTree() {
        String tenantCode = PrincipalContext.getTenantCode();
        String userCode = PrincipalContext.getUserCode();
        return mapper.getGroupTree(tenantCode, userCode);
    }

    public List<MdmMaterialGroup> getPickerTree() {
        String tenantCode = PrincipalContext.getTenantCode();
        String userCode = PrincipalContext.getUserCode();
        return mapper.getPickerGroupTree(tenantCode, userCode);
    }

    @Transactional(rollbackFor = Exception.class)
    public MdmMaterialGroup create(MdmMaterialGroup entity) {
        String tenantCode = PrincipalContext.getTenantCode();
        String userCode = PrincipalContext.getUserCode();

        if (StringUtils.isBlank(entity.getParentCode()) || "0".equals(entity.getParentCode())) {
            entity.setParentCode("0");
        } else {
            checkDepth(entity.getParentCode(), 1);
        }

        entity.setGroupCode(String.valueOf(redisIdGenerator.generateIdWithPrefix("mg_")));
        entity.setTenantCode(tenantCode);
        entity.setUserCode(userCode);

        insert(entity);
        groupCache.clearCache();
        return entity;
    }

    @Transactional(rollbackFor = Exception.class)
    public MdmMaterialGroup update(MdmMaterialGroup entity) {
        MdmMaterialGroup existing = selectById(entity.getId());
        if (existing == null) {
            throw ValidationException.of("分组不存在");
        }
        updateByIdSelective(entity);
        groupCache.clearCache();
        return entity;
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer remove(MdmMaterialGroup entity) {
        MdmMaterialGroup existing = selectById(entity.getId());
        if (existing == null) {
            throw ValidationException.of("分组不存在");
        }

        List<MdmMaterialGroup> children = mapper.getChildGroupCodes(existing.getGroupCode(), PrincipalContext.getTenantCode());
        if (CollectionUtils.isNotEmpty(children)) {
            throw ValidationException.of("分组下存在子分组，无法删除");
        }

        MdmMaterial param = new MdmMaterial();
        param.setGroupCode(existing.getGroupCode());
        long count = selectCountByEntity(new MdmMaterialGroup());
        if (count > 0) {
            throw ValidationException.of("分组下存在素材，请先移动素材到其他分组");
        }

        Integer result = deleteById(entity);
        groupCache.clearCache();
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer move(Long id, Integer version, String parentCode) {
        MdmMaterialGroup existing = selectById(id);
        if (existing == null) {
            throw ValidationException.of("分组不存在");
        }

        if (!"0".equals(parentCode)) {
            if (parentCode.equals(existing.getGroupCode())) {
                throw ValidationException.of("不能将分组移动到自身下");
            }
            checkDepth(parentCode, getSubtreeDepth(existing.getGroupCode()) + 1);
        }

        MdmMaterialGroup update = new MdmMaterialGroup();
        update.setId(id);
        update.setVersion(version);
        update.setParentCode(parentCode);
        Integer result = updateByIdSelective(update);
        groupCache.clearCache();
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer sort(List<Long> ids) {
        int sortOrder = 0;
        for (Long id : ids) {
            MdmMaterialGroup update = new MdmMaterialGroup();
            update.setId(id);
            update.setSort(sortOrder++);
            updateByIdSelective(update);
        }
        groupCache.clearCache();
        return ids.size();
    }

    private void checkDepth(String parentCode, int currentDepth) {
        if (currentDepth >= MAX_DEPTH) {
            throw ValidationException.of("分组层级不能超过" + MAX_DEPTH + "级");
        }
        if ("0".equals(parentCode)) {
            return;
        }
        MdmMaterialGroup parent = findGroupByCode(parentCode);
        if (parent == null) {
            throw ValidationException.of("父级分组不存在");
        }
        checkDepth(parent.getParentCode(), currentDepth + 1);
    }

    private int getSubtreeDepth(String groupCode) {
        List<MdmMaterialGroup> children = mapper.getChildGroupCodes(groupCode, PrincipalContext.getTenantCode());
        if (CollectionUtils.isEmpty(children)) {
            return 1;
        }
        int maxChildDepth = 0;
        for (MdmMaterialGroup child : children) {
            maxChildDepth = Math.max(maxChildDepth, getSubtreeDepth(child.getGroupCode()));
        }
        return 1 + maxChildDepth;
    }

    private MdmMaterialGroup findGroupByCode(String groupCode) {
        String tenantCode = PrincipalContext.getTenantCode();
        MdmMaterialGroup param = new MdmMaterialGroup();
        param.setGroupCode(groupCode);
        param.setTenantCode(tenantCode);
        return selectOneByEntity(param);
    }
}
