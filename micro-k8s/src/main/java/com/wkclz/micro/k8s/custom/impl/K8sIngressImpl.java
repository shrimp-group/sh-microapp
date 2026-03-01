package com.wkclz.micro.k8s.custom.impl;

import com.wkclz.micro.k8s.helper.KubeConfigHelper;
import com.wkclz.micro.k8s.bean.kube.K8sParam;
import com.wkclz.micro.k8s.custom.K8sApi;
import com.wkclz.micro.k8s.utils.YamlUtil;
import io.kubernetes.client.common.KubernetesListObject;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.NetworkingV1Api;
import io.kubernetes.client.openapi.models.V1Ingress;
import io.kubernetes.client.openapi.models.V1Status;
import io.kubernetes.client.util.Yaml;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service("k8sIngressImpl")
public class K8sIngressImpl implements K8sApi {

    @Autowired
    private KubeConfigHelper kubeConfigHelper;

    public NetworkingV1Api getApi(K8sParam param) {
        return kubeConfigHelper.getNetworkingV1Api(param.getClusterName());
    }

    @Override
    public KubernetesListObject list(K8sParam param) throws ApiException {
        NetworkingV1Api api = getApi(param);
        return StringUtils.isBlank(param.getNamespace()) ?
            api.listIngressForAllNamespaces().execute()
            :
            api.listNamespacedIngress(param.getNamespace()).execute()
            ;
    }

    @Override
    public String yaml(K8sParam param) throws ApiException {
        NetworkingV1Api api = getApi(param);
        V1Ingress v1Ingress = api.readNamespacedIngress(param.getName(), param.getNamespace()).execute();
        return Yaml.dump(v1Ingress);
    }

    @Override
    public String create(K8sParam param) throws ApiException {
        NetworkingV1Api api = getApi(param);
        V1Ingress entity = YamlUtil.yamlToK8sObject(param.getYaml(), V1Ingress.class);
        V1Ingress execute = api.createNamespacedIngress(param.getNamespace(), entity).execute();
        return Yaml.dump(execute);
    }

    @Override
    public String update(K8sParam param) throws ApiException, IOException {
        NetworkingV1Api api = getApi(param);
        Object load = Yaml.load(param.getYaml());
        V1Ingress v1Ingress = (V1Ingress) load;
        String name = v1Ingress.getMetadata().getName();
        String namespace = v1Ingress.getMetadata().getNamespace();
        v1Ingress = api.replaceNamespacedIngress(name, namespace, v1Ingress).execute();
        return Yaml.dump(v1Ingress);
    }

    @Override
    public String delete(K8sParam param) throws ApiException {
        NetworkingV1Api api = getApi(param);
        V1Status status = api.deleteNamespacedIngress(param.getName(), param.getNamespace()).execute();
        return Yaml.dump(status);
    }

}
