package com.wkclz.micro.flowable.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.flowable.client.bean.req.ProcessDeployReq;
import com.wkclz.flowable.client.bean.resp.ProcessDeployResp;
import com.wkclz.flowable.client.config.FlowableClient;
import com.wkclz.micro.flowable.bean.entity.FlowableCategory;
import com.wkclz.micro.flowable.bean.entity.FlowableNodeConfig;
import com.wkclz.micro.flowable.bean.entity.FlowableProcessDesign;
import com.wkclz.micro.flowable.bean.enums.DesignStatus;
import com.wkclz.micro.flowable.bean.req.*;
import com.wkclz.micro.flowable.bean.resp.*;
import com.wkclz.micro.flowable.service.FlowableCategoryService;
import com.wkclz.micro.flowable.service.FlowableNodeConfigService;
import com.wkclz.micro.flowable.service.FlowableProcessDesignService;
import com.wkclz.redis.helper.RedisIdGenerator;
import com.wkclz.tool.utils.BeanUtil;
import com.wkclz.web.bean.IdReq;
import com.wkclz.web.bean.RemoveReq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "1.管理端-流程设计", description = "流程设计管理接口")
@RestController
@RequestMapping(Route.PREFIX)
@Validated
public class ProcessDesignRest {


    @Autowired
    private RedisIdGenerator redisIdGenerator;
    @Autowired
    private FlowableClient flowableClient;
    @Autowired
    private FlowableCategoryService categoryService;
    @Autowired
    private FlowableProcessDesignService designService;
    @Autowired
    private FlowableNodeConfigService nodeConfigService;

    @Operation(summary = "1.流程设计-分页查询")
    @GetMapping(Route.ADMIN_DESIGN_PAGE)
    public R<PageData<DesignPageResp>> page(@Valid DesignPageReq req) {
        return R.ok(designService.getDesignPage(req));
    }

    @Operation(summary = "2.流程设计-详情")
    @GetMapping(Route.ADMIN_DESIGN_INFO)
    public R<DesignInfoResp> info(@Valid IdReq req) {
        FlowableProcessDesign design = designService.selectById(req.getId());
        if (design == null) {
            throw ValidationException.of("设计不存在");
        }
        DesignInfoResp resp = BeanUtil.cp(design, DesignInfoResp.class);
        // 查询节点配置
        FlowableNodeConfig nodeParam = new FlowableNodeConfig();
        nodeParam.setDesignCode(design.getDesignCode());
        List<FlowableNodeConfig> nodes = nodeConfigService.selectByEntity(nodeParam);
        resp.setNodes(BeanUtil.cp(nodes, NodeConfigResp.class));
        // 回填分类名称
        resp.setCategoryName(findCategoryName(design.getCategory()));
        return R.ok(resp);
    }

    @Operation(summary = "3.流程设计-创建")
    @PostMapping(Route.ADMIN_DESIGN_CREATE)
    @Transactional(rollbackFor = Exception.class)
    public R<DesignUploadResp> create(@Valid @RequestBody DesignUploadReq req) {
        log.info("上传流程设计: designName={}", req.getDesignName());
        FlowableProcessDesign design = new FlowableProcessDesign();
        design.setDesignCode(redisIdGenerator.generateIdWithPrefix("flow_"));
        design.setDesignName(req.getDesignName());
        design.setCategory(req.getCategory());
        design.setXmlContent(req.getXmlContent());
        design.setFormKey(req.getFormKey());
        design.setDesignVersion(1);
        design.setStatus(DesignStatus.DRAFT.name());
        designService.insert(design);

        // 解析 XML 提取节点，自动生成 node_config（首次创建无存量节点，全部新增）
        nodeConfigService.syncFromXml(design.getDesignCode(), design.getXmlContent());

        DesignUploadResp resp = new DesignUploadResp();
        resp.setDesignId(design.getId());
        resp.setDesignCode(design.getDesignCode());
        resp.setVersion(design.getDesignVersion());
        return R.ok(resp);
    }

    @Operation(summary = "4.流程设计-修改")
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
        boolean xmlChanged = req.getXmlContent() != null && !req.getXmlContent().equals(design.getXmlContent());
        if (xmlChanged) {
            update.setXmlContent(req.getXmlContent());
        }
        designService.updateByIdSelective(update);
        if (xmlChanged) {
            // 仅 XML 变更时差量同步节点，与节点无关的修改不动节点配置
            nodeConfigService.syncFromXml(design.getDesignCode(), req.getXmlContent());
        }
        return R.ok(1);
    }

    @Operation(summary = "5.流程设计-删除")
    @PostMapping(Route.ADMIN_DESIGN_REMOVE)
    @Transactional(rollbackFor = Exception.class)
    public R<Integer> remove(@Valid @RequestBody RemoveReq req) {
        log.info("删除流程设计: id={}", req.getId());
        FlowableProcessDesign design = designService.selectById(req.getId());
        if (design == null) {
            throw ValidationException.of("设计不存在");
        }
        nodeConfigService.deleteByDesignCode(design.getDesignCode());
        FlowableProcessDesign del = new FlowableProcessDesign();
        del.setId(req.getId());
        return R.ok(designService.deleteById(del));
    }

    @Operation(summary = "6.流程设计-部署", description = "推送部署流程设计到 Flowable Server")
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
        // process id/name 归一化为设计表设置，保证引擎 definition key/name 与业务数据一致
        deployReq.setXmlContent(designService.normalizeProcess(design.getXmlContent(), design.getDesignCode(), design.getDesignName()));
        deployReq.setCategory(design.getCategory());

        R<ProcessDeployResp> result = flowableClient.getDeploy().deploy(deployReq);

        ProcessDeployResp deployResp = result.getData();
        String procDefId = deployResp.getProcDefId();
        // 回写部署信息
        FlowableProcessDesign update = new FlowableProcessDesign();
        update.setId(design.getId());
        update.setDeployId(deployResp.getDeployId());
        update.setProcDefId(procDefId);
        update.setStatus(DesignStatus.DEPLOYED.name());
        update.setVersion(design.getVersion());
        designService.updateByIdSelective(update);

        DesignDeployResp resp = new DesignDeployResp();
        resp.setDeployId(deployResp.getDeployId());
        resp.setProcDefId(procDefId);
        return R.ok(resp);
    }

    private String findCategoryName(String categoryCode) {
        if (categoryCode == null || categoryCode.isEmpty()) {
            return null;
        }
        FlowableCategory query = new FlowableCategory();
        query.setCategoryCode(categoryCode);
        List<FlowableCategory> categories = categoryService.selectByEntity(query);
        return categories.isEmpty() ? null : categories.get(0).getCategoryName();
    }
}
