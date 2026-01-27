package com.wkclz.micro.pay.pojo.enums;


import com.wkclz.core.annotation.Desc;

@Desc("终端类型")
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
