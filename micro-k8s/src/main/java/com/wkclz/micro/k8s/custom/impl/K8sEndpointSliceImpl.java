package com.wkclz.micro.k8s.custom.impl;

import com.wkclz.micro.k8s.helper.KubeConfigHelper;
import com.wkclz.micro.k8s.bean.kube.K8sParam;
import com.wkclz.micro.k8s.custom.K8sApi;
import com.wkclz.micro.k8s.utils.YamlUtil;
import io.kubernetes.client.common.KubernetesListObject;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.DiscoveryV1Api;
import io.kubernetes.client.openapi.models.V1EndpointSlice;
import io.kubernetes.client.openapi.models.V1Status;
import io.kubernetes.client.util.Yaml;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service("k8sEndpointSliceImpl")
public class K8sEndpointSliceImpl implements K8sApi {

    @Autowired
    private KubeConfigHelper kubeConfigHelper;

    public DiscoveryV1Api getApi(K8sParam param) {
        log.info("EndpointSlice 获取 DiscoveryV1Api, clusterName: {}", param.getClusterName());
        return kubeConfigHelper.getDiscoveryV1Api(param.getClusterName());
    }

    @Override
    public KubernetesListObject list(K8sParam param) throws ApiException {
        log.info("EndpointSlice list 操作, clusterName: {}, namespace: {}", param.getClusterName(), param.getNamespace());
        DiscoveryV1Api api = getApi(param);
        return StringUtils.isBlank(param.getNamespace()) ?
            api.listEndpointSliceForAllNamespaces().execute()
            :
            api.listNamespacedEndpointSlice(param.getNamespace()).execute()
            ;
    }

    @Override
    public String yaml(K8sParam param) throws ApiException {
        log.info("EndpointSlice yaml 操作, clusterName: {}, namespace: {}, name: {}", param.getClusterName(), param.getNamespace(), param.getName());
        DiscoveryV1Api api = getApi(param);
        V1EndpointSlice v1EndpointSlice = api.readNamespacedEndpointSlice(param.getName(), param.getNamespace()).execute();
        return Yaml.dump(v1EndpointSlice);
    }

    @Override
    public String create(K8sParam param) throws ApiException {
        log.info("EndpointSlice create 操作, clusterName: {}, namespace: {}", param.getClusterName(), param.getNamespace());
        DiscoveryV1Api api = getApi(param);
        V1EndpointSlice entity = YamlUtil.yamlToK8sObject(param.getYaml(), V1EndpointSlice.class);
        V1EndpointSlice execute = api.createNamespacedEndpointSlice(param.getNamespace(), entity).execute();
        return Yaml.dump(execute);
    }

    @Override
    public String update(K8sParam param) throws ApiException, IOException {
        log.info("EndpointSlice update 操作, clusterName: {}", param.getClusterName());
        DiscoveryV1Api api = getApi(param);
        Object load = Yaml.load(param.getYaml());
        V1EndpointSlice v1EndpointSlice = (V1EndpointSlice) load;
        String name = v1EndpointSlice.getMetadata().getName();
        String namespace = v1EndpointSlice.getMetadata().getNamespace();
        v1EndpointSlice = api.replaceNamespacedEndpointSlice(name, namespace, v1EndpointSlice).execute();
        return Yaml.dump(v1EndpointSlice);
    }

    @Override
    public String delete(K8sParam param) throws ApiException {
        log.info("EndpointSlice delete 操作, clusterName: {}, namespace: {}, name: {}", param.getClusterName(), param.getNamespace(), param.getName());
        DiscoveryV1Api api = getApi(param);
        V1Status status = api.deleteNamespacedEndpointSlice(param.getName(), param.getNamespace()).execute();
        return Yaml.dump(status);
    }

}
