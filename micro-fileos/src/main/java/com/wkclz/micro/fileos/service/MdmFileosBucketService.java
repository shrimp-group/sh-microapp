package com.wkclz.micro.fileos.service;

import com.wkclz.core.base.PageData;
import com.wkclz.core.identity.IdentityContext;
import com.wkclz.micro.fileos.bean.entity.MdmFileosBucket;
import com.wkclz.micro.fileos.helper.BucketCache;
import com.wkclz.micro.fileos.mapper.MdmFileosBucketMapper;
import com.wkclz.mybatis.helper.PageQuery;
import com.wkclz.mybatis.service.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class MdmFileosBucketService extends BaseService<MdmFileosBucket, MdmFileosBucketMapper> {

    @Autowired
    private BucketCache bucketCache;

    public void clearCache() {
        bucketCache.clearCache();
    }


    public PageData<MdmFileosBucket> getBucketPage(MdmFileosBucket entity) {
        return PageQuery.page(entity, mapper::getBucketList);
    }

    @Transactional
    public void setDefaultFlag(MdmFileosBucket entity) {
        String tenantCode = entity.getTenantCode();
        if (tenantCode == null) {
            tenantCode = IdentityContext.getTenantCode();
        }

        MdmFileosBucket query = new MdmFileosBucket();
        query.setTenantCode(tenantCode);
        query.setDefaultFlag(1);
        List<MdmFileosBucket> defaultBuckets = selectByEntity(query);

        for (MdmFileosBucket bucket : defaultBuckets) {
            MdmFileosBucket update = new MdmFileosBucket();
            update.setId(bucket.getId());
            update.setDefaultFlag(0);
            update.setVersion(bucket.getVersion());
            updateByIdSelective(update);
        }

        log.info("micro-fileos: 重置租户 {} 的默认Bucket标识, 共重置 {} 项", tenantCode, defaultBuckets.size());
    }


    public List<MdmFileosBucket> getBucketOptions(MdmFileosBucket entity) {
        return mapper.getBucketOptions(entity);
    }

}
