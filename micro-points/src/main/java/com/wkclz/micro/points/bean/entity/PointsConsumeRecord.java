package com.wkclz.micro.points.bean.entity;

import com.wkclz.core.annotation.FieldDesc;
import com.wkclz.core.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 积分消费流水
 * @table points_consume_record (积分消费流水)
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PointsConsumeRecord extends BaseEntity {

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
     * 流水号（系统生成，唯一标识）
     */
    @FieldDesc("流水号")
    private String flowNo;

    /**
     * 使用时间
     */
    @FieldDesc("使用时间")
    private LocalDateTime consumeTime;

    /**
     * 使用积分数
     */
    @FieldDesc("使用积分数")
    private Integer points;

    /**
     * 使用原因
     */
    @FieldDesc("使用原因")
    private String reason;

    /**
     * 关联单据号（业务单据，唯一）
     */
    @FieldDesc("关联单据号")
    private String orderNo;

    /**
     * 状态（FROZEN 冻结 / DEDUCTED 已扣减）
     */
    @FieldDesc("状态")
    private String status;


    public static PointsConsumeRecord copy(PointsConsumeRecord source, PointsConsumeRecord target) {
        if (target == null) { target = new PointsConsumeRecord(); }
        if (source == null) { return target; }
        target.setId(source.getId());
        target.setTenantCode(source.getTenantCode());
        target.setUserCode(source.getUserCode());
        target.setFlowNo(source.getFlowNo());
        target.setConsumeTime(source.getConsumeTime());
        target.setPoints(source.getPoints());
        target.setReason(source.getReason());
        target.setOrderNo(source.getOrderNo());
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

    public static PointsConsumeRecord copyIfNotNull(PointsConsumeRecord source, PointsConsumeRecord target) {
        if (target == null) { target = new PointsConsumeRecord(); }
        if (source == null) { return target; }
        if (source.getId() != null) { target.setId(source.getId()); }
        if (source.getTenantCode() != null) { target.setTenantCode(source.getTenantCode()); }
        if (source.getUserCode() != null) { target.setUserCode(source.getUserCode()); }
        if (source.getFlowNo() != null) { target.setFlowNo(source.getFlowNo()); }
        if (source.getConsumeTime() != null) { target.setConsumeTime(source.getConsumeTime()); }
        if (source.getPoints() != null) { target.setPoints(source.getPoints()); }
        if (source.getReason() != null) { target.setReason(source.getReason()); }
        if (source.getOrderNo() != null) { target.setOrderNo(source.getOrderNo()); }
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
