package com.wkclz.micro.k8s.bean.dto;

import com.wkclz.micro.k8s.bean.entity.K8sConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Description Create by sh-generator
 * @author shrimp
 * @table K8sConfig () 数据库实例扩展，代码重新生成不覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class K8sConfigDto extends K8sConfig {




    /**
     * entity 转 Dto
     * @param source
     * @return
     */
    public static K8sConfigDto copy(K8sConfig source) {
        K8sConfigDto target = new K8sConfigDto();
        K8sConfig.copy(source, target);
        return target;
    }
}

