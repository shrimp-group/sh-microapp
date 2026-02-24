package com.wkclz.micro.k8s.custom.impl;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.k8s.bean.kube.K8sParam;
import com.wkclz.micro.k8s.custom.K8sApi;
import io.kubernetes.client.common.KubernetesListObject;
import io.kubernetes.client.openapi.ApiException;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service("k8sVolumeImpl")
public class K8sVolumeImpl implements K8sApi {

    // TODO 暂不支持

    @Override
    public KubernetesListObject list(K8sParam param) throws ApiException {
        throw ValidationException.of("kind 处理力暂不支持 {}", param.getKind());
    }

    @Override
    public String yaml(K8sParam param) throws ApiException {
        throw ValidationException.of("kind 处理力暂不支持 {}", param.getKind());
    }

    @Override
    public String create(K8sParam param) throws ApiException {
        throw ValidationException.of("kind 处理力暂不支持 {}", param.getKind());
    }

    @Override
    public String update(K8sParam param) throws ApiException, IOException {
        throw ValidationException.of("kind 处理力暂不支持 {}", param.getKind());
    }

    @Override
    public String delete(K8sParam param) throws ApiException {
        throw ValidationException.of("kind 处理力暂不支持 {}", param.getKind());
    }

}
