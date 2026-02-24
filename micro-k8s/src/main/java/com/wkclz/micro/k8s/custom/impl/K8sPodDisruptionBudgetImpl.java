package com.wkclz.micro.k8s.custom.impl;

import com.wkclz.micro.k8s.helper.KubeConfigHelper;
import com.wkclz.micro.k8s.bean.kube.K8sParam;
import com.wkclz.micro.k8s.custom.K8sApi;
import com.wkclz.micro.k8s.utils.YamlUtil;
import io.kubernetes.client.common.KubernetesListObject;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.PolicyV1Api;
import io.kubernetes.client.openapi.models.V1PodDisruptionBudget;
import io.kubernetes.client.openapi.models.V1Status;
import io.kubernetes.client.util.Yaml;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service("k8sPodDisruptionBudgetImpl")
public class K8sPodDisruptionBudgetImpl implements K8sApi {

    @Autowired
    private KubeConfigHelper kubeConfigHelper;

    public PolicyV1Api getApi(K8sParam param) {
        return kubeConfigHelper.getPolicyV1Api(param.getClusterName());
    }

    @Override
    public KubernetesListObject list(K8sParam param) throws ApiException {
        PolicyV1Api api = getApi(param);
        return StringUtils.isBlank(param.getNamespace()) ?
            api.listPodDisruptionBudgetForAllNamespaces().execute()
            :
            api.listNamespacedPodDisruptionBudget(param.getNamespace()).execute()
            ;
    }

    @Override
    public String yaml(K8sParam param) throws ApiException {
        PolicyV1Api api = getApi(param);
        V1PodDisruptionBudget v1PodDisruptionBudget = api.readNamespacedPodDisruptionBudget(param.getName(), param.getNamespace()).execute();
        return Yaml.dump(v1PodDisruptionBudget);
    }

    @Override
    public String create(K8sParam param) throws ApiException {
        PolicyV1Api api = getApi(param);
        V1PodDisruptionBudget entity = YamlUtil.yamlToK8sObject(param.getYaml(), V1PodDisruptionBudget.class);
        V1PodDisruptionBudget execute = api.createNamespacedPodDisruptionBudget(param.getNamespace(), entity).execute();
        return Yaml.dump(execute);
    }

    @Override
    public String update(K8sParam param) throws ApiException, IOException {
        PolicyV1Api api = getApi(param);
        Object load = Yaml.load(param.getYaml());
        V1PodDisruptionBudget v1PodDisruptionBudget = (V1PodDisruptionBudget) load;
        String name = v1PodDisruptionBudget.getMetadata().getName();
        String namespace = v1PodDisruptionBudget.getMetadata().getNamespace();
        v1PodDisruptionBudget = api.replaceNamespacedPodDisruptionBudget(name, namespace, v1PodDisruptionBudget).execute();
        return Yaml.dump(v1PodDisruptionBudget);
    }

    @Override
    public String delete(K8sParam param) throws ApiException {
        PolicyV1Api api = getApi(param);
        V1Status status = api.deleteNamespacedPodDisruptionBudget(param.getName(), param.getNamespace()).execute();
        return Yaml.dump(status);
    }

}
