package com.wkclz.micro.fileos.bean.enums;

public enum UploadStatusEnum {

    UPLOADING("上传中"),
    COMPLETED("已完成"),
    ABORTED("已中止"),

    ;

    private final String code;
    private final String desc;

    UploadStatusEnum(String desc) {
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
