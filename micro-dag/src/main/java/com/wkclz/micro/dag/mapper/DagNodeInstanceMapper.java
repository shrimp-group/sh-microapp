package com.wkclz.micro.dag.mapper;

import com.wkclz.micro.dag.bean.entity.DagNodeInstance;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface DagNodeInstanceMapper extends BaseMapper<DagNodeInstance> {

    /** CAS 领取：PENDING → RUNNING，attempt+1。影响行数=1 才代表领取成功 */
    int casClaim(@Param("id") Long id,
                 @Param("version") Integer version,
                 @Param("leaseOwner") String leaseOwner,
                 @Param("leaseExpireTime") LocalDateTime leaseExpireTime);

    /** RETRYING 且到期的提升为 PENDING */
    int promoteRetrying(@Param("now") LocalDateTime now);

    /** 租约过期的 RUNNING 重置为 PENDING（故障转移，乐观锁防原实例回写） */
    int reclaimExpiredLease(@Param("now") LocalDateTime now);

    /** 幂等辅助：仅当 trace_code 为空时回填（运行期生成的编码） */
    int backfillTraceCode(@Param("instanceCode") String instanceCode, @Param("traceCode") String traceCode);

    /** 批量清空中断标记（重跑前） */
    int clearInterruptFlag(@Param("instanceCode") String instanceCode);

    /** 按实例查询（deleted 过滤由拦截器处理） */
    List<DagNodeInstance> listByInstanceCode(@Param("instanceCode") String instanceCode);
}
