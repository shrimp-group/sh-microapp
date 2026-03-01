package com.wkclz.micro.k8s.custom.impl;

import com.wkclz.micro.k8s.helper.KubeConfigHelper;
import com.wkclz.micro.k8s.bean.kube.K8sParam;
import com.wkclz.micro.k8s.custom.K8sApi;
import com.wkclz.micro.k8s.utils.YamlUtil;
import io.kubernetes.client.common.KubernetesListObject;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.ApiextensionsV1Api;
import io.kubernetes.client.openapi.models.V1CustomResourceDefinition;
import io.kubernetes.client.openapi.models.V1Status;
import io.kubernetes.client.util.Yaml;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service("k8sCustomResourceDefinitionImpl")
public class K8sCustomResourceDefinitionImpl implements K8sApi {

    @Autowired
    private KubeConfigHelper kubeConfigHelper;

    public ApiextensionsV1Api getApi(K8sParam param) {
        return kubeConfigHelper.getApiextensionsV1Api(param.getClusterName());
    }

    @Override
    public KubernetesListObject list(K8sParam param) throws ApiException {
        ApiextensionsV1Api api = getApi(param);
        return api.listCustomResourceDefinition().execute();
    }

    @Override
    public String yaml(K8sParam param) throws ApiException {
        ApiextensionsV1Api api = getApi(param);
        V1CustomResourceDefinition v1CustomResourceDefinition = api.readCustomResourceDefinition(param.getName()).execute();
        return Yaml.dump(v1CustomResourceDefinition);
    }

    @Override
    public String create(K8sParam param) throws ApiException {
        ApiextensionsV1Api api = getApi(param);
        V1CustomResourceDefinition entity = YamlUtil.yamlToK8sObject(param.getYaml(), V1CustomResourceDefinition.class);
        V1CustomResourceDefinition execute = api.createCustomResourceDefinition(entity).execute();
        return Yaml.dump(execute);
    }

    @Override
    public String update(K8sParam param) throws ApiException, IOException {
        ApiextensionsV1Api api = getApi(param);
        Object load = Yaml.load(param.getYaml());
        V1CustomResourceDefinition v1CustomResourceDefinition = (V1CustomResourceDefinition) load;
        String name = v1CustomResourceDefinition.getMetadata().getName();
        String namespace = v1CustomResourceDefinition.getMetadata().getNamespace();
        v1CustomResourceDefinition = api.replaceCustomResourceDefinition(name, v1CustomResourceDefinition).execute();
        return Yaml.dump(v1CustomResourceDefinition);
    }

    @Override
    public String delete(K8sParam param) throws ApiException {
        ApiextensionsV1Api api = getApi(param);
        V1Status execute = api.deleteCustomResourceDefinition(param.getName()).execute();
        return Yaml.dump(execute);
    }

}
