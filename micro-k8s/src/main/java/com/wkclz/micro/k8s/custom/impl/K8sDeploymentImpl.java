package com.wkclz.micro.k8s.custom.impl;

import com.wkclz.micro.k8s.helper.KubeConfigHelper;
import com.wkclz.micro.k8s.bean.kube.K8sParam;
import com.wkclz.micro.k8s.custom.K8sApi;
import com.wkclz.micro.k8s.utils.YamlUtil;
import io.kubernetes.client.common.KubernetesListObject;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1Status;
import io.kubernetes.client.util.Yaml;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service("k8sDeploymentImpl")
public class K8sDeploymentImpl implements K8sApi {

    @Autowired
    private KubeConfigHelper kubeConfigHelper;

    public AppsV1Api getApi(K8sParam param) {
        return kubeConfigHelper.getAppsV1Api(param.getClusterName());
    }

    @Override
    public KubernetesListObject list(K8sParam param) throws ApiException {
        AppsV1Api api = getApi(param);
        return StringUtils.isBlank(param.getNamespace()) ?
            api.listDeploymentForAllNamespaces().execute()
            :
            api.listNamespacedDeployment(param.getNamespace()).execute()
            ;
    }

    @Override
    public String yaml(K8sParam param) throws ApiException {
        AppsV1Api api = getApi(param);
        V1Deployment v1Deployment = api.readNamespacedDeployment(param.getName(), param.getNamespace()).execute();
        return Yaml.dump(v1Deployment);
    }

    @Override
    public String create(K8sParam param) throws ApiException {
        AppsV1Api api = getApi(param);
        V1Deployment entity = YamlUtil.yamlToK8sObject(param.getYaml(), V1Deployment.class);
        V1Deployment execute = api.createNamespacedDeployment(param.getNamespace(), entity).execute();
        return Yaml.dump(execute);
    }

    @Override
    public String update(K8sParam param) throws ApiException, IOException {
        AppsV1Api api = getApi(param);
        Object load = Yaml.load(param.getYaml());
        V1Deployment v1Deployment = (V1Deployment) load;
        String name = v1Deployment.getMetadata().getName();
        String namespace = v1Deployment.getMetadata().getNamespace();
        v1Deployment = api.replaceNamespacedDeployment(name, namespace, v1Deployment).execute();
        return Yaml.dump(v1Deployment);
    }

    @Override
    public String delete(K8sParam param) throws ApiException {
        AppsV1Api api = getApi(param);
        V1Status status = api.deleteNamespacedDeployment(param.getName(), param.getNamespace()).execute();
        return Yaml.dump(status);
    }

}
