package com.wkclz.micro.points.mapper;

import com.wkclz.micro.points.bean.entity.PointsEarnRecord;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 积分获取流水 Mapper
 * @table points_earn_record (积分获取流水)
 * @author sh-microapp
 */
@Mapper
public interface PointsEarnRecordMapper extends BaseMapper<PointsEarnRecord> {

    /**
     * 按 user_code + expire_time ASC 批量查询可用获取流水（available_points > 0），支持分页
     * 用于异步扣减的指数退避批量拉取
     */
    List<PointsEarnRecord> selectAvailableBatchByExpireTime(@Param("tenantCode") String tenantCode,
                                                            @Param("userCode") String userCode,
                                                            @Param("limit") int limit,
                                                            @Param("offset") int offset);

    /**
     * 按 source_no 聚合 REFUND 获取流水的 points 之和
     * 用于回退校验的 already_refunded 计算
     */
    Integer sumRefundPointsBySourceNo(@Param("tenantCode") String tenantCode, @Param("sourceNo") String sourceNo);

    /**
     * 扫描过期且可用的获取流水（expire_time < #{expireTime} AND available_points > 0）
     * 用于过期定时任务
     */
    List<PointsEarnRecord> selectExpiredAvailable(@Param("expireTime") LocalDateTime expireTime,
                                                   @Param("offset") int offset,
                                                   @Param("limit") int limit);

    /**
     * 批量更新获取流水的 used_points/available_points/is_used_up
     */
    int batchUpdateUsedPoints(@Param("list") List<PointsEarnRecord> list);

}
