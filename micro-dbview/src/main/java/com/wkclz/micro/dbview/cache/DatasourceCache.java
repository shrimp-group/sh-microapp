package com.wkclz.micro.dbview.cache;

import com.wkclz.micro.dbview.bean.entity.DbviewDatasource;
import com.wkclz.micro.dbview.mapper.DbviewDatasourceMapper;
import com.wkclz.redis.helper.RedisHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class DatasourceCache {

    private static final String CACHE_PREFIX = "dbview:datasource:";

    @Autowired
    private RedisHelper redisHelper;

    @Autowired
    private DbviewDatasourceMapper datasourceMapper;

    @SuppressWarnings("unchecked")
    public DbviewDatasource getDatasource(Long datasourceId) {
        String cacheKey = CACHE_PREFIX + datasourceId;
        Object cached = redisHelper.get(cacheKey);
        if (cached != null) {
            return (DbviewDatasource) cached;
        }
        DbviewDatasource ds = datasourceMapper.selectById(datasourceId);
        if (ds != null) {
            redisHelper.set(cacheKey, ds, 300, TimeUnit.SECONDS);
        }
        return ds;
    }

    public void evict(Long datasourceId) {
        String cacheKey = CACHE_PREFIX + datasourceId;
        redisHelper.delete(cacheKey);
    }
}
