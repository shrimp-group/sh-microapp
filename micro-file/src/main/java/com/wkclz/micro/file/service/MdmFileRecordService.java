package com.wkclz.micro.file.service;

import com.wkclz.core.base.PageData;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.iam.sdk.helper.SessionHelper;
import com.wkclz.micro.file.mapper.MdmFileRecordMapper;
import com.wkclz.micro.file.pojo.entity.MdmFileRecord;
import com.wkclz.mybatis.helper.PageQuery;
import com.wkclz.mybatis.service.BaseService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Description Create by sh-generator
 * @author shrimp
 * @table mdm_file_record (附件) 单表服务类，代码重新生成不覆盖. 只建议完成单表的逻辑，或主表为 mdm_file_record 的逻辑. 其他逻辑放 custom 中
 */
 
@Service
public class MdmFileRecordService extends BaseService<MdmFileRecord, MdmFileRecordMapper> {


    public PageData<MdmFileRecord> getPage(MdmFileRecord entity) {
        entity.setTenantCode(SessionHelper.getTenantCode());
        return PageQuery.page(entity, mapper::getFileList4Page);
    }

    public MdmFileRecord getInfo(MdmFileRecord entity) {
        entity.setTenantCode(SessionHelper.getTenantCode());
        MdmFileRecord mdmFsUpload = selectOneByEntity(entity);
        if (mdmFsUpload == null) {
            throw ValidationException.of("上传的附件 不存在或无权操作");
        }
        return mdmFsUpload;
    }

    public MdmFileRecord getFilesByFileId(String fileId) {
        if (StringUtils.isBlank(fileId)) {
            throw ValidationException.of("fileId 不能为空");
        }
        return mapper.getFilesByFileId(fileId);
    }
    public List<MdmFileRecord> getFilesByFileIds(List<String> fileIds) {
        if (CollectionUtils.isEmpty(fileIds)) {
            throw ValidationException.of("fileId 不能为空");
        }
        return mapper.getFilesByFileIds(fileIds);
    }

    public Integer remove(MdmFileRecord entity) {
        entity.setTenantCode(SessionHelper.getTenantCode());
        Long count = selectCountByEntity(entity);
        if (count == 0) {
            throw ValidationException.of("上传的附件 不存在或无权操作");
        }

        // TODO 还需要对应删除 file system 内的文件

        return deleteById(entity);
    }


}

