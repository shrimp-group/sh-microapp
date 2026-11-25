package com.wkclz.micro.flowable.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.core.identity.IdentityContext;
import com.wkclz.flowable.client.bean.req.ProcessStartReq;
import com.wkclz.flowable.client.bean.req.ProcInsIdsReq;
import com.wkclz.flowable.client.bean.resp.ActiveTaskResp;
import com.wkclz.flowable.client.bean.resp.InstanceActiveTaskResp;
import com.wkclz.flowable.client.bean.resp.ProcessInstanceResp;
import com.wkclz.flowable.client.config.FlowableClient;
import com.wkclz.micro.flowable.bean.entity.FlowableApply;
import com.wkclz.micro.flowable.bean.entity.FlowableApproval;
import com.wkclz.micro.flowable.bean.entity.FlowableNodeConfig;
import com.wkclz.micro.flowable.bean.entity.FlowableProcessDesign;
import com.wkclz.micro.flowable.bean.constant.FlowableConstants;
import com.wkclz.micro.flowable.bean.enums.ApplyStatus;
import com.wkclz.micro.flowable.bean.enums.ApprovalAction;
import com.wkclz.micro.flowable.bean.req.ApplyCreateReq;
import com.wkclz.micro.flowable.bean.req.ApplyPageReq;
import com.wkclz.micro.flowable.bean.resp.ApplyCreateResp;
import com.wkclz.micro.flowable.bean.resp.ApplyInfoResp;
import com.wkclz.micro.flowable.bean.resp.ApplyPageResp;
import com.wkclz.micro.flowable.service.FlowableApplyService;
import com.wkclz.micro.flowable.service.FlowableApprovalService;
import com.wkclz.micro.flowable.service.FlowableNodeConfigService;
import com.wkclz.micro.flowable.service.FlowableProcessDesignService;
import com.wkclz.redis.helper.RedisIdGenerator;
import com.wkclz.tool.utils.BeanUtil;
import com.wkclz.web.bean.IdReq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Tag(name = "4.业务端-流程申请", description = "流程申请管理接口")
@RestController
@RequestMapping(Route.PREFIX)
@Validated
public class ApplyRest {


    @Resource
    private FlowableClient flowableClient;
    @Resource
    private RedisIdGenerator redisIdGenerator;
    @Resource
    private FlowableApplyService applyService;
    @Resource
    private FlowableProcessDesignService designService;
    @Resource
    private FlowableNodeConfigService nodeConfigService;
    @Resource
    private FlowableApprovalService approvalService;

    @Operation(summary = "1.流程申请-发起")
    @PostMapping(Route.APPLY_CREATE)
    @Transactional(rollbackFor = Exception.class)
    public R<ApplyCreateResp> create(@Valid @RequestBody ApplyCreateReq req) {
        String userCode = IdentityContext.getUserCode();
        log.info("发起流程申请: designCode={}, user={}", req.getDesignCode(), userCode);

        // 查询设计
        FlowableProcessDesign design = designService.selectByDesignCode(req.getDesignCode());
        if (design == null) {
            throw ValidationException.of("流程设计不存在: {}", req.getDesignCode());
        }

        // 创建申请单
        FlowableApply apply = createApply(req, ApplyStatus.RUNNING.name());
        String applyCode = apply.getApplyCode();

        // 启动流程
        ProcessStartReq startReq = new ProcessStartReq();
        startReq.setDefinitionKey(design.getDesignCode());
        startReq.setBusinessKey(applyCode);
        startReq.setVariables(req.getVariables());

        R<ProcessInstanceResp> result = flowableClient.getInstance().start(startReq);

        // 回填流程实例 ID（insert 不回填 version，乐观锁需先查询再更新）
        FlowableApply dbApply = applyService.selectById(apply.getId());
        ProcessInstanceResp instanceResp = result.getData();
        dbApply.setProcInsId(instanceResp.getProcessInstanceId());
        dbApply.setProcDefId(instanceResp.getDefinitionKey());
        applyService.updateByIdSelective(dbApply);

        recordSubmit(apply.getId(), instanceResp.getProcessInstanceId(), userCode);

        ApplyCreateResp resp = new ApplyCreateResp();
        resp.setApplyCode(applyCode);
        resp.setProcInsId(instanceResp.getProcessInstanceId());
        return R.ok(resp);
    }

