package com.wkclz.micro.k8s.custom.impl;

import com.wkclz.micro.k8s.helper.KubeConfigHelper;
import com.wkclz.micro.k8s.bean.kube.K8sParam;
import com.wkclz.micro.k8s.custom.K8sApi;
import com.wkclz.micro.k8s.utils.YamlUtil;
import io.kubernetes.client.common.KubernetesListObject;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.NetworkingV1Api;
import io.kubernetes.client.openapi.models.V1NetworkPolicy;
import io.kubernetes.client.openapi.models.V1Status;
import io.kubernetes.client.util.Yaml;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service("k8sNetworkPolicyImpl")
public class K8sNetworkPolicyImpl implements K8sApi {

    @Autowired
    private KubeConfigHelper kubeConfigHelper;

    public NetworkingV1Api getApi(K8sParam param) {
        return kubeConfigHelper.getNetworkingV1Api(param.getClusterName());
    }

    @Override
    public KubernetesListObject list(K8sParam param) throws ApiException {
        NetworkingV1Api api = getApi(param);
        return StringUtils.isBlank(param.getNamespace()) ?
            api.listNetworkPolicyForAllNamespaces().execute()
            :
            api.listNamespacedNetworkPolicy(param.getNamespace()).execute()
            ;
    }

    @Override
    public String yaml(K8sParam param) throws ApiException {
        NetworkingV1Api api = getApi(param);
        V1NetworkPolicy v1NetworkPolicy = api.readNamespacedNetworkPolicy(param.getName(), param.getNamespace()).execute();
        return Yaml.dump(v1NetworkPolicy);
    }

    @Override
    public String create(K8sParam param) throws ApiException {
        NetworkingV1Api api = getApi(param);
        V1NetworkPolicy entity = YamlUtil.yamlToK8sObject(param.getYaml(), V1NetworkPolicy.class);
        V1NetworkPolicy execute = api.createNamespacedNetworkPolicy(param.getNamespace(), entity).execute();
        return Yaml.dump(execute);
    }

    @Override
    public String update(K8sParam param) throws ApiException, IOException {
        NetworkingV1Api api = getApi(param);
        Object load = Yaml.load(param.getYaml());
        V1NetworkPolicy v1NetworkPolicy = (V1NetworkPolicy) load;
        String name = v1NetworkPolicy.getMetadata().getName();
        String namespace = v1NetworkPolicy.getMetadata().getNamespace();
        v1NetworkPolicy = api.replaceNamespacedNetworkPolicy(name, namespace, v1NetworkPolicy).execute();
        return Yaml.dump(v1NetworkPolicy);
    }

    @Override
    public String delete(K8sParam param) throws ApiException {
        NetworkingV1Api api = getApi(param);
        V1Status execute = api.deleteNamespacedNetworkPolicy(param.getName(), param.getNamespace()).execute();
        return Yaml.dump(execute);
    }

}
