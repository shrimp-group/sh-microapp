package com.wkclz.micro.form.bean.enums;

public enum ValidatorTypeEnum {

    REQUIRED( "必填"),

    INTEGER_GT( "整数_大于"),
    INTEGER_GE( "整数_大于等于"),
    INTEGER_LT( "整数_小于"),
    INTEGER_LE( "整数_小于等于"),

    FLOAT_GT( "浮点数_大于"),
    FLOAT_GE( "浮点数_大于等于"),
    FLOAT_LT( "浮点数_小于"),
    FLOAT_LE( "浮点数_小于等于"),

    STRING_GT( "字符串长度_大于"),
    STRING_GE( "字符串长度_大于等于"),
    STRING_LT( "字符串长度_小于"),
    STRING_LE( "字符串长度_小于等于"),

    DATE("日期: yyyy-MM-dd"),
    DATETIME("日期时间: yyyy-MM-ddTHH:mm:ss"),
    TIME("时间: HH:mm:ss"),

    EMAIL("邮箱"),
    MOBILE("手机事情"),
    URL("URL地址"),
    DOMAIN("域名"),
    IP("IP"),
    ID_CARD("身份证号"),

    JSON("JSON"),

    DIY("自定义"),
    TEMPLATE("模板"),

    ;


    private String desc;

    ValidatorTypeEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

}
