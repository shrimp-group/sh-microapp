package com.wkclz.micro.k8s.rest;

import com.wkclz.core.base.R;
import com.wkclz.micro.k8s.Route;
import com.wkclz.micro.k8s.bean.kube.K8sParam;
import com.wkclz.micro.k8s.bean.req.K8sClusterReq;
import com.wkclz.micro.k8s.service.K8sClusterService;
import io.kubernetes.client.openapi.models.V1NamespaceList;
import io.kubernetes.client.openapi.models.V1NodeList;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.wkclz.tool.utils.BeanUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "2.K8s集群", description = "K8s集群查询接口")
@RestController
@RequestMapping(Route.PREFIX)
@Validated
public class K8sRest {

    @Autowired
    private K8sClusterService k8sClusterService;

    @Operation(summary = "1.集群-获取节点", description = "获取指定集群的Node节点列表")
    @GetMapping(Route.CLUSTER_NODES)
    public R<V1NodeList> clusterNodes(@Valid K8sClusterReq req) {
        K8sParam param = BeanUtil.cp(req, K8sParam.class);
        V1NodeList nodeList = k8sClusterService.getNodes(param);
        return R.ok(nodeList);
    }

    @Operation(summary = "2.集群-获取命名空间", description = "获取指定集群的Namespace列表")
    @GetMapping(Route.CLUSTER_NAMESPACES)
    public R<V1NamespaceList> clusterNamespaces(@Valid K8sClusterReq req) {
        K8sParam param = BeanUtil.cp(req, K8sParam.class);
        V1NamespaceList namespaceList = k8sClusterService.getNamespaces(param);
        return R.ok(namespaceList);
    }

    @Operation(summary = "3.集群-获取命名空间(简要)", description = "获取指定集群的Namespace名称列表")
    @GetMapping(Route.CLUSTER_NAMESPACES_BRIEFLY)
    public R<List<String>> clusterNamespacesBriefly(@Valid K8sClusterReq req) {
        K8sParam param = BeanUtil.cp(req, K8sParam.class);
        List<String> namespaces = k8sClusterService.getNamespacesBriefly(param);
        return R.ok(namespaces);
    }

}
