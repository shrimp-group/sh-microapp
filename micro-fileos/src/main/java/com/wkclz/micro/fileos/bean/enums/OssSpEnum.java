package com.wkclz.micro.fileos.bean.enums;

public enum OssSpEnum {

    ALI_OSS("AliOssService", "ali oss"),
    AWS_S3("S3Service", "aws s3"),
    S3_COMPATIBLE("S3Service", "s3 compatible"),
    MINIO("S3Service", "minio"),

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
