package com.wkclz.micro.k8s.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.micro.k8s.Route;
import com.wkclz.micro.k8s.bean.entity.K8sConfig;
import com.wkclz.micro.k8s.bean.req.K8sConfigCreateReq;
import com.wkclz.micro.k8s.bean.req.K8sConfigInfoReq;
import com.wkclz.micro.k8s.bean.req.K8sConfigPageReq;
import com.wkclz.micro.k8s.bean.req.K8sConfigRemoveReq;
import com.wkclz.micro.k8s.bean.req.K8sConfigUpdateReq;
import com.wkclz.micro.k8s.bean.resp.K8sConfigResp;
import com.wkclz.micro.k8s.service.K8sConfigService;
import com.wkclz.tool.utils.BeanUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table k8s_config (k8s配置) 示例rest 接口，代码重新生成会覆盖
 */
@Tag(name = "1.K8s配置", description = "K8s集群配置管理接口")
@RestController
@RequestMapping(Route.PREFIX)
@Validated
public class K8sConfigRest {

    @Autowired
    private K8sConfigService k8sConfigService;

    @Operation(summary = "1.K8s配置-分页查询", description = "根据条件分页查询K8s集群配置列表")
    @GetMapping(Route.CONFIG_PAGE)
    public R<PageData<K8sConfigResp>> configPage(@Valid K8sConfigPageReq req) {
        K8sConfig entity = BeanUtil.cp(req, K8sConfig.class);
        PageData<K8sConfig> page = k8sConfigService.getClusterPage(entity);
        PageData<K8sConfigResp> newPage = page.convert(K8sConfigResp.class);
        return R.ok(newPage);
    }

    @Operation(summary = "2.K8s配置-详情", description = "根据ID查询K8s集群配置详情")
    @GetMapping(Route.CONFIG_INFO)
    public R<K8sConfigResp> configInfo(@Valid K8sConfigInfoReq req) {
        K8sConfig entity = k8sConfigService.selectById(req.getId());
        K8sConfigResp resp = BeanUtil.cp(entity, K8sConfigResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "3.K8s配置-创建", description = "新增K8s集群配置")
    @PostMapping(Route.CONFIG_CREATE)
    public R<K8sConfigResp> configCreate(@Valid @RequestBody K8sConfigCreateReq req) {
        K8sConfig entity = BeanUtil.cp(req, K8sConfig.class);
        entity = k8sConfigService.create(entity);
        K8sConfigResp resp = BeanUtil.cp(entity, K8sConfigResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "4.K8s配置-更新", description = "更新K8s集群配置")
    @PostMapping(Route.CONFIG_UPDATE)
    public R<K8sConfigResp> configUpdate(@Valid @RequestBody K8sConfigUpdateReq req) {
        K8sConfig entity = BeanUtil.cp(req, K8sConfig.class);
        entity = k8sConfigService.update(entity);
        K8sConfigResp resp = BeanUtil.cp(entity, K8sConfigResp.class);
        return R.ok(resp);
    }

    @Operation(summary = "5.K8s配置-删除", description = "删除K8s集群配置")
    @PostMapping(Route.CONFIG_REMOVE)
    public R<Integer> configRemove(@Valid @RequestBody K8sConfigRemoveReq req) {
        k8sConfigService.deleteById(req.getId());
        return R.ok(1);
    }

    @Operation(summary = "6.K8s配置-选项列表", description = "获取K8s集群名称选项列表")
    @GetMapping(Route.CONFIG_OPTIONS)
    public R<List<String>> configOptions() {
        List<String> clusterOptions = k8sConfigService.getClusterOptions();
        return R.ok(clusterOptions);
    }


}

