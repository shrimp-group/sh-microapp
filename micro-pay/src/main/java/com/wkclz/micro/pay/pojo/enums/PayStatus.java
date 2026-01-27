package com.wkclz.micro.pay.pojo.enums;


import com.wkclz.core.annotation.Desc;

@Desc("支付状态")
public enum PayStatus {

    NEW("新建"),
    PAYING("支付中"),
    PAYERROR("支付失败"),
    ORDERNOTEXIST("订单不存在"),
    CLOSED("订单已关闭"),
    CANCEL("支付取消"),
    PAID("已支付"),
    REFUNDING("退款中"),
    REFUNDED("已退款"),
    FINISHED("已完成"),
    ;

    private String value;

    PayStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
