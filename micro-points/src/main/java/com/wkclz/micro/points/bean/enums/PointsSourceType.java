package com.wkclz.micro.points.bean.enums;

import com.wkclz.core.annotation.FieldDesc;

/**
 * 积分来源类型
 */
@FieldDesc("积分来源类型")
public enum PointsSourceType {

    ISSUANCE("发放"),
    REFUND("回退"),
    ADMIN_ISSUE("管理员手动发放"),
    ;

    private String value;

    PointsSourceType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
