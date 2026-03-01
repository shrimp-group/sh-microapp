package com.wkclz.micro.k8s.bean.entity;

import com.wkclz.core.annotation.Desc;
import com.wkclz.core.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;




/**
 * Description Create by sh-generator
 * @author shrimp
 * @table k8s_config (k8s配置) 重新生成代码会覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class K8sConfig extends BaseEntity {

    /**
     * 集群名称
     */
    @Desc("集群名称")
    private String clusterName;

    /**
     * 配置信息
     */
    @Desc("配置信息")
    private String kubeConfig;


    public static K8sConfig copy(K8sConfig source, K8sConfig target) {
        if (target == null ) { target = new K8sConfig();}
        if (source == null) { return target; }
        target.setId(source.getId());
        target.setClusterName(source.getClusterName());
        target.setKubeConfig(source.getKubeConfig());
        target.setSort(source.getSort());
        target.setCreateTime(source.getCreateTime());
        target.setCreateBy(source.getCreateBy());
        target.setUpdateTime(source.getUpdateTime());
        target.setUpdateBy(source.getUpdateBy());
        target.setRemark(source.getRemark());
        target.setVersion(source.getVersion());
        return target;
    }

    public static K8sConfig copyIfNotNull(K8sConfig source, K8sConfig target) {
        if (target == null ) { target = new K8sConfig();}
        if (source == null) { return target; }
        if (source.getId() != null) { target.setId(source.getId()); }
        if (source.getClusterName() != null) { target.setClusterName(source.getClusterName()); }
        if (source.getKubeConfig() != null) { target.setKubeConfig(source.getKubeConfig()); }
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

