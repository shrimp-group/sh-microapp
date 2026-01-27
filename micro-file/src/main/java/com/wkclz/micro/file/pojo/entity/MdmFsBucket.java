package com.wkclz.micro.file.pojo.entity;

import com.wkclz.core.annotation.Desc;
import com.wkclz.core.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;




/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_fs_bucket (Bucket管理) 重新生成代码会覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class MdmFsBucket extends BaseEntity {

    /**
     * 租户编码
     */
    @Desc("租户编码")
    private String tenantCode;

    /**
     * Bucket
     */
    @Desc("Bucket")
    private String bucket;

    /**
     * OSS服务商
     */
    @Desc("OSS服务商")
    private String ossSp;

    /**
     * 内网Endpoint
     */
    @Desc("内网Endpoint")
    private String endpointInner;

    /**
     * 外网Endpoint
     */
    @Desc("外网Endpoint")
    private String endpointOuter;

    /**
     * 区域
     */
    @Desc("区域")
    private String region;

    /**
     * Access Key
     */
    @Desc("Access Key")
    private String accessKey;

    /**
     * Secret Key
     */
    @Desc("Secret Key")
    private String secretKey;

    /**
     * 默认标识
     */
    @Desc("默认标识")
    private Integer defaultFlag;


    public static MdmFsBucket copy(MdmFsBucket source, MdmFsBucket target) {
        if (target == null ) { target = new MdmFsBucket();}
        if (source == null) { return target; }
        target.setId(source.getId());
        target.setTenantCode(source.getTenantCode());
        target.setBucket(source.getBucket());
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

    public static MdmFsBucket copyIfNotNull(MdmFsBucket source, MdmFsBucket target) {
        if (target == null ) { target = new MdmFsBucket();}
        if (source == null) { return target; }
        if (source.getId() != null) { target.setId(source.getId()); }
        if (source.getTenantCode() != null) { target.setTenantCode(source.getTenantCode()); }
        if (source.getBucket() != null) { target.setBucket(source.getBucket()); }
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

