package com.wkclz.micro.k8s.custom.impl;

import com.wkclz.micro.k8s.bean.kube.K8sParam;
import com.wkclz.micro.k8s.custom.K8sApi;
import com.wkclz.micro.k8s.helper.KubeConfigHelper;
import com.wkclz.micro.k8s.utils.YamlUtil;
import io.kubernetes.client.common.KubernetesListObject;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.RbacAuthorizationV1Api;
import io.kubernetes.client.openapi.models.V1ClusterRole;
import io.kubernetes.client.openapi.models.V1Status;
import io.kubernetes.client.util.Yaml;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service("k8sClusterRoleImpl")
public class K8sClusterRoleImpl implements K8sApi {

    @Autowired
    private KubeConfigHelper kubeConfigHelper;

    public RbacAuthorizationV1Api getApi(K8sParam param) {
        return kubeConfigHelper.getRbacAuthorizationV1Api(param.getClusterName());
    }

    @Override
    public KubernetesListObject list(K8sParam param) throws ApiException {
        RbacAuthorizationV1Api api = getApi(param);
        return api.listClusterRole().execute();
    }

    @Override
    public String yaml(K8sParam param) throws ApiException {
        RbacAuthorizationV1Api api = getApi(param);
        V1ClusterRole v1ClusterRole = api.readClusterRole(param.getName()).execute();
        return Yaml.dump(v1ClusterRole);
    }

    @Override
    public String create(K8sParam param) throws ApiException {
        RbacAuthorizationV1Api api = getApi(param);
        V1ClusterRole entity = YamlUtil.yamlToK8sObject(param.getYaml(), V1ClusterRole.class);
        V1ClusterRole execute = api.createClusterRole(entity).execute();
        return Yaml.dump(execute);
    }

    @Override
    public String update(K8sParam param) throws ApiException, IOException {
        RbacAuthorizationV1Api api = getApi(param);
        Object load = Yaml.load(param.getYaml());
        V1ClusterRole v1ClusterRole = (V1ClusterRole) load;
        String name = v1ClusterRole.getMetadata().getName();
        v1ClusterRole = api.replaceClusterRole(name, v1ClusterRole).execute();
        return Yaml.dump(v1ClusterRole);
    }

    @Override
    public String delete(K8sParam param) throws ApiException {
        RbacAuthorizationV1Api api = getApi(param);
        V1Status execute = api.deleteClusterRole(param.getName()).execute();
        return Yaml.dump(execute);
    }

}
