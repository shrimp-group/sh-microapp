package com.wkclz.micro.flowable.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.flowable.client.bean.req.ProcessDeployReq;
import com.wkclz.flowable.client.bean.resp.ProcessDeployResp;
import com.wkclz.micro.flowable.bean.entity.FlowableNodeConfig;
import com.wkclz.micro.flowable.bean.entity.FlowableProcessDesign;
import com.wkclz.micro.flowable.bean.enums.DesignStatus;
import com.wkclz.micro.flowable.bean.enums.ErrorType;
import com.wkclz.micro.flowable.bean.enums.NodeType;
import com.wkclz.micro.flowable.bean.req.*;
import com.wkclz.micro.flowable.bean.resp.*;
import com.wkclz.micro.flowable.service.FlowableClientWrapper;
import com.wkclz.micro.flowable.service.FlowableNodeConfigService;
import com.wkclz.micro.flowable.service.FlowableProcessDesignService;
import com.wkclz.tool.utils.BeanUtil;
import com.wkclz.web.bean.IdReq;
import com.wkclz.web.bean.RemoveReq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "管理端-流程设计", description = "流程设计管理接口")
@RestController
@RequestMapping(Route.PREFIX)
@Validated
public class ProcessDesignRest {

    private static final Logger log = LoggerFactory.getLogger(ProcessDesignRest.class);

    @Autowired
    private FlowableProcessDesignService designService;
    @Autowired
    private FlowableNodeConfigService nodeConfigService;
    @Autowired
    private FlowableClientWrapper clientWrapper;

