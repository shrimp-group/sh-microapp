package com.wkclz.micro.k8s.service;

import com.wkclz.core.exception.SystemException;
import com.wkclz.micro.k8s.bean.kube.K8sParam;
import com.wkclz.micro.k8s.helper.KubeConfigHelper;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1NamespaceList;
import io.kubernetes.client.openapi.models.V1NodeList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class K8sClusterService {

    @Autowired
    private KubeConfigHelper kubeConfigHelper;

    public V1NodeList getNodes(K8sParam param) {
        try {
            CoreV1Api api = kubeConfigHelper.getCoreV1Api(param.getClusterName());
            return api.listNode().execute();
        } catch (ApiException e) {
            log.error("获取集群节点失败, clusterName: {}, code: {}, body: {}", param.getClusterName(), e.getCode(), e.getResponseBody(), e);
            throw SystemException.of("获取集群节点失败: " + e.getResponseBody());
        }
    }

    public V1NamespaceList getNamespaces(K8sParam param) {
        try {
            CoreV1Api api = kubeConfigHelper.getCoreV1Api(param.getClusterName());
            return api.listNamespace().execute();
        } catch (ApiException e) {
            log.error("获取命名空间失败, clusterName: {}, code: {}, body: {}", param.getClusterName(), e.getCode(), e.getResponseBody(), e);
            throw SystemException.of("获取命名空间失败: " + e.getResponseBody());
        }
    }

    public List<String> getNamespacesBriefly(K8sParam param) {
        V1NamespaceList namespaceList = getNamespaces(param);
        return namespaceList.getItems().stream()
            .map(item -> Objects.requireNonNull(item.getMetadata()).getName())
            .toList();
    }
}
