package com.wkclz.micro.k8s.custom.impl;

import com.wkclz.micro.k8s.bean.kube.K8sParam;
import com.wkclz.micro.k8s.custom.K8sApi;
import com.wkclz.micro.k8s.helper.KubeConfigHelper;
import com.wkclz.micro.k8s.utils.YamlUtil;
import io.kubernetes.client.common.KubernetesListObject;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.RbacAuthorizationV1Api;
import io.kubernetes.client.openapi.models.V1ClusterRoleBinding;
import io.kubernetes.client.openapi.models.V1Status;
import io.kubernetes.client.util.Yaml;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service("k8sClusterRoleBindingImpl")
public class K8sClusterRoleBindingImpl implements K8sApi {

    @Autowired
    private KubeConfigHelper kubeConfigHelper;

    public RbacAuthorizationV1Api getApi(K8sParam param) {
        return kubeConfigHelper.getRbacAuthorizationV1Api(param.getClusterName());
    }

    @Override
    public KubernetesListObject list(K8sParam param) throws ApiException {
        RbacAuthorizationV1Api api = getApi(param);
        return api.listClusterRoleBinding().execute();
    }

    @Override
    public String yaml(K8sParam param) throws ApiException {
        RbacAuthorizationV1Api api = getApi(param);
        V1ClusterRoleBinding v1ClusterRoleBinding = api.readClusterRoleBinding(param.getName()).execute();
        return Yaml.dump(v1ClusterRoleBinding);
    }

    @Override
    public String create(K8sParam param) throws ApiException {
        RbacAuthorizationV1Api api = getApi(param);
        V1ClusterRoleBinding entity = YamlUtil.yamlToK8sObject(param.getYaml(), V1ClusterRoleBinding.class);
        V1ClusterRoleBinding execute = api.createClusterRoleBinding(entity).execute();
        return Yaml.dump(execute);
    }

    @Override
    public String update(K8sParam param) throws ApiException, IOException {
        RbacAuthorizationV1Api api = getApi(param);
        Object load = Yaml.load(param.getYaml());
        V1ClusterRoleBinding v1ClusterRoleBinding = (V1ClusterRoleBinding) load;
        String name = v1ClusterRoleBinding.getMetadata().getName();
        v1ClusterRoleBinding = api.replaceClusterRoleBinding(name, v1ClusterRoleBinding).execute();
        return Yaml.dump(v1ClusterRoleBinding);
    }

    @Override
    public String delete(K8sParam param) throws ApiException {
        RbacAuthorizationV1Api api = getApi(param);
        V1Status execute = api.deleteClusterRoleBinding(param.getName()).execute();
        return Yaml.dump(execute);
    }

}
