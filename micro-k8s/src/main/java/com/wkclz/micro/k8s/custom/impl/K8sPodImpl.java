package com.wkclz.micro.k8s.custom.impl;

import com.wkclz.micro.k8s.helper.KubeConfigHelper;
import com.wkclz.micro.k8s.bean.kube.K8sParam;
import com.wkclz.micro.k8s.custom.K8sApi;
import com.wkclz.micro.k8s.utils.YamlUtil;
import io.kubernetes.client.common.KubernetesListObject;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.util.Yaml;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("k8sPodImpl")
public class K8sPodImpl implements K8sApi {

    @Autowired
    private KubeConfigHelper kubeConfigHelper;

    public CoreV1Api getApi(K8sParam param) {
        return kubeConfigHelper.getCoreV1Api(param.getClusterName());
    }

    @Override
    public KubernetesListObject list(K8sParam param) throws ApiException {
        CoreV1Api api = getApi(param);
        V1PodList v1PodList = StringUtils.isBlank(param.getNamespace()) ?
            api.listPodForAllNamespaces().execute()
            :
            api.listNamespacedPod(param.getNamespace()).execute()
            ;

        if (StringUtils.isNotBlank(param.getName())) {
            List<V1Pod> items = v1PodList.getItems().stream().filter(t -> {
                V1ObjectMeta metadata = t.getMetadata();
                if (metadata != null) {
                    Map<String, String> labels = metadata.getLabels();
                    if (labels != null) {
                        if (param.getName().equals(labels.get("app"))) {
                            return true;
                        }
                    }
                }
                return false;
            }).collect(Collectors.toList());
            v1PodList.setItems(items);
        }
        return v1PodList;
    }

    @Override
    public String yaml(K8sParam param) throws ApiException {
        CoreV1Api api = getApi(param);
        V1Pod v1Pod = api.readNamespacedPod(param.getName(), param.getNamespace()).execute();
        return Yaml.dump(v1Pod);
    }

    @Override
    public String create(K8sParam param) throws ApiException {
        CoreV1Api api = getApi(param);
        V1Pod entity = YamlUtil.yamlToK8sObject(param.getYaml(), V1Pod.class);
        V1Pod execute = api.createNamespacedPod(param.getNamespace(), entity).execute();
        return Yaml.dump(execute);
    }

    @Override
    public String update(K8sParam param) throws ApiException, IOException {
        CoreV1Api api = getApi(param);
        Object load = Yaml.load(param.getYaml());
        V1Pod v1Pod = (V1Pod) load;
        String name = v1Pod.getMetadata().getName();
        String namespace = v1Pod.getMetadata().getNamespace();
        v1Pod = api.replaceNamespacedPod(name, namespace, v1Pod).execute();
        return Yaml.dump(v1Pod);
    }

    @Override
    public String delete(K8sParam param) throws ApiException {
        CoreV1Api api = getApi(param);
        V1Pod pod = api.deleteNamespacedPod(param.getName(), param.getNamespace()).execute();
        return Yaml.dump(pod);
    }

}
