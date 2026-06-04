package com.wkclz.micro.fileos.service;

import com.wkclz.core.base.PageData;
import com.wkclz.micro.fileos.bean.entity.MdmFileosRecord;
import com.wkclz.micro.fileos.mapper.MdmFileosRecordMapper;
import com.wkclz.mybatis.helper.PageQuery;
import com.wkclz.mybatis.service.BaseService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class MdmFileosRecordService extends BaseService<MdmFileosRecord, MdmFileosRecordMapper> {

    @Autowired
    private MdmFileosRecordMapper mapper;

    public PageData<MdmFileosRecord> getRecordPage(MdmFileosRecord entity) {
        return PageQuery.page(entity, mapper::getRecordList4Page);
    }

    public MdmFileosRecord getRecordByFileId(String fileId, String tenantCode) {
        if (StringUtils.isBlank(fileId)) {
            return null;
        }
        return mapper.getRecordByFileId(fileId, tenantCode);
    }

    public List<MdmFileosRecord> getRecordByFileIds(List<String> fileIds, String tenantCode) {
        if (CollectionUtils.isEmpty(fileIds)) {
            return Collections.emptyList();
        }
        return mapper.getRecordByFileIds(fileIds, tenantCode);
    }

    public MdmFileosRecord getRecordByFileHash(String fileHash, String tenantCode) {
        if (StringUtils.isBlank(fileHash)) {
            return null;
        }
        return mapper.getRecordByFileHash(fileHash, tenantCode);
    }

}
