package com.wkclz.micro.k8s.custom.impl;

import com.wkclz.micro.k8s.helper.KubeConfigHelper;
import com.wkclz.micro.k8s.bean.kube.K8sParam;
import com.wkclz.micro.k8s.custom.K8sApi;
import com.wkclz.micro.k8s.utils.YamlUtil;
import io.kubernetes.client.common.KubernetesListObject;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Secret;
import io.kubernetes.client.openapi.models.V1Status;
import io.kubernetes.client.util.Yaml;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service("k8sSecretImpl")
public class K8sSecretImpl implements K8sApi {

    @Autowired
    private KubeConfigHelper kubeConfigHelper;

    public CoreV1Api getApi(K8sParam param) {
        return kubeConfigHelper.getCoreV1Api(param.getClusterName());
    }

    @Override
    public KubernetesListObject list(K8sParam param) throws ApiException {
        CoreV1Api api = getApi(param);
        return StringUtils.isBlank(param.getNamespace()) ?
            api.listSecretForAllNamespaces().execute()
            :
            api.listNamespacedSecret(param.getNamespace()).execute()
            ;
    }

    @Override
    public String yaml(K8sParam param) throws ApiException {
        CoreV1Api api = getApi(param);
        V1Secret v1Secret = api.readNamespacedSecret(param.getName(), param.getNamespace()).execute();
        return Yaml.dump(v1Secret);
    }

    @Override
    public String create(K8sParam param) throws ApiException {
        CoreV1Api api = getApi(param);
        V1Secret entity = YamlUtil.yamlToK8sObject(param.getYaml(), V1Secret.class);
        V1Secret execute = api.createNamespacedSecret(param.getNamespace(), entity).execute();
        return Yaml.dump(execute);
    }

    @Override
    public String update(K8sParam param) throws ApiException, IOException {
        CoreV1Api api = getApi(param);
        Object load = Yaml.load(param.getYaml());
        V1Secret v1Secret = (V1Secret) load;
        String name = v1Secret.getMetadata().getName();
        String namespace = v1Secret.getMetadata().getNamespace();
        v1Secret = api.replaceNamespacedSecret(name, namespace, v1Secret).execute();
        return Yaml.dump(v1Secret);
    }

    @Override
    public String delete(K8sParam param) throws ApiException {
        CoreV1Api api = getApi(param);
        V1Status status = api.deleteNamespacedSecret(param.getName(), param.getNamespace()).execute();
        return Yaml.dump(status);
    }

}
