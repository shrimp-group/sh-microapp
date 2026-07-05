package com.wkclz.micro.points.mapper;

import com.wkclz.micro.points.bean.entity.PointsConsumeRecord;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 积分消费流水 Mapper
 * @table points_consume_record (积分消费流水)
 * @author sh-microapp
 */
@Mapper
public interface PointsConsumeRecordMapper extends BaseMapper<PointsConsumeRecord> {

    /**
     * 按 order_no 查单条消费记录（用于回退校验）
     */
    PointsConsumeRecord selectByOrderNo(@Param("tenantCode") String tenantCode, @Param("orderNo") String orderNo);

    /**
     * 按 user_code 查消费流水，可选时间范围过滤（用于对账遍历）
     * 按 consume_time 倒序返回
     */
    List<PointsConsumeRecord> selectByUserCodeAndTimeRange(@Param("tenantCode") String tenantCode,
                                                          @Param("userCode") String userCode,
                                                          @Param("startTime") LocalDateTime startTime,
                                                          @Param("endTime") LocalDateTime endTime);

}
