package com.wkclz.micro.points.bean.enums;

import com.wkclz.core.annotation.FieldDesc;

/**
 * 积分扣减记录状态
 */
@FieldDesc("积分扣减状态")
public enum PointsDeductionStatus {

    PENDING("待处理"),
    PROCESSED("已处理"),
    COMPLETED("已完成"),
    PARTIAL("部分完成"),
    CANCELLED("已取消"),
    ;

    private String value;

    PointsDeductionStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
