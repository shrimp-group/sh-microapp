package com.wkclz.micro.points.bean.enums;

import com.wkclz.core.annotation.FieldDesc;

/**
 * 积分消费流水状态
 */
@FieldDesc("积分消费状态")
public enum PointsConsumeStatus {

    FROZEN("冻结"),
    DEDUCTED("已扣减"),
    CANCELLED("已取消"),
    ;

    private String value;

    PointsConsumeStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
