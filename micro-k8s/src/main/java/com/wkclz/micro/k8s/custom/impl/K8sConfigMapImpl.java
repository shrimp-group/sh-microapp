package com.wkclz.micro.k8s.custom.impl;

import com.wkclz.micro.k8s.helper.KubeConfigHelper;
import com.wkclz.micro.k8s.bean.kube.K8sParam;
import com.wkclz.micro.k8s.custom.K8sApi;
import com.wkclz.micro.k8s.utils.YamlUtil;
import io.kubernetes.client.common.KubernetesListObject;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.openapi.models.V1Status;
import io.kubernetes.client.util.Yaml;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service("k8sConfigMapImpl")
public class K8sConfigMapImpl implements K8sApi {

    @Autowired
    private KubeConfigHelper kubeConfigHelper;

    public CoreV1Api getApi(K8sParam param) {
        return kubeConfigHelper.getCoreV1Api(param.getClusterName());
    }

    @Override
    public KubernetesListObject list(K8sParam param) throws ApiException {
        CoreV1Api api = getApi(param);
        return StringUtils.isBlank(param.getNamespace()) ?
            api.listConfigMapForAllNamespaces().execute()
            :
            api.listNamespacedConfigMap(param.getNamespace()).execute()
            ;
    }

    @Override
    public String yaml(K8sParam param) throws ApiException {
        CoreV1Api api = getApi(param);
        V1ConfigMap v1ConfigMap = api.readNamespacedConfigMap(param.getName(), param.getNamespace()).execute();
        return Yaml.dump(v1ConfigMap);
    }

    @Override
    public String create(K8sParam param) throws ApiException {
        CoreV1Api api = getApi(param);
        V1ConfigMap entity = YamlUtil.yamlToK8sObject(param.getYaml(), V1ConfigMap.class);
        V1ConfigMap execute = api.createNamespacedConfigMap(param.getNamespace(), entity).execute();
        return Yaml.dump(execute);
    }

    @Override
    public String update(K8sParam param) throws ApiException, IOException {
        CoreV1Api api = getApi(param);
        Object load = Yaml.load(param.getYaml());
        V1ConfigMap v1ConfigMap = (V1ConfigMap) load;
        String name = v1ConfigMap.getMetadata().getName();
        String namespace = v1ConfigMap.getMetadata().getNamespace();
        v1ConfigMap = api.replaceNamespacedConfigMap(name, namespace, v1ConfigMap).execute();
        return Yaml.dump(v1ConfigMap);
    }

    @Override
    public String delete(K8sParam param) throws ApiException {
        CoreV1Api api = getApi(param);
        V1Status execute = api.deleteNamespacedConfigMap(param.getName(), param.getNamespace()).execute();
        return Yaml.dump(execute);
    }

}
