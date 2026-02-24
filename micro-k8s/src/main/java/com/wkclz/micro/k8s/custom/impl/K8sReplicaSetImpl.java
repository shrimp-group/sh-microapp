package com.wkclz.micro.k8s.custom.impl;

import com.wkclz.micro.k8s.helper.KubeConfigHelper;
import com.wkclz.micro.k8s.bean.kube.K8sParam;
import com.wkclz.micro.k8s.custom.K8sApi;
import com.wkclz.micro.k8s.utils.YamlUtil;
import io.kubernetes.client.common.KubernetesListObject;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.models.V1ReplicaSet;
import io.kubernetes.client.openapi.models.V1Status;
import io.kubernetes.client.util.Yaml;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service("k8sReplicaSetImpl")
public class K8sReplicaSetImpl implements K8sApi {

    @Autowired
    private KubeConfigHelper kubeConfigHelper;

    public AppsV1Api getApi(K8sParam param) {
        return kubeConfigHelper.getAppsV1Api(param.getClusterName());
    }

    @Override
    public KubernetesListObject list(K8sParam param) throws ApiException {
        AppsV1Api api = getApi(param);
        return StringUtils.isBlank(param.getNamespace()) ?
            api.listReplicaSetForAllNamespaces().execute()
            :
            api.listNamespacedReplicaSet(param.getNamespace()).execute()
            ;
    }

    @Override
    public String yaml(K8sParam param) throws ApiException {
        AppsV1Api api = getApi(param);
        V1ReplicaSet v1ReplicaSet = api.readNamespacedReplicaSet(param.getName(), param.getNamespace()).execute();
        return Yaml.dump(v1ReplicaSet);
    }

    @Override
    public String create(K8sParam param) throws ApiException {
        AppsV1Api api = getApi(param);
        V1ReplicaSet entity = YamlUtil.yamlToK8sObject(param.getYaml(), V1ReplicaSet.class);
        V1ReplicaSet execute = api.createNamespacedReplicaSet(param.getNamespace(), entity).execute();
        return Yaml.dump(execute);
    }

    @Override
    public String update(K8sParam param) throws ApiException, IOException {
        AppsV1Api api = getApi(param);
        Object load = Yaml.load(param.getYaml());
        V1ReplicaSet v1ReplicaSet = (V1ReplicaSet) load;
        String name = v1ReplicaSet.getMetadata().getName();
        String namespace = v1ReplicaSet.getMetadata().getNamespace();
        v1ReplicaSet = api.replaceNamespacedReplicaSet(name, namespace, v1ReplicaSet).execute();
        return Yaml.dump(v1ReplicaSet);
    }

    @Override
    public String delete(K8sParam param) throws ApiException {
        AppsV1Api api = getApi(param);
        V1Status execute = api.deleteNamespacedReplicaSet(param.getName(), param.getNamespace()).execute();
        return Yaml.dump(execute);
    }

}
