package com.wkclz.micro.k8s.custom.impl;

import com.wkclz.micro.k8s.helper.KubeConfigHelper;
import com.wkclz.micro.k8s.bean.kube.K8sParam;
import com.wkclz.micro.k8s.custom.K8sApi;
import com.wkclz.micro.k8s.utils.YamlUtil;
import io.kubernetes.client.common.KubernetesListObject;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.CoreV1Event;
import io.kubernetes.client.openapi.models.V1Status;
import io.kubernetes.client.util.Yaml;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service("k8sEventImpl")
public class K8sEventImpl implements K8sApi {

    @Autowired
    private KubeConfigHelper kubeConfigHelper;

    public CoreV1Api getApi(K8sParam param) {
        return kubeConfigHelper.getCoreV1Api(param.getClusterName());
    }

    @Override
    public KubernetesListObject list(K8sParam param) throws ApiException {
        CoreV1Api api = getApi(param);
        return StringUtils.isBlank(param.getNamespace()) ?
            api.listEventForAllNamespaces().execute()
            :
            api.listNamespacedEvent(param.getNamespace()).execute()
            ;
    }

    @Override
    public String yaml(K8sParam param) throws ApiException {
        CoreV1Api api = getApi(param);
        CoreV1Event coreV1Event = api.readNamespacedEvent(param.getName(), param.getNamespace()).execute();
        return Yaml.dump(coreV1Event);
    }

    @Override
    public String create(K8sParam param) throws ApiException {
        CoreV1Api api = getApi(param);
        CoreV1Event entity = YamlUtil.yamlToK8sObject(param.getYaml(), CoreV1Event.class);
        CoreV1Event execute = api.createNamespacedEvent(param.getNamespace(), entity).execute();
        return Yaml.dump(execute);
    }

    @Override
    public String update(K8sParam param) throws ApiException, IOException {
        CoreV1Api api = getApi(param);
        Object load = Yaml.load(param.getYaml());
        CoreV1Event v1Event = (CoreV1Event) load;
        String name = v1Event.getMetadata().getName();
        String namespace = v1Event.getMetadata().getNamespace();
        v1Event = api.replaceNamespacedEvent(name, namespace, v1Event).execute();
        return Yaml.dump(v1Event);
    }

    @Override
    public String delete(K8sParam param) throws ApiException {
        CoreV1Api api = getApi(param);
        V1Status execute = api.deleteNamespacedEvent(param.getName(), param.getNamespace()).execute();
        return Yaml.dump(execute);
    }

}
