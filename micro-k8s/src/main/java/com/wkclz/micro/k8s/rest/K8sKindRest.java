package com.wkclz.micro.k8s.rest;

import cn.hutool.core.lang.Assert;
import com.wkclz.core.base.R;
import com.wkclz.micro.k8s.Route;
import com.wkclz.micro.k8s.bean.kube.K8sParam;
import com.wkclz.micro.k8s.custom.K8sApi;
import com.wkclz.micro.k8s.helper.K8sHelper;
import io.kubernetes.client.common.KubernetesListObject;
import io.kubernetes.client.openapi.ApiException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping(Route.PREFIX)
public class K8sKindRest {

    private final static List<String> NO_NAMESPACE_KIND = Arrays.asList(
        "ClusterRoleBinding",
        "ClusterRole",
        "CustomResourceDefinition",
        "Namespace"
    );

    @GetMapping(Route.CLUSTER_KIND_LIST)
    public R clusterKindList(K8sParam param) {
        try {
            paramCheck(param);
            K8sApi api = K8sHelper.getImplByKind(param.getKind());
            KubernetesListObject list = api.list(param);
            return R.ok(list);
        } catch (ApiException e) {
            return R.error(e.getResponseBody());
        }
    }

    @GetMapping(Route.CLUSTER_KIND_YAML)
    public R clusterKindYaml(K8sParam param) {
        try {
            paramCheck(param);
            Assert.notNull(param.getName(), "name 不能为空");
            K8sApi api = K8sHelper.getImplByKind(param.getKind());
            String yaml = api.yaml(param);
            return R.ok(yaml);
        } catch (ApiException e) {
            return R.ok(e.getResponseBody());
        }
    }

    @PostMapping(Route.CLUSTER_KIND_CREATE)
    public R clusterKindCreate(@RequestBody K8sParam param) {
        try {
            paramCheck(param);
            Assert.notNull(param.getYaml(), "yaml 不能为空");
            K8sApi api = K8sHelper.getImplByKind(param.getKind());
            String yaml = api.create(param);
            return R.ok(yaml);
        } catch (ApiException e) {
            return R.ok(e.getResponseBody());
        }
    }

    @PostMapping(Route.CLUSTER_KIND_UPDATE)
    public R clusterKindUpdate(@RequestBody K8sParam param) {
        try {
            paramCheck(param);
            Assert.notNull(param.getYaml(), "yaml 不能为空");
            K8sApi api = K8sHelper.getImplByKind(param.getKind());
            String yaml = api.update(param);
            return R.ok(yaml);
        } catch (ApiException e) {
            return R.ok(e.getResponseBody());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping(Route.CLUSTER_KIND_DELETE)
    public R clusterKindDelete(@RequestBody K8sParam param) {
        try {
            paramCheck(param);
            Assert.notNull(param.getName(), "name 不能为空");
            K8sApi api = K8sHelper.getImplByKind(param.getKind());
            String yaml = api.delete(param);
            return R.ok(yaml);
        } catch (ApiException e) {
            return R.ok(e.getResponseBody());
        }
    }


    private void paramCheck(K8sParam param) {
        Assert.notNull(param.getKind(), "kind 不能为空");
        Assert.notNull(param.getClusterName(), "clusterName 不能为空");

        // 部分逻辑不需要 namespace
        if (NO_NAMESPACE_KIND.contains(param.getKind())) {
            return;
        }

        Assert.notNull(param.getNamespace(), "namespace 不能为空");
    }

}
