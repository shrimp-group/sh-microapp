package com.wkclz.micro.fileos.service;

import com.wkclz.micro.fileos.bean.entity.MdmFileosMultipart;
import com.wkclz.micro.fileos.mapper.MdmFileosMultipartMapper;
import com.wkclz.mybatis.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class MdmFileosMultipartService extends BaseService<MdmFileosMultipart, MdmFileosMultipartMapper> {

    @Autowired
    private MdmFileosMultipartMapper mapper;

    public List<MdmFileosMultipart> getExpiredMultipartList() {
        return mapper.getExpiredMultipartList(new Date());
    }

    public Integer updateMultipartFileStatus(MdmFileosMultipart entity) {
        return mapper.updateMultipartFileStatus(entity);
    }



}
