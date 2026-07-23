package com.wkclz.micro.points.bean.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.wkclz.core.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 积分获取流水
 * @table points_earn_record (积分获取流水)
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PointsEarnRecord extends BaseEntity {

    /**
     * 租户编码
     */
    @Schema(description = "租户编码")
    private String tenantCode;

    /**
     * 用户编码
     */
    @Schema(description = "用户编码")
    private String userCode;

    /**
     * 流水号（系统生成，唯一标识）
     */
    @Schema(description = "流水号")
    private String flowNo;

    /**
     * 获取时间
     */
    @Schema(description = "获取时间")
    private LocalDateTime earnTime;

    /**
     * 获取积分数
     */
    @Schema(description = "获取积分数")
    private Integer points;

    /**
     * 获取原因
     */
    @Schema(description = "获取原因")
    private String reason;

    /**
     * 到期时间（DB 默认 2099-12-31 23:59:59）
     */
    @Schema(description = "到期时间")
    private LocalDateTime expireTime;

    /**
     * 已使用积分数
     */
    @Schema(description = "已使用积分数")
    private Integer usedPoints;

    /**
     * 可用积分数
     */
    @Schema(description = "可用积分数")
    private Integer availablePoints;

    /**
     * 是否已使用完(0/1)
     */
    @Schema(description = "是否已使用完(0/1)")
    private Integer isUsedUp;

    /**
     * 来源类型（枚举 PointsSourceType：ISSUANCE 发放 / REFUND 回退 / ADMIN_ISSUE 管理员手动发放）
     */
    @Schema(description = "积分来源类型")
    private String pointSourceType;

    /**
     * 来源单据号（发放时为业务单据号；回退时为原消费单据号 order_no）
     */
    @Schema(description = "来源单据号")
    private String sourceNo;


    public static PointsEarnRecord copy(PointsEarnRecord source, PointsEarnRecord target) {
        if (target == null) { target = new PointsEarnRecord(); }
        if (source == null) { return target; }
        target.setId(source.getId());
        target.setTenantCode(source.getTenantCode());
        target.setUserCode(source.getUserCode());
        target.setFlowNo(source.getFlowNo());
        target.setEarnTime(source.getEarnTime());
        target.setPoints(source.getPoints());
        target.setReason(source.getReason());
        target.setExpireTime(source.getExpireTime());
        target.setUsedPoints(source.getUsedPoints());
        target.setAvailablePoints(source.getAvailablePoints());
        target.setIsUsedUp(source.getIsUsedUp());
        target.setPointSourceType(source.getPointSourceType());
        target.setSourceNo(source.getSourceNo());
        target.setSort(source.getSort());
        target.setCreateTime(source.getCreateTime());
        target.setCreateBy(source.getCreateBy());
        target.setUpdateTime(source.getUpdateTime());
        target.setUpdateBy(source.getUpdateBy());
        target.setRemark(source.getRemark());
        target.setVersion(source.getVersion());
        return target;
    }

    public static PointsEarnRecord copyIfNotNull(PointsEarnRecord source, PointsEarnRecord target) {
        if (target == null) { target = new PointsEarnRecord(); }
        if (source == null) { return target; }
        if (source.getId() != null) { target.setId(source.getId()); }
        if (source.getTenantCode() != null) { target.setTenantCode(source.getTenantCode()); }
        if (source.getUserCode() != null) { target.setUserCode(source.getUserCode()); }
        if (source.getFlowNo() != null) { target.setFlowNo(source.getFlowNo()); }
        if (source.getEarnTime() != null) { target.setEarnTime(source.getEarnTime()); }
        if (source.getPoints() != null) { target.setPoints(source.getPoints()); }
        if (source.getReason() != null) { target.setReason(source.getReason()); }
        if (source.getExpireTime() != null) { target.setExpireTime(source.getExpireTime()); }
        if (source.getUsedPoints() != null) { target.setUsedPoints(source.getUsedPoints()); }
        if (source.getAvailablePoints() != null) { target.setAvailablePoints(source.getAvailablePoints()); }
        if (source.getIsUsedUp() != null) { target.setIsUsedUp(source.getIsUsedUp()); }
        if (source.getPointSourceType() != null) { target.setPointSourceType(source.getPointSourceType()); }
        if (source.getSourceNo() != null) { target.setSourceNo(source.getSourceNo()); }
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
