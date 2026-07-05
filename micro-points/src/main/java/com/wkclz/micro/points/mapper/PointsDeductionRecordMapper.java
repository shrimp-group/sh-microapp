package com.wkclz.micro.points.mapper;

import com.wkclz.micro.points.bean.entity.PointsDeductionRecord;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 积分扣减记录 Mapper
 * 存放两类记录，通过 earn_flow_no 是否为 NULL 区分：
 * 任务记录（earn_flow_no = NULL）：消费时创建，status=PENDING
 * 动作记录（earn_flow_no 非空）：异步处理时为每次扣减创建，status=COMPLETED
 * @table points_deduction_record (积分扣减记录)
 * @author sh-microapp
 */
@Mapper
public interface PointsDeductionRecordMapper extends BaseMapper<PointsDeductionRecord> {

    /**
     * 批量查询 PENDING 任务记录（earn_flow_no IS NULL AND status='PENDING'）
     * 按 user_code 排序便于分组
     */
    List<PointsDeductionRecord> selectPendingTaskRecords(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * 按 order_no 聚合 COMPLETED 动作记录（earn_flow_no IS NOT NULL AND status='COMPLETED'）的 deduction_points 之和
     * 用于回退校验的 total_deducted 计算
     */
    Integer sumCompletedDeductionPointsByOrderNo(@Param("tenantCode") String tenantCode, @Param("orderNo") String orderNo);

    /**
     * 按 order_no 查所有 COMPLETED 动作记录（earn_flow_no IS NOT NULL AND status='COMPLETED'）
     * 用于对账与运营端扣减明细查询
     */
    List<PointsDeductionRecord> selectCompletedActionsByOrderNo(@Param("tenantCode") String tenantCode, @Param("orderNo") String orderNo);

    /**
     * 按 order_no 查任务记录（earn_flow_no IS NULL），返回最新一条（ORDER BY id DESC LIMIT 1）
     * 用于对账时判断 PENDING / PARTIAL / PROCESSED 任务状态
     */
    PointsDeductionRecord selectTaskRecordByOrderNo(@Param("tenantCode") String tenantCode, @Param("orderNo") String orderNo);

    /**
     * 乐观锁更新状态
     */
    int updateStatusByVersion(@Param("id") Long id, @Param("status") String status, @Param("version") Integer version);

}
