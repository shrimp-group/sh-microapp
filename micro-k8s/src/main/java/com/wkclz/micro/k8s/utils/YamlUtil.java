package com.wkclz.micro.k8s.utils;

import com.wkclz.core.exception.ValidationException;
import io.kubernetes.client.common.KubernetesObject;
import io.kubernetes.client.util.Yaml;

import java.io.IOException;

public class YamlUtil {

    public static <T extends KubernetesObject> T yamlToK8sObject(String yaml, Class<T> clazz) {
        KubernetesObject kubernetesObject = yamlToK8sObject(yaml);
        return (T)kubernetesObject;
    }

    public static KubernetesObject yamlToK8sObject(String yaml) {
        KubernetesObject k8sObject;
        try {
            Object load = Yaml.load(yaml);
            if (!(load instanceof KubernetesObject)) {
                throw ValidationException.of("yaml 解析异常, 无法确认 yaml 类型");
            }
            k8sObject = (KubernetesObject)load;
        } catch (IOException e) {
            throw ValidationException.of("yaml 解析异常: {}", e.getMessage());
        }
        return k8sObject;
    }

}
