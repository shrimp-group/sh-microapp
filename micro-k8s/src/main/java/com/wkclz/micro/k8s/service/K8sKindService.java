package com.wkclz.micro.k8s.service;

import com.wkclz.core.exception.SystemException;
import com.wkclz.micro.k8s.bean.kube.K8sParam;
import com.wkclz.micro.k8s.custom.K8sApi;
import com.wkclz.micro.k8s.helper.K8sHelper;
import io.kubernetes.client.common.KubernetesListObject;
import io.kubernetes.client.openapi.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class K8sKindService {

    private static final List<String> NO_NAMESPACE_KIND = Arrays.asList(
        "ClusterRoleBinding",
        "ClusterRole",
        "CustomResourceDefinition",
        "Namespace"
    );

    public KubernetesListObject list(K8sParam param) {
        try {
            validateNamespace(param);
            K8sApi api = K8sHelper.getImplByKind(param.getKind());
            return api.list(param);
        } catch (ApiException e) {
            log.error("Kind资源列表查询失败, kind: {}, clusterName: {}, namespace: {}, code: {}, body: {}",
                param.getKind(), param.getClusterName(), param.getNamespace(), e.getCode(), e.getResponseBody(), e);
            throw SystemException.of("Kind资源列表查询失败: " + e.getResponseBody());
        }
    }

    public String yaml(K8sParam param) {
        try {
            validateNamespace(param);
            K8sApi api = K8sHelper.getImplByKind(param.getKind());
            return api.yaml(param);
        } catch (ApiException e) {
            log.error("Kind资源YAML查询失败, kind: {}, name: {}, clusterName: {}, code: {}, body: {}",
                param.getKind(), param.getName(), param.getClusterName(), e.getCode(), e.getResponseBody(), e);
            throw SystemException.of("Kind资源YAML查询失败: " + e.getResponseBody());
        }
    }

    public String create(K8sParam param) {
        try {
            validateNamespace(param);
            K8sApi api = K8sHelper.getImplByKind(param.getKind());
            return api.create(param);
        } catch (ApiException e) {
            log.error("Kind资源创建失败, kind: {}, clusterName: {}, namespace: {}, code: {}, body: {}",
                param.getKind(), param.getClusterName(), param.getNamespace(), e.getCode(), e.getResponseBody(), e);
            throw SystemException.of("Kind资源创建失败: " + e.getResponseBody());
        }
    }

    public String update(K8sParam param) {
        try {
            validateNamespace(param);
            K8sApi api = K8sHelper.getImplByKind(param.getKind());
            return api.update(param);
        } catch (ApiException e) {
            log.error("Kind资源更新失败, kind: {}, clusterName: {}, namespace: {}, code: {}, body: {}",
                param.getKind(), param.getClusterName(), param.getNamespace(), e.getCode(), e.getResponseBody(), e);
            throw SystemException.of("Kind资源更新失败: " + e.getResponseBody());
        } catch (IOException e) {
            log.error("Kind资源更新IO异常, kind: {}, clusterName: {}, namespace: {}",
                param.getKind(), param.getClusterName(), param.getNamespace(), e);
            throw SystemException.of("Kind资源更新失败: " + e.getMessage());
        }
    }

    public String delete(K8sParam param) {
        try {
            validateNamespace(param);
            K8sApi api = K8sHelper.getImplByKind(param.getKind());
            return api.delete(param);
        } catch (ApiException e) {
            log.error("Kind资源删除失败, kind: {}, name: {}, clusterName: {}, code: {}, body: {}",
                param.getKind(), param.getName(), param.getClusterName(), e.getCode(), e.getResponseBody(), e);
            throw SystemException.of("Kind资源删除失败: " + e.getResponseBody());
        }
    }

    private void validateNamespace(K8sParam param) {
        if (NO_NAMESPACE_KIND.contains(param.getKind())) {
            return;
        }
        if (param.getNamespace() == null || param.getNamespace().isBlank()) {
            throw new com.wkclz.core.exception.ValidationException("namespace 不能为空");
        }
    }
}
