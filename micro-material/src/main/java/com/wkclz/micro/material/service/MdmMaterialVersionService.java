package com.wkclz.micro.material.service;

import com.wkclz.core.identity.IdentityContext;
import com.wkclz.micro.material.bean.entity.MdmMaterialVersion;
import com.wkclz.micro.material.mapper.MdmMaterialVersionMapper;
import com.wkclz.mybatis.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MdmMaterialVersionService extends BaseService<MdmMaterialVersion, MdmMaterialVersionMapper> {

    @Autowired
    private MdmMaterialVersionMapper mapper;

    public List<MdmMaterialVersion> listByMaterialCode(String materialCode) {
        return mapper.getByMaterialCode(materialCode, IdentityContext.getTenantCode());
    }
}
