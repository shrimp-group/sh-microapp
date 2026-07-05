package com.wkclz.micro.k8s.rest;

import com.wkclz.core.base.R;
import com.wkclz.micro.k8s.Route;
import com.wkclz.micro.k8s.bean.kube.K8sParam;
import com.wkclz.micro.k8s.bean.req.K8sKindCreateReq;
import com.wkclz.micro.k8s.bean.req.K8sKindDeleteReq;
import com.wkclz.micro.k8s.bean.req.K8sKindListReq;
import com.wkclz.micro.k8s.bean.req.K8sKindUpdateReq;
import com.wkclz.micro.k8s.bean.req.K8sKindYamlReq;
import com.wkclz.micro.k8s.service.K8sKindService;
import io.kubernetes.client.common.KubernetesListObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.wkclz.tool.utils.BeanUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "3.K8s Kind资源", description = "K8s Kind资源CRUD接口")
@RestController
@RequestMapping(Route.PREFIX)
@Validated
public class K8sKindRest {

    @Autowired
    private K8sKindService k8sKindService;

    @Operation(summary = "1.Kind资源-列表查询", description = "根据Kind类型查询K8s资源列表")
    @GetMapping(Route.CLUSTER_KIND_LIST)
    public R<KubernetesListObject> clusterKindList(@Valid K8sKindListReq req) {
        K8sParam param = BeanUtil.cp(req, K8sParam.class);
        KubernetesListObject list = k8sKindService.list(param);
        return R.ok(list);
    }

    @Operation(summary = "2.Kind资源-YAML查询", description = "根据名称查询K8s资源的YAML定义")
    @GetMapping(Route.CLUSTER_KIND_YAML)
    public R<String> clusterKindYaml(@Valid K8sKindYamlReq req) {
        K8sParam param = new K8sParam();
        param.setClusterName(req.getClusterName());
        param.setNamespace(req.getNamespace());
        param.setKind(req.getKind());
        param.setName(req.getName());
        String yaml = k8sKindService.yaml(param);
        return R.ok(yaml);
    }

    @Operation(summary = "3.Kind资源-创建", description = "根据YAML创建K8s资源")
    @PostMapping(Route.CLUSTER_KIND_CREATE)
    public R<String> clusterKindCreate(@Valid @RequestBody K8sKindCreateReq req) {
        K8sParam param = new K8sParam();
        param.setClusterName(req.getClusterName());
        param.setNamespace(req.getNamespace());
        param.setKind(req.getKind());
        param.setYaml(req.getYaml());
        String result = k8sKindService.create(param);
        return R.ok(result);
    }

    @Operation(summary = "4.Kind资源-更新", description = "根据YAML更新K8s资源")
    @PostMapping(Route.CLUSTER_KIND_UPDATE)
    public R<String> clusterKindUpdate(@Valid @RequestBody K8sKindUpdateReq req) {
        K8sParam param = BeanUtil.cp(req, K8sParam.class);
        String result = k8sKindService.update(param);
        return R.ok(result);
    }

    @Operation(summary = "5.Kind资源-删除", description = "根据名称删除K8s资源")
    @PostMapping(Route.CLUSTER_KIND_DELETE)
    public R<String> clusterKindDelete(@Valid @RequestBody K8sKindDeleteReq req) {
        K8sParam param = new K8sParam();
        param.setClusterName(req.getClusterName());
        param.setNamespace(req.getNamespace());
        param.setKind(req.getKind());
        param.setName(req.getName());
        String result = k8sKindService.delete(param);
        return R.ok(result);
    }

}