    @Operation(summary = "2.流程申请-我的申请", description = "分页查询当前用户发起的流程申请")
    @GetMapping(Route.APPLY_PAGE)
    public R<PageData<ApplyPageResp>> page(@Valid ApplyPageReq req) {
        FlowableApply entity = BeanUtil.cp(req, FlowableApply.class);
        entity.setStartUserId(IdentityContext.getUserCode());
        PageData<ApplyPageResp> respPage = applyService.getApplyPage(entity);
        fillCurrentNode(respPage.getRecords());
        return R.ok(respPage);
    }

    @Operation(summary = "3.流程申请-详情")
    @GetMapping(Route.APPLY_INFO)
    public R<ApplyInfoResp> info(@Valid IdReq req) {
        ApplyInfoResp resp = applyService.selectInfoById(req.getId());
        if (resp == null) {
            log.warn("申请详情不存在: id={}", req.getId());
            throw ValidationException.of("申请单不存在");
        }
        fillCurrentNode(Collections.singletonList(resp));
        log.info("申请详情回填: id={}, currentNodeName={}, currentAssignee={}", resp.getId(), resp.getCurrentNodeName(), resp.getCurrentAssignee());
        return R.ok(resp);
    }

    @Operation(summary = "4.流程申请-存草稿")
    @PostMapping(Route.APPLY_CREATE_DRAFT)
    public R<ApplyCreateResp> createDraft(@Valid @RequestBody ApplyCreateReq req) {
        log.info("保存申请草稿: designCode={}, user={}", req.getDesignCode(), IdentityContext.getUserCode());
        if (designService.selectByDesignCode(req.getDesignCode()) == null) {
            log.warn("保存申请草稿失败, 流程设计不存在: designCode={}", req.getDesignCode());
            throw ValidationException.of("流程设计不存在: {}", req.getDesignCode());
        }
        FlowableApply apply = createApply(req, ApplyStatus.DRAFT.name());
        ApplyCreateResp resp = new ApplyCreateResp();
        resp.setApplyCode(apply.getApplyCode());
        return R.ok(resp);
    }

    @Operation(summary = "5.流程申请-草稿提交")
    @PostMapping(Route.APPLY_SUBMIT)
    @Transactional(rollbackFor = Exception.class)
    public R<ApplyCreateResp> submit(@Valid @RequestBody IdReq req) {
        String userCode = IdentityContext.getUserCode();
        log.info("提交申请草稿: id={}, user={}", req.getId(), userCode);
        FlowableApply apply = applyService.selectById(req.getId());
        if (apply == null) {
            throw ValidationException.of("申请单不存在");
        }
        if (!apply.getStatus().equals(ApplyStatus.DRAFT.name())) {
            log.warn("提交申请草稿失败, 非草稿状态: id={}, status={}", req.getId(), apply.getStatus());
            throw ValidationException.of("仅草稿可提交");
        }
        if (!apply.getStartUserId().equals(userCode)) {
            log.warn("提交申请草稿失败, 非本人申请单: id={}, startUser={}, user={}", req.getId(), apply.getStartUserId(), userCode);
            throw ValidationException.of("无权操作他人申请单");
        }
        FlowableProcessDesign design = designService.selectByDesignCode(apply.getDesignCode());
        if (design == null) {
            log.warn("提交申请草稿失败, 流程设计不存在: designCode={}", apply.getDesignCode());
            throw ValidationException.of("流程设计不存在: {}", apply.getDesignCode());
        }
        ProcessStartReq startReq = new ProcessStartReq();
        startReq.setDefinitionKey(design.getDesignCode());
        startReq.setBusinessKey(apply.getApplyCode());
        startReq.setVariables(null);
        R<ProcessInstanceResp> result = flowableClient.getInstance().start(startReq);
        FlowableApply update = new FlowableApply();
        update.setId(apply.getId());
        update.setVersion(apply.getVersion());
        update.setProcInsId(result.getData().getProcessInstanceId());
        update.setProcDefId(result.getData().getDefinitionKey());
        update.setStatus(ApplyStatus.RUNNING.name());
        applyService.updateByIdSelective(update);

        recordSubmit(apply.getId(), result.getData().getProcessInstanceId(), userCode);

        ApplyCreateResp resp = new ApplyCreateResp();
        resp.setApplyCode(apply.getApplyCode());
        resp.setProcInsId(result.getData().getProcessInstanceId());
        return R.ok(resp);
    }

