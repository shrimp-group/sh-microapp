package com.wkclz.micro.k8s.custom.impl;

import com.wkclz.micro.k8s.helper.KubeConfigHelper;
import com.wkclz.micro.k8s.bean.kube.K8sParam;
import com.wkclz.micro.k8s.custom.K8sApi;
import com.wkclz.micro.k8s.utils.YamlUtil;
import io.kubernetes.client.common.KubernetesListObject;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.BatchV1Api;
import io.kubernetes.client.openapi.models.V1Job;
import io.kubernetes.client.openapi.models.V1Status;
import io.kubernetes.client.util.Yaml;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service("k8sJobImpl")
public class K8sJobImpl implements K8sApi {

    @Autowired
    private KubeConfigHelper kubeConfigHelper;

    public BatchV1Api getApi(K8sParam param) {
        return kubeConfigHelper.getBatchV1Api(param.getClusterName());
    }

    @Override
    public KubernetesListObject list(K8sParam param) throws ApiException {
        BatchV1Api api = getApi(param);
        return StringUtils.isBlank(param.getNamespace()) ?
            api.listJobForAllNamespaces().execute()
            :
            api.listNamespacedJob(param.getNamespace()).execute()
            ;
    }

    @Override
    public String yaml(K8sParam param) throws ApiException {
        BatchV1Api api = getApi(param);
        V1Job v1Job = api.readNamespacedJob(param.getName(), param.getNamespace()).execute();
        return Yaml.dump(v1Job);
    }

    @Override
    public String create(K8sParam param) throws ApiException {
        BatchV1Api api = getApi(param);
        V1Job entity = YamlUtil.yamlToK8sObject(param.getYaml(), V1Job.class);
        V1Job execute = api.createNamespacedJob(param.getNamespace(), entity).execute();
        return Yaml.dump(execute);
    }

    @Override
    public String update(K8sParam param) throws ApiException, IOException {
        BatchV1Api api = getApi(param);
        Object load = Yaml.load(param.getYaml());
        V1Job v1Job = (V1Job) load;
        String name = v1Job.getMetadata().getName();
        String namespace = v1Job.getMetadata().getNamespace();
        v1Job = api.replaceNamespacedJob(name, namespace, v1Job).execute();
        return Yaml.dump(v1Job);
    }

    @Override
    public String delete(K8sParam param) throws ApiException {
        BatchV1Api api = getApi(param);
        V1Status execute = api.deleteNamespacedJob(param.getName(), param.getNamespace()).execute();
        return Yaml.dump(execute);
    }

}
