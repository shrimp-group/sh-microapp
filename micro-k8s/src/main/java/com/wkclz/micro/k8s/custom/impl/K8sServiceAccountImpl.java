package com.wkclz.micro.k8s.custom.impl;

import com.wkclz.micro.k8s.helper.KubeConfigHelper;
import com.wkclz.micro.k8s.bean.kube.K8sParam;
import com.wkclz.micro.k8s.custom.K8sApi;
import com.wkclz.micro.k8s.utils.YamlUtil;
import io.kubernetes.client.common.KubernetesListObject;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ServiceAccount;
import io.kubernetes.client.util.Yaml;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service("k8sServiceAccountImpl")
public class K8sServiceAccountImpl implements K8sApi {

    @Autowired
    private KubeConfigHelper kubeConfigHelper;

    public CoreV1Api getApi(K8sParam param) {
        return kubeConfigHelper.getCoreV1Api(param.getClusterName());
    }

    @Override
    public KubernetesListObject list(K8sParam param) throws ApiException {
        CoreV1Api api = getApi(param);
        return StringUtils.isBlank(param.getNamespace()) ?
            api.listServiceAccountForAllNamespaces().execute()
            :
            api.listNamespacedServiceAccount(param.getNamespace()).execute()
            ;
    }

    @Override
    public String yaml(K8sParam param) throws ApiException {
        CoreV1Api api = getApi(param);
        V1ServiceAccount v1ServiceAccount = api.readNamespacedServiceAccount(param.getName(), param.getNamespace()).execute();
        return Yaml.dump(v1ServiceAccount);
    }

    @Override
    public String create(K8sParam param) throws ApiException {
        CoreV1Api api = getApi(param);
        V1ServiceAccount entity = YamlUtil.yamlToK8sObject(param.getYaml(), V1ServiceAccount.class);
        V1ServiceAccount execute = api.createNamespacedServiceAccount(param.getNamespace(), entity).execute();
        return Yaml.dump(execute);
    }

    @Override
    public String update(K8sParam param) throws ApiException, IOException {
        CoreV1Api api = getApi(param);
        Object load = Yaml.load(param.getYaml());
        V1ServiceAccount v1ServiceAccount = (V1ServiceAccount) load;
        String name = v1ServiceAccount.getMetadata().getName();
        String namespace = v1ServiceAccount.getMetadata().getNamespace();
        v1ServiceAccount = api.replaceNamespacedServiceAccount(name, namespace, v1ServiceAccount).execute();
        return Yaml.dump(v1ServiceAccount);
    }

    @Override
    public String delete(K8sParam param) throws ApiException {
        CoreV1Api api = getApi(param);
        V1ServiceAccount execute = api.deleteNamespacedServiceAccount(param.getName(), param.getNamespace()).execute();
        return Yaml.dump(execute);
    }

}