    /** 创建申请单，applyCode 由 redisIdGenerator 生成 */
    private FlowableApply createApply(ApplyCreateReq req, String status) {
        FlowableApply apply = new FlowableApply();
        apply.setApplyCode(redisIdGenerator.generateIdWithPrefix(FlowableConstants.APPLY_CODE_PREFIX));
        apply.setDesignCode(req.getDesignCode());
        apply.setBusinessType(req.getBusinessType());
        apply.setBusinessSummary(req.getBusinessSummary());
        apply.setBusinessData(req.getBusinessData());
        apply.setStartUserId(IdentityContext.getUserCode());
        apply.setStatus(status);
        applyService.insert(apply);
        return apply;
    }

    /** 记录流程发起审批意见（发起人、流程实例、节点名固定为"发起"） */
    private void recordSubmit(Long applyId, String procInsId, String approverId) {
        try {
            FlowableApproval approval = new FlowableApproval();
            approval.setApplyId(applyId);
            approval.setProcInsId(procInsId);
            approval.setApproverId(approverId);
            approval.setAction(ApprovalAction.SUBMIT.name());
            approval.setNodeName("发起");
            approvalService.insert(approval);
        } catch (Exception e) {
            log.error("记录发起审批意见失败: applyId={}, procInsId={}", applyId, procInsId, e);
        }
    }

    /**
     * 回填运行中申请的当前审批节点与审批人：活动任务、节点配置各一次批量查询；
     * 节点名引擎侧已合并直接映射；审批人按节点配置计算后去重连接，全空显示 '-'；无活动任务的行保持 null
     */
    private void fillCurrentNode(List<? extends ApplyPageResp> rows) {
        List<String> procInsIds = new ArrayList<>();
        for (ApplyPageResp row : rows) {
            boolean running = ApplyStatus.RUNNING.name().equals(row.getStatus())
                    && row.getProcInsId() != null && !row.getProcInsId().isEmpty();
            if (running && !procInsIds.contains(row.getProcInsId())) {
                procInsIds.add(row.getProcInsId());
            }
        }
        if (procInsIds.isEmpty()) {
            return;
        }
        ProcInsIdsReq activeReq = new ProcInsIdsReq();
        activeReq.setProcessInstanceIds(procInsIds);
        List<InstanceActiveTaskResp> assemblies = flowableClient.getTask().activeTaskList(activeReq).getData();
        if (assemblies == null || assemblies.isEmpty()) {
            return;
        }
        Map<String, InstanceActiveTaskResp> assemblyMap = assemblies.stream()
                .filter(a -> a.getProcessInstanceId() != null)
                .collect(Collectors.toMap(InstanceActiveTaskResp::getProcessInstanceId, a -> a, (a, b) -> a));
        Set<String> designCodes = new HashSet<>();
        for (ApplyPageResp row : rows) {
            if (assemblyMap.containsKey(row.getProcInsId())
                    && row.getDesignCode() != null && !row.getDesignCode().isEmpty()) {
                designCodes.add(row.getDesignCode());
            }
        }
        Map<String, List<FlowableNodeConfig>> nodeMap = nodeConfigService.groupByDesignCode(designCodes);
        int filled = 0;
        for (ApplyPageResp row : rows) {
            InstanceActiveTaskResp assembly = assemblyMap.get(row.getProcInsId());
            if (assembly == null) {
                continue;
            }
            row.setCurrentNodeName(assembly.getCurrentNodeName());
            Set<String> assignees = new LinkedHashSet<>();
            for (ActiveTaskResp task : assembly.getTasks()) {
                String text = nodeConfigService.calcAssigneeText(
                        task.getTaskDefinitionKey(), task.getAssignee(), row.getStartUserId(), nodeMap.get(row.getDesignCode()));
                if (text != null && !text.isEmpty() && !FlowableConstants.EMPTY_ASSIGNEE_PLACEHOLDER.equals(text)) {
                    assignees.add(text);
                }
            }
            row.setCurrentAssignee(assignees.isEmpty() ? FlowableConstants.EMPTY_ASSIGNEE_PLACEHOLDER : String.join("、", assignees));
            filled++;
        }
        log.info("回填申请当前审批节点: rows={}, filled={}", rows.size(), filled);
    }
}
