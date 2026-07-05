package com.wkclz.micro.material.service;

import com.wkclz.core.base.PageData;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.iam.sdk.helper.SessionHelper;
import com.wkclz.micro.fileos.api.FileosDeleteApi;
import com.wkclz.micro.material.mapper.MdmMaterialMapper;
import com.wkclz.micro.material.mapper.MdmMaterialRefMapper;
import com.wkclz.micro.material.mapper.MdmMaterialVersionMapper;
import com.wkclz.micro.material.bean.entity.MdmMaterial;
import com.wkclz.micro.material.bean.entity.MdmMaterialVersion;
import com.wkclz.micro.material.bean.req.MaterialBatchCreateReq;
import com.wkclz.mybatis.helper.PageQuery;
import com.wkclz.mybatis.service.BaseService;
import com.wkclz.redis.helper.RedisIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class MdmMaterialService extends BaseService<MdmMaterial, MdmMaterialMapper> {

    @Autowired
    private MdmMaterialMapper mapper;
    @Autowired
    private MdmMaterialRefMapper refMapper;
    @Autowired
    private MdmMaterialVersionMapper versionMapper;
    @Autowired
    private FileosDeleteApi fileosDeleteApi;
    @Autowired
    private RedisIdGenerator redisIdGenerator;

    public PageData<MdmMaterial> getPage(MdmMaterial entity) {
        entity.setTenantCode(SessionHelper.getTenantCode());
        entity.setUserCode(SessionHelper.getUserCode());
        return PageQuery.page(entity, mapper::getMaterialList4Page);
    }

    public MdmMaterial getInfo(Long id) {
        MdmMaterial entity = selectById(id);
        if (entity == null) {
            throw ValidationException.of("素材不存在或无权操作");
        }
        checkViewPermission(entity);
        return entity;
    }

    @Transactional(rollbackFor = Exception.class)
    public MdmMaterial create(String fileId, String fileName, Long fileSize, String materialName, String materialType, String groupCode, String visibility, String description) {
        String tenantCode = SessionHelper.getTenantCode();
        String userCode = SessionHelper.getUserCode();

        MdmMaterial material = new MdmMaterial();
        material.setMaterialCode(String.valueOf(redisIdGenerator.generateIdWithPrefix("m_")));
        material.setMaterialName(materialName);
        material.setMaterialType(materialType);
        material.setSourceType("UPLOAD");
        material.setGroupCode(groupCode);
        material.setVisibility(visibility);
        material.setDescription(description);
        material.setTenantCode(tenantCode);
        material.setUserCode(userCode);

        if (StringUtils.isNotBlank(fileId)) {
            log.info("素材创建关联文件, fileId={}, fileName={}, fileSize={}", fileId, fileName, fileSize);
            material.setFileId(fileId);
            material.setFileName(fileName);
            material.setFileSize(fileSize);
        }

        insert(material);
        return material;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<MdmMaterial> batchCreate(MaterialBatchCreateReq req) {
        List<MaterialBatchCreateReq.MaterialCreateItem> items = req.getItems();
        if (CollectionUtils.isEmpty(items)) {
            throw ValidationException.of("素材列表不能为空");
        }

        List<MdmMaterial> results = new ArrayList<>();
        for (MaterialBatchCreateReq.MaterialCreateItem item : items) {
            try {
                MdmMaterial material = create(item.getFileId(), item.getFileName(), item.getFileSize(),
                        item.getFileName(), req.getMaterialType(), req.getGroupCode(), req.getVisibility(), null);
                results.add(material);
            } catch (Exception e) {
                log.warn("批量创建素材失败, fileName={}, error={}", item.getFileName(), e.getMessage());
                MdmMaterial failed = new MdmMaterial();
                failed.setFileName(item.getFileName());
                results.add(failed);
            }
        }
        return results;
    }

    @Transactional(rollbackFor = Exception.class)
    public MdmMaterial linkCreate(String materialName, String materialType, String linkUrl, String groupCode, String visibility, String description) {
        String tenantCode = SessionHelper.getTenantCode();
        String userCode = SessionHelper.getUserCode();

        String linkStatus = checkLinkStatus(linkUrl);

        MdmMaterial material = new MdmMaterial();
        material.setMaterialCode(String.valueOf(redisIdGenerator.generateIdWithPrefix("m_")));
        material.setMaterialName(materialName);
        material.setMaterialType(materialType);
        material.setSourceType("LINK");
        material.setLinkUrl(linkUrl);
        material.setLinkStatus(linkStatus);
        material.setGroupCode(groupCode);
        material.setVisibility(visibility);
        material.setDescription(description);
        material.setTenantCode(tenantCode);
        material.setUserCode(userCode);

        insert(material);
        return material;
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer update(MdmMaterial entity) {
        MdmMaterial existing = selectById(entity.getId());
        if (existing == null) {
            throw ValidationException.of("素材不存在或无权操作");
        }
        checkEditPermission(existing);
        entity.setTenantCode(SessionHelper.getTenantCode());
        return updateByIdSelective(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer remove(List<Long> ids) {
        List<MdmMaterial> materials = selectByIds(ids);
        if (CollectionUtils.isEmpty(materials)) {
            throw ValidationException.of("素材不存在或无权操作");
        }
        for (MdmMaterial material : materials) {
            checkEditPermission(material);
        }
        for (MdmMaterial material : materials) {
            if ("UPLOAD".equals(material.getSourceType()) && StringUtils.isNotBlank(material.getFileId())) {
                try {
                    fileosDeleteApi.delete(material.getFileId());
                } catch (Exception ignored) {
                    log.warn("素材删除关联文件失败, fileId={}", material.getFileId());
                }
            }
        }
        MdmMaterial param = new MdmMaterial();
        param.setIds(ids);
        return deleteByIdsEntity(param);
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer restore(List<Long> ids) {
        List<MdmMaterial> materials = selectByIds(ids);
        int count = 0;
        for (MdmMaterial material : materials) {
            MdmMaterial update = new MdmMaterial();
            update.setId(material.getId());
            update.setVersion(material.getVersion());
            count += updateByIdSelective(update);
        }
        return count;
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer move(List<Long> ids, String groupCode) {
        MdmMaterial update = new MdmMaterial();
        update.setGroupCode(groupCode);
        update.setIds(ids);
        return updateBatch(update);
    }

    @Transactional(rollbackFor = Exception.class)
    public MdmMaterial replaceFile(Long id, Integer version, String fileId, String fileName, Long fileSize) {
        MdmMaterial existing = selectById(id);
        if (existing == null) {
            throw ValidationException.of("素材不存在或无权操作");
        }
        checkEditPermission(existing);

        MdmMaterialVersion versionRecord = new MdmMaterialVersion();
        versionRecord.setMaterialCode(existing.getMaterialCode());
        versionRecord.setVersionNo(getNextVersionNo(existing.getMaterialCode()));
        versionRecord.setFileId(existing.getFileId());
        versionRecord.setFileName(existing.getFileName());
        versionRecord.setFileSize(existing.getFileSize());
        versionRecord.setTenantCode(SessionHelper.getTenantCode());
        versionRecord.setUserCode(SessionHelper.getUserCode());
        versionMapper.insert(versionRecord);

        log.info("素材替换文件, materialCode={}, newFileId={}", existing.getMaterialCode(), fileId);
        if (StringUtils.isNotBlank(existing.getFileId())) {
            try {
                fileosDeleteApi.delete(existing.getFileId());
            } catch (Exception ignored) {
                log.warn("素材替换文件时删除旧文件失败, fileId={}", existing.getFileId());
            }
        }

        MdmMaterial update = new MdmMaterial();
        update.setId(existing.getId());
        update.setVersion(existing.getVersion());
        update.setFileId(fileId);
        update.setFileName(fileName);
        update.setFileSize(fileSize);
        updateByIdSelective(update);

        cleanupOldVersions(existing.getMaterialCode());

        return selectById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer updateVisibility(List<Long> ids, String visibility) {
        MdmMaterial update = new MdmMaterial();
        update.setVisibility(visibility);
        update.setIds(ids);
        return updateBatch(update);
    }

    public String checkLink(String linkUrl) {
        return checkLinkStatus(linkUrl);
    }

    public PageData<MdmMaterial> getHotPage(MdmMaterial entity) {
        entity.setTenantCode(SessionHelper.getTenantCode());
        return PageQuery.page(entity, mapper::getHotMaterialList);
    }

    public PageData<MdmMaterial> getPickerPage(MdmMaterial entity) {
        entity.setTenantCode(SessionHelper.getTenantCode());
        entity.setUserCode(SessionHelper.getUserCode());
        return PageQuery.page(entity, mapper::getPickerMaterialList);
    }

    private void checkViewPermission(MdmMaterial material) {
        if ("PRIVATE".equals(material.getVisibility())) {
            String currentUser = SessionHelper.getUserCode();
            if (!material.getUserCode().equals(currentUser)) {
                throw ValidationException.of("素材不存在或无权操作");
            }
        }
    }

    private void checkEditPermission(MdmMaterial material) {
        String currentUser = SessionHelper.getUserCode();
        if (!material.getUserCode().equals(currentUser)) {
            throw ValidationException.of("无权操作该素材");
        }
    }

    private String checkLinkStatus(String linkUrl) {
        try {
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) new java.net.URL(linkUrl).openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            int code = connection.getResponseCode();
            return (code >= 200 && code < 400) ? "VALID" : "INVALID";
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }

    private Integer getNextVersionNo(String materialCode) {
        List<MdmMaterialVersion> versions = versionMapper.getByMaterialCode(materialCode, SessionHelper.getTenantCode());
        if (CollectionUtils.isEmpty(versions)) {
            return 1;
        }
        return versions.stream().mapToInt(MdmMaterialVersion::getVersionNo).max().orElse(0) + 1;
    }

    private void cleanupOldVersions(String materialCode) {
        int keepCount = 10;
        versionMapper.deleteOldestVersions(materialCode, SessionHelper.getTenantCode(), keepCount);
    }
}
