package com.wkclz.micro.k8s.custom.impl;

import com.wkclz.micro.k8s.helper.KubeConfigHelper;
import com.wkclz.micro.k8s.bean.kube.K8sParam;
import com.wkclz.micro.k8s.custom.K8sApi;
import com.wkclz.micro.k8s.utils.YamlUtil;
import io.kubernetes.client.common.KubernetesListObject;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.models.V1DaemonSet;
import io.kubernetes.client.openapi.models.V1Status;
import io.kubernetes.client.util.Yaml;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service("k8sDaemonSetImpl")
public class K8sDaemonSetImpl implements K8sApi {

    @Autowired
    private KubeConfigHelper kubeConfigHelper;

    public AppsV1Api getApi(K8sParam param) {
        return kubeConfigHelper.getAppsV1Api(param.getClusterName());
    }

    @Override
    public KubernetesListObject list(K8sParam param) throws ApiException {
        AppsV1Api api = getApi(param);
        return StringUtils.isBlank(param.getNamespace()) ?
            api.listDaemonSetForAllNamespaces().execute()
            :
            api.listNamespacedDaemonSet(param.getNamespace()).execute()
            ;
    }

    @Override
    public String yaml(K8sParam param) throws ApiException {
        AppsV1Api api = getApi(param);
        V1DaemonSet v1DaemonSet = api.readNamespacedDaemonSet(param.getName(), param.getNamespace()).execute();
        return Yaml.dump(v1DaemonSet);
    }

    @Override
    public String create(K8sParam param) throws ApiException {
        AppsV1Api api = getApi(param);
        V1DaemonSet entity = YamlUtil.yamlToK8sObject(param.getYaml(), V1DaemonSet.class);
        V1DaemonSet execute = api.createNamespacedDaemonSet(param.getNamespace(), entity).execute();
        return Yaml.dump(execute);
    }

    @Override
    public String update(K8sParam param) throws ApiException, IOException {
        AppsV1Api api = getApi(param);
        Object load = Yaml.load(param.getYaml());
        V1DaemonSet v1DaemonSet = (V1DaemonSet) load;
        String name = v1DaemonSet.getMetadata().getName();
        String namespace = v1DaemonSet.getMetadata().getNamespace();
        v1DaemonSet = api.replaceNamespacedDaemonSet(name, namespace, v1DaemonSet).execute();
        return Yaml.dump(v1DaemonSet);
    }

    @Override
    public String delete(K8sParam param) throws ApiException {
        AppsV1Api api = getApi(param);
        V1Status execute = api.deleteNamespacedDaemonSet(param.getName(), param.getNamespace()).execute();
        return Yaml.dump(execute);
    }

}
