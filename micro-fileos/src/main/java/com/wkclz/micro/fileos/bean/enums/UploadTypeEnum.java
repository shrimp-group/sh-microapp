package com.wkclz.micro.fileos.bean.enums;

public enum UploadTypeEnum {

    SIMPLE("简单上传"),
    MULTIPART("分片上传"),
    PRESIGN("预签名上传"),

    ;

    private final String code;
    private final String desc;

    UploadTypeEnum(String desc) {
        this.code = this.name();
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

}
