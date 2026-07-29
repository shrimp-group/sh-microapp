package com.wkclz.micro.flowable.rest;

import com.wkclz.core.base.R;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.flowable.bean.entity.MdmFlowableNodeConfig;
import com.wkclz.micro.flowable.bean.req.NodeListReq;
import com.wkclz.micro.flowable.bean.req.NodeUpdateReq;
import com.wkclz.micro.flowable.bean.resp.NodeConfigResp;
import com.wkclz.micro.flowable.service.MdmFlowableNodeConfigService;
import com.wkclz.tool.utils.BeanUtil;
import com.wkclz.web.bean.IdReq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "管理端-节点配置", description = "节点配置管理接口")
@RestController
@RequestMapping(Route.PREFIX)
@Validated
public class NodeConfigRest {

    private static final Logger log = LoggerFactory.getLogger(NodeConfigRest.class);

    @Autowired
    private MdmFlowableNodeConfigService nodeConfigService;

    @Operation(summary = "节点配置列表")
    @GetMapping(Route.ADMIN_NODE_LIST)
    public R<List<NodeConfigResp>> list(@Valid NodeListReq req) {
        MdmFlowableNodeConfig param = new MdmFlowableNodeConfig();
        param.setDesignId(req.getDesignId());
        List<MdmFlowableNodeConfig> nodes = nodeConfigService.selectByEntity(param);
        return R.ok(BeanUtil.cp(nodes, NodeConfigResp.class));
    }

    @Operation(summary = "节点配置详情")
    @GetMapping(Route.ADMIN_NODE_INFO)
    public R<NodeConfigResp> info(@Valid IdReq req) {
        MdmFlowableNodeConfig node = nodeConfigService.selectById(req.getId());
        if (node == null) {
            throw ValidationException.of("节点配置不存在");
        }
        return R.ok(BeanUtil.cp(node, NodeConfigResp.class));
    }

    @Operation(summary = "更新节点配置")
    @PostMapping(Route.ADMIN_NODE_UPDATE)
    public R<Integer> update(@Valid @RequestBody NodeUpdateReq req) {
        log.info("更新节点配置: id={}", req.getId());
        MdmFlowableNodeConfig node = nodeConfigService.selectById(req.getId());
        if (node == null) {
            throw ValidationException.of("节点配置不存在");
        }
        MdmFlowableNodeConfig update = new MdmFlowableNodeConfig();
        update.setId(req.getId());
        update.setVersion(req.getVersion());
        if (req.getAssigneeType() != null) { update.setAssigneeType(req.getAssigneeType()); }
        if (req.getAssigneeValue() != null) { update.setAssigneeValue(req.getAssigneeValue()); }
        if (req.getFormFields() != null) { update.setFormFields(req.getFormFields()); }
        nodeConfigService.updateByIdSelective(update);
        return R.ok(1);
    }
}
