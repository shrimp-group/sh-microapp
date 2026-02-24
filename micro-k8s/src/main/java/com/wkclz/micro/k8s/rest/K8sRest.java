package com.wkclz.micro.k8s.rest;

import cn.hutool.core.lang.Assert;
import com.wkclz.core.base.R;
import com.wkclz.micro.k8s.Route;
import com.wkclz.micro.k8s.bean.kube.K8sParam;
import com.wkclz.micro.k8s.helper.KubeConfigHelper;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1NamespaceList;
import io.kubernetes.client.openapi.models.V1NodeList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping(Route.PREFIX)
public class K8sRest {

    @Autowired
    private KubeConfigHelper kubeConfigHelper;

    @GetMapping(Route.CLUSTER_NAMESPACES)
    public R clusterNamespaces(K8sParam param) throws ApiException {
        Assert.notNull(param.getClusterName(), "clusterName 不能为空");
        CoreV1Api api = kubeConfigHelper.getCoreV1Api(param.getClusterName());
        V1NamespaceList namespaceList = api.listNamespace().execute();
        return R.ok(namespaceList);
    }

    @GetMapping(Route.CLUSTER_NAMESPACES_BRIEFLY)
    public R clusterNamespacesBriefly(K8sParam param) throws ApiException {
        R<V1NamespaceList> result = clusterNamespaces(param);
        List<String> namespaces = result.getData().getItems().stream().map(item -> Objects.requireNonNull(item.getMetadata()).getName()).toList();
        return R.ok(namespaces);
    }

    @GetMapping(Route.CLUSTER_NODES)
    public R clusterNodes(K8sParam param) throws ApiException {
        Assert.notNull(param.getClusterName(), "clusterName 不能为空");
        CoreV1Api api = kubeConfigHelper.getCoreV1Api(param.getClusterName());
        V1NodeList nodeList = api.listNode().execute();
        return R.ok(nodeList);
    }


}
