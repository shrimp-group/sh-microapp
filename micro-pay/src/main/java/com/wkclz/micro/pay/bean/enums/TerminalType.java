package com.wkclz.micro.pay.bean.enums;


import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "终端类型")
public enum TerminalType {

    PC("PC"),
    H5("H5"),
    APP("应用"),
    WX("微信"),
    MINIAPP("小程序"),
    ;

    private String value;

    TerminalType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
