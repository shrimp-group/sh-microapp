package com.wkclz.micro.material.service;

import com.wkclz.iam.contract.context.PrincipalContext;
import com.wkclz.micro.material.mapper.MdmMaterialRefMapper;
import com.wkclz.micro.material.bean.entity.MdmMaterialRef;
import com.wkclz.mybatis.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MdmMaterialRefService extends BaseService<MdmMaterialRef, MdmMaterialRefMapper> {

    @Autowired
    private MdmMaterialRefMapper mapper;

    @Transactional(rollbackFor = Exception.class)
    public Integer bind(String materialCode, String bizType, String bizCode, String refDesc) {
        MdmMaterialRef ref = new MdmMaterialRef();
        ref.setMaterialCode(materialCode);
        ref.setBizType(bizType);
        ref.setBizCode(bizCode);
        ref.setRefDesc(refDesc);
        ref.setTenantCode(PrincipalContext.getTenantCode());
        ref.setUserCode(PrincipalContext.getUserCode());
        return insert(ref);
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer unbind(String materialCode, String bizType, String bizCode) {
        return mapper.deleteByBiz(materialCode, bizType, bizCode, PrincipalContext.getTenantCode());
    }

    public List<MdmMaterialRef> listByMaterialCode(String materialCode) {
        return mapper.getByMaterialCode(materialCode, PrincipalContext.getTenantCode());
    }

    public Map<String, Object> check(String materialCode) {
        Long count = mapper.countByMaterialCode(materialCode, PrincipalContext.getTenantCode());
        Map<String, Object> result = new HashMap<>();
        result.put("referenced", count != null && count > 0);
        result.put("count", count != null ? count : 0);
        return result;
    }
}
