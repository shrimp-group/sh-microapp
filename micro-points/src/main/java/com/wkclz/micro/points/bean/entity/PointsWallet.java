package com.wkclz.micro.points.bean.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.wkclz.core.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 积分钱包
 * @table points_wallet (积分钱包)
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PointsWallet extends BaseEntity {

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
     * 可用积分
     */
    @Schema(description = "可用积分")
    private Integer availablePoints;

    /**
     * 冻结积分
     */
    @Schema(description = "冻结积分")
    private Integer frozenPoints;

    /**
     * 历史总获得积分
     */
    @Schema(description = "历史总获得积分")
    private Integer totalEarnedPoints;


    public static PointsWallet copy(PointsWallet source, PointsWallet target) {
        if (target == null) { target = new PointsWallet(); }
        if (source == null) { return target; }
        target.setId(source.getId());
        target.setTenantCode(source.getTenantCode());
        target.setUserCode(source.getUserCode());
        target.setAvailablePoints(source.getAvailablePoints());
        target.setFrozenPoints(source.getFrozenPoints());
        target.setTotalEarnedPoints(source.getTotalEarnedPoints());
        target.setSort(source.getSort());
        target.setCreateTime(source.getCreateTime());
        target.setCreateBy(source.getCreateBy());
        target.setUpdateTime(source.getUpdateTime());
        target.setUpdateBy(source.getUpdateBy());
        target.setRemark(source.getRemark());
        target.setVersion(source.getVersion());
        return target;
    }

    public static PointsWallet copyIfNotNull(PointsWallet source, PointsWallet target) {
        if (target == null) { target = new PointsWallet(); }
        if (source == null) { return target; }
        if (source.getId() != null) { target.setId(source.getId()); }
        if (source.getTenantCode() != null) { target.setTenantCode(source.getTenantCode()); }
        if (source.getUserCode() != null) { target.setUserCode(source.getUserCode()); }
        if (source.getAvailablePoints() != null) { target.setAvailablePoints(source.getAvailablePoints()); }
        if (source.getFrozenPoints() != null) { target.setFrozenPoints(source.getFrozenPoints()); }
        if (source.getTotalEarnedPoints() != null) { target.setTotalEarnedPoints(source.getTotalEarnedPoints()); }
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
