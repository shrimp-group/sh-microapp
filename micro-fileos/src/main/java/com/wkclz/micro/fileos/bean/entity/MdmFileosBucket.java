package com.wkclz.micro.fileos.bean.entity;

import com.wkclz.core.annotation.FieldDesc;
import com.wkclz.core.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MdmFileosBucket extends BaseEntity {

    @FieldDesc("租户编码")
    private String tenantCode;

    @FieldDesc("Bucket名称")
    private String bucketName;

    @FieldDesc("OSS服务商")
    private String ossSp;

    @FieldDesc("内网Endpoint")
    private String endpointInner;

    @FieldDesc("外网Endpoint")
    private String endpointOuter;

    @FieldDesc("区域")
    private String region;

    @FieldDesc("Access Key")
    private String accessKey;

    @FieldDesc("Secret Key")
    private String secretKey;

    @FieldDesc("默认标识")
    private Integer defaultFlag;

    public static MdmFileosBucket copy(MdmFileosBucket source, MdmFileosBucket target) {
        if (target == null) { target = new MdmFileosBucket(); }
        if (source == null) { return target; }
        target.setId(source.getId());
        target.setTenantCode(source.getTenantCode());
        target.setBucketName(source.getBucketName());
        target.setOssSp(source.getOssSp());
        target.setEndpointInner(source.getEndpointInner());
        target.setEndpointOuter(source.getEndpointOuter());
        target.setRegion(source.getRegion());
        target.setAccessKey(source.getAccessKey());
        target.setSecretKey(source.getSecretKey());
        target.setDefaultFlag(source.getDefaultFlag());
        target.setSort(source.getSort());
        target.setCreateTime(source.getCreateTime());
        target.setCreateBy(source.getCreateBy());
        target.setUpdateTime(source.getUpdateTime());
        target.setUpdateBy(source.getUpdateBy());
        target.setRemark(source.getRemark());
        target.setVersion(source.getVersion());
        return target;
    }

    public static MdmFileosBucket copyIfNotNull(MdmFileosBucket source, MdmFileosBucket target) {
        if (target == null) { target = new MdmFileosBucket(); }
        if (source == null) { return target; }
        if (source.getId() != null) { target.setId(source.getId()); }
        if (source.getTenantCode() != null) { target.setTenantCode(source.getTenantCode()); }
        if (source.getBucketName() != null) { target.setBucketName(source.getBucketName()); }
        if (source.getOssSp() != null) { target.setOssSp(source.getOssSp()); }
        if (source.getEndpointInner() != null) { target.setEndpointInner(source.getEndpointInner()); }
        if (source.getEndpointOuter() != null) { target.setEndpointOuter(source.getEndpointOuter()); }
        if (source.getRegion() != null) { target.setRegion(source.getRegion()); }
        if (source.getAccessKey() != null) { target.setAccessKey(source.getAccessKey()); }
        if (source.getSecretKey() != null) { target.setSecretKey(source.getSecretKey()); }
        if (source.getDefaultFlag() != null) { target.setDefaultFlag(source.getDefaultFlag()); }
        if (source.getSort() != null) { target.setSort(source.getSort()); }
        if (source.getCreateTime() != null) { target.setCreateTime(source.getCreateTime()); }
        if (source.getCreateBy() != null) { target.setCreateBy(source.getCreateBy()); }
        if (source.getUpdateTime() != null) { target.setUpdateTime(source.getUpdateTime()); }
        if (source.getUpdateBy() != null) { target.setUpdateBy(source.getUpdateBy()); }
        if (source.getRemark() != null) { target.setRemark(source.getRemark()); }
        if (source.getVersion() != null) { target.setVersion(source.getVersion()); }
        return target;
    }

}