    @Operation(summary = "上传 BPMN XML 创建设计")
    @PostMapping(Route.ADMIN_DESIGN_UPLOAD)
    @Transactional(rollbackFor = Exception.class)
    public R<DesignUploadResp> upload(@Valid @RequestBody DesignUploadReq req) {
        log.info("上传流程设计: designName={}", req.getDesignName());
        FlowableProcessDesign design = new FlowableProcessDesign();
        design.setDesignCode("FD" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
        design.setDesignName(req.getDesignName());
        design.setCategory(req.getCategory());
        design.setXmlContent(req.getXmlContent());
        design.setFormKey(req.getFormKey());
        design.setDesignVersion(1);
        design.setStatus(DesignStatus.DRAFT.name());
        designService.insert(design);

        // 解析 XML 提取节点，自动生成 node_config
        parseAndCreateNodes(design);

        DesignUploadResp resp = new DesignUploadResp();
        resp.setDesignId(design.getId());
        resp.setDesignCode(design.getDesignCode());
        resp.setVersion(design.getDesignVersion());
        return R.ok(resp);
    }

    @Operation(summary = "设计列表分页")
    @GetMapping(Route.ADMIN_DESIGN_PAGE)
    public R<PageData<DesignPageResp>> page(@Valid DesignPageReq req) {
        FlowableProcessDesign entity = BeanUtil.cp(req, FlowableProcessDesign.class);
        PageData<FlowableProcessDesign> page = designService.getDesignPage(entity);
        PageData<DesignPageResp> respPage = page.convert(DesignPageResp.class);
        return R.ok(respPage);
    }

    @Operation(summary = "设计详情")
    @GetMapping(Route.ADMIN_DESIGN_INFO)
    public R<DesignInfoResp> info(@Valid IdReq req) {
        FlowableProcessDesign design = designService.selectById(req.getId());
        if (design == null) {
            throw ValidationException.of("设计不存在");
        }
        DesignInfoResp resp = BeanUtil.cp(design, DesignInfoResp.class);
        // 查询节点配置
        FlowableNodeConfig nodeParam = new FlowableNodeConfig();
        nodeParam.setDesignId(design.getId());
        List<FlowableNodeConfig> nodes = nodeConfigService.selectByEntity(nodeParam);
        resp.setNodes(BeanUtil.cp(nodes, NodeConfigResp.class));
        return R.ok(resp);
    }

    @Operation(summary = "更新设计")
    @PostMapping(Route.ADMIN_DESIGN_UPDATE)
    @Transactional(rollbackFor = Exception.class)
    public R<Integer> update(@Valid @RequestBody DesignUpdateReq req) {
        log.info("更新流程设计: id={}", req.getId());
        FlowableProcessDesign design = designService.selectById(req.getId());
        if (design == null) {
            throw ValidationException.of("设计不存在");
        }
        FlowableProcessDesign update = new FlowableProcessDesign();
        update.setId(req.getId());
        update.setVersion(req.getVersion());
        if (req.getDesignName() != null) { update.setDesignName(req.getDesignName()); }
        if (req.getCategory() != null) { update.setCategory(req.getCategory()); }
        if (req.getFormKey() != null) { update.setFormKey(req.getFormKey()); }
        if (req.getXmlContent() != null) {
            update.setXmlContent(req.getXmlContent());
            // XML 变更时重新解析节点
            deleteOldNodes(req.getId());
        }
        designService.updateByIdSelective(update);
        if (req.getXmlContent() != null) {
            FlowableProcessDesign saved = designService.selectById(req.getId());
            parseAndCreateNodes(saved);
        }
        return R.ok(1);
    }

    @Operation(summary = "删除设计")
    @PostMapping(Route.ADMIN_DESIGN_REMOVE)
    @Transactional(rollbackFor = Exception.class)
    public R<Integer> remove(@Valid @RequestBody RemoveReq req) {
        log.info("删除流程设计: id={}", req.getId());
        FlowableProcessDesign design = designService.selectById(req.getId());
        if (design == null) {
            throw ValidationException.of("设计不存在");
        }
        deleteOldNodes(req.getId());
        FlowableProcessDesign del = new FlowableProcessDesign();
        del.setId(req.getId());
        return R.ok(designService.deleteById(del));
    }

    @Operation(summary = "推送部署到 flowable")
    @PostMapping(Route.ADMIN_DESIGN_DEPLOY)
    @Transactional(rollbackFor = Exception.class)
    public R<DesignDeployResp> deploy(@Valid @RequestBody DesignDeployReq req) {
        log.info("部署流程设计: id={}", req.getId());
        FlowableProcessDesign design = designService.selectById(req.getId());
        if (design == null) {
            throw ValidationException.of("设计不存在");
        }
        ProcessDeployReq deployReq = new ProcessDeployReq();
        deployReq.setName(design.getDesignCode() + ".bpmn20.xml");
        deployReq.setXmlContent(design.getXmlContent());
        deployReq.setCategory(design.getCategory());

        R<ProcessDeployResp> result = clientWrapper.call(
                ErrorType.DEPLOY_ERROR, "ProcessDeployClient#deploy", deployReq,
                () -> clientWrapper.getClient().getDeploy().deploy(deployReq));

        ProcessDeployResp deployResp = result.getData();
        // 回写部署信息
        FlowableProcessDesign update = new FlowableProcessDesign();
        update.setId(design.getId());
        update.setDeployId(deployResp.getDeployId());
        update.setStatus(DesignStatus.DEPLOYED.name());
        update.setVersion(design.getVersion());
        designService.updateByIdSelective(update);

        DesignDeployResp resp = new DesignDeployResp();
        resp.setDeployId(deployResp.getDeployId());
        resp.setProcDefId(design.getProcDefId());
        return R.ok(resp);
    }

    /**
     * 解析 BPMN XML 提取用户任务节点，自动生成 node_config 记录。
     * 使用 Flowable BPMN Model API（flowable-bpmn-model 由 sh-flowable-client 传递依赖引入）。
     * 若解析失败仅记录日志，不阻塞设计保存。
     */
    private void parseAndCreateNodes(FlowableProcessDesign design) {
        try {
            org.flowable.bpmn.model.BpmnModel model = new org.flowable.bpmn.converter.BpmnXMLConverter()
                    .convertToBpmnModel(
                            () -> new java.io.ByteArrayInputStream(design.getXmlContent().getBytes(java.nio.charset.StandardCharsets.UTF_8)),
                            false, false);
            int order = 0;
            for (org.flowable.bpmn.model.Process process : model.getProcesses()) {
                for (org.flowable.bpmn.model.FlowElement element : process.getFlowElements()) {
                    FlowableNodeConfig node = new FlowableNodeConfig();
                    node.setDesignId(design.getId());
                    node.setNodeKey(element.getId());
                    node.setNodeName(element.getName() != null ? element.getName() : element.getId());
                    node.setOrderNum(order++);
                    if (element instanceof org.flowable.bpmn.model.StartEvent) {
                        node.setNodeType(NodeType.START.name());
                    } else if (element instanceof org.flowable.bpmn.model.UserTask) {
                        node.setNodeType(NodeType.APPROVAL.name());
                    } else if (element instanceof org.flowable.bpmn.model.Gateway) {
                        node.setNodeType(NodeType.GATEWAY.name());
                    } else if (element instanceof org.flowable.bpmn.model.EndEvent) {
                        node.setNodeType(NodeType.END.name());
                    } else {
                        node.setNodeType(NodeType.APPROVAL.name());
                    }
                    nodeConfigService.insert(node);
                }
            }
            log.info("解析 BPMN XML 生成 {} 个节点配置: designId={}", order, design.getId());
        } catch (Exception e) {
            log.warn("解析 BPMN XML 失败，不阻塞设计保存: designId={}", design.getId(), e);
        }
    }

    private void deleteOldNodes(Long designId) {
        FlowableNodeConfig param = new FlowableNodeConfig();
        param.setDesignId(designId);
        List<FlowableNodeConfig> oldNodes = nodeConfigService.selectByEntity(param);
        for (FlowableNodeConfig old : oldNodes) {
            FlowableNodeConfig del = new FlowableNodeConfig();
            del.setId(old.getId());
            nodeConfigService.deleteById(del);
        }
    }
}
