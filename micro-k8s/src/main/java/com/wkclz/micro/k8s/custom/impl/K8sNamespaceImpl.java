package com.wkclz.micro.k8s.custom.impl;

import com.wkclz.micro.k8s.helper.KubeConfigHelper;
import com.wkclz.micro.k8s.bean.kube.K8sParam;
import com.wkclz.micro.k8s.custom.K8sApi;
import com.wkclz.micro.k8s.utils.YamlUtil;
import io.kubernetes.client.common.KubernetesListObject;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Namespace;
import io.kubernetes.client.openapi.models.V1Status;
import io.kubernetes.client.util.Yaml;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service("k8sNamespaceImpl")
public class K8sNamespaceImpl implements K8sApi {

    @Autowired
    private KubeConfigHelper kubeConfigHelper;

    public CoreV1Api getApi(K8sParam param) {
        return kubeConfigHelper.getCoreV1Api(param.getClusterName());
    }

    @Override
    public KubernetesListObject list(K8sParam param) throws ApiException {
        CoreV1Api api = getApi(param);
        return api.listNamespace().execute();
    }

    @Override
    public String yaml(K8sParam param) throws ApiException {
        CoreV1Api api = getApi(param);
        V1Namespace v1Namespace = api.readNamespace(param.getName()).execute();
        return Yaml.dump(v1Namespace);
    }

    @Override
    public String create(K8sParam param) throws ApiException {
        CoreV1Api api = getApi(param);
        V1Namespace entity = YamlUtil.yamlToK8sObject(param.getYaml(), V1Namespace.class);
        V1Namespace execute = api.createNamespace(entity).execute();
        return Yaml.dump(execute);
    }

    @Override
    public String update(K8sParam param) throws ApiException, IOException {
        CoreV1Api api = getApi(param);
        Object load = Yaml.load(param.getYaml());
        V1Namespace v1Namespace = (V1Namespace) load;
        String name = v1Namespace.getMetadata().getName();
        v1Namespace = api.replaceNamespace(name, v1Namespace).execute();
        return Yaml.dump(v1Namespace);
    }

    @Override
    public String delete(K8sParam param) throws ApiException {
        CoreV1Api api = getApi(param);
        V1Status execute = api.deleteNamespace(param.getName()).execute();
        return Yaml.dump(execute);
    }

}
