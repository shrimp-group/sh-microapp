package com.wkclz.micro.points.bean.entity;

import com.wkclz.core.annotation.FieldDesc;
import com.wkclz.core.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 积分扣减记录
 * 存放两类记录，通过 earn_flow_no 是否为 NULL 区分：
 * 任务记录（earn_flow_no = NULL）：消费时创建，status=PENDING
 * 动作记录（earn_flow_no 非空）：异步处理时为每次扣减创建，status=COMPLETED
 * @table points_deduction_record (积分扣减记录)
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PointsDeductionRecord extends BaseEntity {

    /**
     * 租户编码
     */
    @FieldDesc("租户编码")
    private String tenantCode;

    /**
     * 用户编码
     */
    @FieldDesc("用户编码")
    private String userCode;

    /**
     * 扣减流水号（系统生成，唯一标识）
     */
    @FieldDesc("扣减流水号")
    private String flowNo;

    /**
     * 关联消费单据号（= 消费流水的 order_no，用于溯源）
     */
    @FieldDesc("关联消费单据号")
    private String orderNo;

    /**
     * 积分获取流水号（任务记录为 NULL，动作记录指向 earn_record.flow_no）
     */
    @FieldDesc("积分获取流水号")
    private String earnFlowNo;

    /**
     * 扣减金额
     */
    @FieldDesc("扣减金额")
    private Integer deductionPoints;

    /**
     * 状态（PENDING 待处理 / PROCESSED 已处理 / COMPLETED 已完成 / PARTIAL 部分完成）
     */
    @FieldDesc("状态")
    private String status;


    public static PointsDeductionRecord copy(PointsDeductionRecord source, PointsDeductionRecord target) {
        if (target == null) { target = new PointsDeductionRecord(); }
        if (source == null) { return target; }
        target.setId(source.getId());
        target.setTenantCode(source.getTenantCode());
        target.setUserCode(source.getUserCode());
        target.setFlowNo(source.getFlowNo());
        target.setOrderNo(source.getOrderNo());
        target.setEarnFlowNo(source.getEarnFlowNo());
        target.setDeductionPoints(source.getDeductionPoints());
        target.setStatus(source.getStatus());
        target.setSort(source.getSort());
        target.setCreateTime(source.getCreateTime());
        target.setCreateBy(source.getCreateBy());
        target.setUpdateTime(source.getUpdateTime());
        target.setUpdateBy(source.getUpdateBy());
        target.setRemark(source.getRemark());
        target.setVersion(source.getVersion());
        return target;
    }

    public static PointsDeductionRecord copyIfNotNull(PointsDeductionRecord source, PointsDeductionRecord target) {
        if (target == null) { target = new PointsDeductionRecord(); }
        if (source == null) { return target; }
        if (source.getId() != null) { target.setId(source.getId()); }
        if (source.getTenantCode() != null) { target.setTenantCode(source.getTenantCode()); }
        if (source.getUserCode() != null) { target.setUserCode(source.getUserCode()); }
        if (source.getFlowNo() != null) { target.setFlowNo(source.getFlowNo()); }
        if (source.getOrderNo() != null) { target.setOrderNo(source.getOrderNo()); }
        if (source.getEarnFlowNo() != null) { target.setEarnFlowNo(source.getEarnFlowNo()); }
        if (source.getDeductionPoints() != null) { target.setDeductionPoints(source.getDeductionPoints()); }
        if (source.getStatus() != null) { target.setStatus(source.getStatus()); }
        if (source.getSort() != null) { target.setSort(source.getSort()); }
        if (source.getCreateTime() != null) { target.setCreateTime(source.getCreateTime()); }
        if (source.getCreateBy() != null) { target.setCreateBy(source.getCreateBy()); }
        if (source.getUpdateTime() != null) { target.setUpdateTime(source.getUpdateTime()); }
        if (source.getUpdateBy() != null) { target.setUpdateBy(source.getUpdateBy()); }
        if (source.getRemark() != null) { target.setRemark(source.getRemark()); }
        if (source.getVersion() != null) { target.setVersion(source.getVersion()); }
        return target;
    }

}
