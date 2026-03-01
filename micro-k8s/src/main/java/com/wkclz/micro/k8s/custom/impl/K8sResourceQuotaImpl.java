package com.wkclz.micro.k8s.custom.impl;

import com.wkclz.micro.k8s.helper.KubeConfigHelper;
import com.wkclz.micro.k8s.bean.kube.K8sParam;
import com.wkclz.micro.k8s.custom.K8sApi;
import com.wkclz.micro.k8s.utils.YamlUtil;
import io.kubernetes.client.common.KubernetesListObject;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ResourceQuota;
import io.kubernetes.client.util.Yaml;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service("k8sResourceQuotaImpl")
public class K8sResourceQuotaImpl implements K8sApi {

    @Autowired
    private KubeConfigHelper kubeConfigHelper;

    public CoreV1Api getApi(K8sParam param) {
        return kubeConfigHelper.getCoreV1Api(param.getClusterName());
    }

    @Override
    public KubernetesListObject list(K8sParam param) throws ApiException {
        CoreV1Api api = getApi(param);
        return StringUtils.isBlank(param.getNamespace()) ?
            api.listResourceQuotaForAllNamespaces().execute()
            :
            api.listNamespacedResourceQuota(param.getNamespace()).execute()
            ;
    }

    @Override
    public String yaml(K8sParam param) throws ApiException {
        CoreV1Api api = getApi(param);
        V1ResourceQuota v1ResourceQuota = api.readNamespacedResourceQuota(param.getName(), param.getNamespace()).execute();
        return Yaml.dump(v1ResourceQuota);
    }

    @Override
    public String create(K8sParam param) throws ApiException {
        CoreV1Api api = getApi(param);
        V1ResourceQuota entity = YamlUtil.yamlToK8sObject(param.getYaml(), V1ResourceQuota.class);
        V1ResourceQuota execute = api.createNamespacedResourceQuota(param.getNamespace(), entity).execute();
        return Yaml.dump(execute);
    }

    @Override
    public String update(K8sParam param) throws ApiException, IOException {
        CoreV1Api api = getApi(param);
        Object load = Yaml.load(param.getYaml());
        V1ResourceQuota v1ResourceQuota = (V1ResourceQuota) load;
        String name = v1ResourceQuota.getMetadata().getName();
        String namespace = v1ResourceQuota.getMetadata().getNamespace();
        v1ResourceQuota = api.replaceNamespacedResourceQuota(name, namespace, v1ResourceQuota).execute();
        return Yaml.dump(v1ResourceQuota);
    }

    @Override
    public String delete(K8sParam param) throws ApiException {
        CoreV1Api api = getApi(param);
        V1ResourceQuota execute = api.deleteNamespacedResourceQuota(param.getName(), param.getNamespace()).execute();
        return Yaml.dump(execute);
    }

}
