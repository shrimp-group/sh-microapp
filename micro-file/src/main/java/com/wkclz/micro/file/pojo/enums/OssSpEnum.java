package com.wkclz.micro.file.pojo.enums;

public enum OssSpEnum {

    /**
     *
     */

    ALI_OSS("AliOssService", "ali oss"),
    AWS_S3("S3Service","aws s3"),
    MINIO("S3Service","minio"),

    ;

    private final String serviceName;
    private final String desc;

    OssSpEnum(String serviceName, String desc) {
        this.serviceName = serviceName;
        this.desc = desc;
    }

    public String getServiceName() {
        return serviceName;
    }
    public String getDesc() {
        return desc;
    }


}
