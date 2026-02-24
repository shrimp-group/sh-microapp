package com.wkclz.micro.k8s.custom;

import com.wkclz.micro.k8s.bean.kube.K8sParam;
import io.kubernetes.client.common.KubernetesListObject;
import io.kubernetes.client.openapi.ApiException;

import java.io.IOException;

public interface K8sApi {

    KubernetesListObject list(K8sParam param) throws ApiException;

    String yaml(K8sParam param) throws ApiException;

    String create(K8sParam param) throws ApiException;

    String update(K8sParam param) throws ApiException, IOException;

    String delete(K8sParam param) throws ApiException;

}
