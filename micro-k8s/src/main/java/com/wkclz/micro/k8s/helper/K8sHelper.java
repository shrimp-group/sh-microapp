package com.wkclz.micro.k8s.helper;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.k8s.bean.kube.Kind;
import com.wkclz.micro.k8s.custom.K8sApi;
import com.wkclz.spring.config.SpringContextHolder;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;

public class K8sHelper {

    public static K8sApi getImplByKind(String kind) {
        if (StringUtils.isBlank(kind)) {
            throw ValidationException.of("kind不能为空");
        }

        if (!EnumUtils.isValidEnum(Kind.class, kind)) {
            throw ValidationException.of("不支持的 k8s Kind 类型");
        }

        String implName = "k8s" + kind + "Impl";
        Object bean = SpringContextHolder.getBean(implName);

        if (bean == null) {
            throw ValidationException.of("当前客户端不支持的 kind: {}", kind);
        }

        if (!(bean instanceof K8sApi)) {
            throw ValidationException.of("不是有效的 kind: {}", kind);
        }
        return (K8sApi) bean;
    }
}
