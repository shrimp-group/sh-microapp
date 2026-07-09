package com.wkclz.micro.material.service;

import com.wkclz.iam.contract.context.PrincipalContext;
import com.wkclz.micro.material.mapper.MdmMaterialTransferLogMapper;
import com.wkclz.micro.material.mapper.MdmMaterialMapper;
import com.wkclz.micro.material.bean.entity.MdmMaterial;
import com.wkclz.micro.material.bean.entity.MdmMaterialTransferLog;
import com.wkclz.mybatis.service.BaseService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MdmMaterialTransferLogService extends BaseService<MdmMaterialTransferLog, MdmMaterialTransferLogMapper> {

    @Autowired
    private MdmMaterialMapper materialMapper;
    @Autowired
    private MdmMaterialTransferLogMapper mapper;

    @Transactional(rollbackFor = Exception.class)
    public Integer transfer(List<Long> ids, String toUserCode) {
        String userCode = PrincipalContext.getUserCode();
        String tenantCode = PrincipalContext.getTenantCode();

        List<MdmMaterial> materials = materialMapper.selectByIds(ids);
        if (CollectionUtils.isEmpty(materials)) {
            return 0;
        }

        int count = 0;
        for (MdmMaterial material : materials) {
            MdmMaterial update = new MdmMaterial();
            update.setId(material.getId());
            update.setVersion(material.getVersion());
            update.setUserCode(toUserCode);
            materialMapper.updateByIdSelective(update);

            MdmMaterialTransferLog log = new MdmMaterialTransferLog();
            log.setMaterialCode(material.getMaterialCode());
            log.setFromUserCode(material.getUserCode());
            log.setToUserCode(toUserCode);
            log.setOperatorCode(userCode);
            log.setTenantCode(tenantCode);
            log.setUserCode(userCode);
            insert(log);
            count++;
        }
        return count;
    }

    public List<MdmMaterialTransferLog> listByMaterialCode(String materialCode) {
        return mapper.getByMaterialCode(materialCode, PrincipalContext.getTenantCode());
    }
}
