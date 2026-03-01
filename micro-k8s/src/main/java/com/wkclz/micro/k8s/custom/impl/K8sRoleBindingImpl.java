package com.wkclz.micro.k8s.custom.impl;

import com.wkclz.micro.k8s.helper.KubeConfigHelper;
import com.wkclz.micro.k8s.bean.kube.K8sParam;
import com.wkclz.micro.k8s.custom.K8sApi;
import com.wkclz.micro.k8s.utils.YamlUtil;
import io.kubernetes.client.common.KubernetesListObject;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.RbacAuthorizationV1Api;
import io.kubernetes.client.openapi.models.V1RoleBinding;
import io.kubernetes.client.openapi.models.V1Status;
import io.kubernetes.client.util.Yaml;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service("k8sRoleBindingImpl")
public class K8sRoleBindingImpl implements K8sApi {

    @Autowired
    private KubeConfigHelper kubeConfigHelper;

    public RbacAuthorizationV1Api getApi(K8sParam param) {
        return kubeConfigHelper.getRbacAuthorizationV1Api(param.getClusterName());
    }

    @Override
    public KubernetesListObject list(K8sParam param) throws ApiException {
        RbacAuthorizationV1Api api = getApi(param);
        return StringUtils.isBlank(param.getNamespace()) ?
            api.listRoleBindingForAllNamespaces().execute()
            :
            api.listNamespacedRoleBinding(param.getNamespace()).execute()
            ;
    }

    @Override
    public String yaml(K8sParam param) throws ApiException {
        RbacAuthorizationV1Api api = getApi(param);
        V1RoleBinding v1RoleBinding = api.readNamespacedRoleBinding(param.getName(), param.getNamespace()).execute();
        return Yaml.dump(v1RoleBinding);
    }

    @Override
    public String create(K8sParam param) throws ApiException {
        RbacAuthorizationV1Api api = getApi(param);
        V1RoleBinding entity = YamlUtil.yamlToK8sObject(param.getYaml(), V1RoleBinding.class);
        V1RoleBinding execute = api.createNamespacedRoleBinding(param.getNamespace(), entity).execute();
        return Yaml.dump(execute);
    }

    @Override
    public String update(K8sParam param) throws ApiException, IOException {
        RbacAuthorizationV1Api api = getApi(param);
        Object load = Yaml.load(param.getYaml());
        V1RoleBinding v1RoleBinding = (V1RoleBinding) load;
        String name = v1RoleBinding.getMetadata().getName();
        String namespace = v1RoleBinding.getMetadata().getNamespace();
        v1RoleBinding = api.replaceNamespacedRoleBinding(name, namespace, v1RoleBinding).execute();
        return Yaml.dump(v1RoleBinding);
    }

    @Override
    public String delete(K8sParam param) throws ApiException {
        RbacAuthorizationV1Api api = getApi(param);
        V1Status execute = api.deleteNamespacedRoleBinding(param.getName(), param.getNamespace()).execute();
        return Yaml.dump(execute);
    }

}
