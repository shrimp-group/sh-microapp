package com.wkclz.micro.pay.bean.enums;


import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "支付方式")
public enum PayMethod {

    ALI_PAY("支付宝"),
    WX_PAY("微信"),
    UNION_PAY("银联"),

    MOCK_PAY("模拟支付");

    private String value;
    PayMethod(String value) { this.value = value; }
    public String getValue() { return value; }
}
