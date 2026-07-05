package com.wkclz.micro.material.api;

import com.wkclz.micro.material.service.MdmMaterialRefService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MaterialApiImpl implements MaterialApi {

    @Autowired
    private MdmMaterialRefService mdmMaterialRefService;

    @Override
    public Integer bind(String materialCode, String bizType, String bizCode, String refDesc) {
        return mdmMaterialRefService.bind(materialCode, bizType, bizCode, refDesc);
    }

    @Override
    public Integer unbind(String materialCode, String bizType, String bizCode) {
        return mdmMaterialRefService.unbind(materialCode, bizType, bizCode);
    }

    @Override
    public Map<String, Object> check(String materialCode) {
        return mdmMaterialRefService.check(materialCode);
    }
}
