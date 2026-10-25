package com.wkclz.micro.flowable.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.core.identity.IdentityContext;
import com.wkclz.flowable.client.bean.req.TaskCompleteReq;
import com.wkclz.flowable.client.bean.req.TaskIdReq;
import com.wkclz.flowable.client.bean.req.TaskPageReq;
import com.wkclz.flowable.client.bean.resp.TaskPageResp;
import com.wkclz.flowable.client.bean.resp.TaskResp;
import com.wkclz.flowable.client.config.FlowableClient;
import com.wkclz.micro.flowable.bean.entity.FlowableApply;
import com.wkclz.micro.flowable.bean.entity.FlowableApproval;
import com.wkclz.micro.flowable.bean.entity.FlowableNodeConfig;
import com.wkclz.micro.flowable.bean.enums.ApprovalAction;
import com.wkclz.micro.flowable.bean.req.ApprovalListReq;
import com.wkclz.micro.flowable.bean.resp.ApprovalResp;
import com.wkclz.micro.flowable.service.FlowableApplyService;
import com.wkclz.micro.flowable.service.FlowableApprovalService;
import com.wkclz.micro.flowable.service.FlowableNodeConfigService;
import com.wkclz.tool.utils.BeanUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "5.业务端-任务审批", description = "待办/已办/审批流转/审批意见")
@RestController
@RequestMapping(Route.PREFIX)
@Validated
public class FlowableTaskRest {

    private static final Logger log = LoggerFactory.getLogger(FlowableTaskRest.class);

    @Autowired
    private FlowableClient flowableClient;
    @Autowired
    private FlowableApprovalService approvalService;
    @Autowired
    private FlowableApplyService applyService;
    @Autowired
    private FlowableNodeConfigService nodeConfigService;

    @Operation(summary = "1.任务审批-待办分页查询")
    @GetMapping(Route.TASK_TODO_PAGE)
    public R<PageData<TaskPageResp>> todoPage(@Valid TaskPageReq req) {
        R<PageData<TaskPageResp>> result = flowableClient.getTask().todoPage(req);
        fillTaskInfo(result.getData(), TaskPageResp::getBusinessKey, TaskPageResp::getTaskDefinitionKey,
                TaskPageResp::getAssignee,
                (r, a) -> { r.setBusinessType(a.getBusinessType()); r.setBusinessSummary(a.getBusinessSummary()); },
                (r, text) -> r.setAssigneeText(text));
        return result;
    }

    @Operation(summary = "2.任务审批-已办分页查询")
    @GetMapping(Route.TASK_DONE_PAGE)
    public R<PageData<TaskPageResp>> donePage(@Valid TaskPageReq req) {
        // 已办固定按当前用户过滤
        req.setAssignee(IdentityContext.getUserCode());
        R<PageData<TaskPageResp>> result = flowableClient.getTask().donePage(req);
        fillTaskInfo(result.getData(), TaskPageResp::getBusinessKey, TaskPageResp::getTaskDefinitionKey,
                TaskPageResp::getAssignee,
                (r, a) -> { r.setBusinessType(a.getBusinessType()); r.setBusinessSummary(a.getBusinessSummary()); },
                (r, text) -> r.setAssigneeText(text));
        return result;
    }

    @Operation(summary = "3.任务审批-详情")
    @GetMapping(Route.TASK_INFO)
    public R<TaskResp> taskInfo(@Valid TaskIdReq req) {
        return flowableClient.getTask().info(req);
    }

    @Operation(summary = "4.任务审批-通过", description = "完成任务（审批通过）")
    @PostMapping(Route.TASK_COMPLETE)
    @Transactional(rollbackFor = Exception.class)
    public R<Integer> complete(@Valid @RequestBody TaskCompleteReq req) {
        log.info("完成任务: taskId={}", req.getTaskId());
        R<Integer> result = flowableClient.getTask().complete(req);
        recordApproval(req.getTaskId(), null, ApprovalAction.APPROVE, req.getComment(), null);
        return result;
    }

    @Operation(summary = "5.任务审批-认领")
    @PostMapping(Route.TASK_CLAIM)
    @Transactional(rollbackFor = Exception.class)
    public R<Integer> claim(@Valid @RequestBody TaskIdReq req) {
        log.info("认领任务: taskId={}", req.getTaskId());
        R<Integer> result = flowableClient.getTask().claim(req);
        recordApproval(req.getTaskId(), null, ApprovalAction.CLAIM, null, null);
        return result;
    }

    @Operation(summary = "6.任务审批-取消认领")
    @PostMapping(Route.TASK_UNCLAIM)
    public R<Integer> unclaim(@Valid @RequestBody TaskIdReq req) {
        log.info("取消认领: taskId={}", req.getTaskId());
        return flowableClient.getTask().unclaim(req);
    }

    @Operation(summary = "7.任务审批-意见时间线")
    @GetMapping(Route.APPROVAL_LIST)
    public R<List<ApprovalResp>> approvalList(@Valid ApprovalListReq req) {
        FlowableApproval param = new FlowableApproval();
        if (req.getProcInsId() != null) { param.setProcInsId(req.getProcInsId()); }
        if (req.getApplyId() != null) { param.setApplyId(req.getApplyId()); }
        List<FlowableApproval> list = approvalService.selectByEntity(param);
        return R.ok(BeanUtil.cp(list, ApprovalResp.class));
    }

    @Operation(summary = "8.任务审批-驳回")
    @PostMapping(Route.TASK_REJECT)
    @Transactional(rollbackFor = Exception.class)
    public R<Integer> reject(@Valid @RequestBody com.wkclz.micro.flowable.bean.req.TaskRejectReq req) {
        log.info("驳回任务: taskId={}", req.getTaskId());
        com.wkclz.flowable.client.bean.req.TaskRejectReq clientReq = new com.wkclz.flowable.client.bean.req.TaskRejectReq();
        clientReq.setTaskId(req.getTaskId());
        clientReq.setComment(req.getComment());
        clientReq.setTargetNodeKey(req.getTargetNodeKey());
        R<Integer> result = flowableClient.getTask().reject(clientReq);
        recordApproval(req.getTaskId(), null, ApprovalAction.REJECT, req.getComment(), null);
        return result;
    }

    @Operation(summary = "9.任务审批-转办")
    @PostMapping(Route.TASK_TRANSFER)
    @Transactional(rollbackFor = Exception.class)
    public R<Integer> transfer(@Valid @RequestBody com.wkclz.micro.flowable.bean.req.TaskTransferReq req) {
        log.info("转办任务: taskId={}, targetUserId={}", req.getTaskId(), req.getTargetUserId());
        com.wkclz.flowable.client.bean.req.TaskTransferReq clientReq = new com.wkclz.flowable.client.bean.req.TaskTransferReq();
        clientReq.setTaskId(req.getTaskId());
        clientReq.setTargetUserId(req.getTargetUserId());
        clientReq.setComment(req.getComment());
        R<Integer> result = flowableClient.getTask().transfer(clientReq);
        recordApproval(req.getTaskId(), null, ApprovalAction.TRANSFER, req.getComment(), req.getTargetUserId());
        return result;
    }

    @Operation(summary = "10.任务审批-委派")
    @PostMapping(Route.TASK_DELEGATE)
    @Transactional(rollbackFor = Exception.class)
    public R<Integer> delegate(@Valid @RequestBody com.wkclz.micro.flowable.bean.req.TaskDelegateReq req) {
        log.info("委派任务: taskId={}, targetUserId={}", req.getTaskId(), req.getTargetUserId());
        com.wkclz.flowable.client.bean.req.TaskDelegateReq clientReq = new com.wkclz.flowable.client.bean.req.TaskDelegateReq();
        clientReq.setTaskId(req.getTaskId());
        clientReq.setTargetUserId(req.getTargetUserId());
        clientReq.setComment(req.getComment());
        R<Integer> result = flowableClient.getTask().delegate(clientReq);
        recordApproval(req.getTaskId(), null, ApprovalAction.DELEGATE, req.getComment(), req.getTargetUserId());
        return result;
    }

    /**
     * 回填任务行业务信息：按申请单号（businessKey）反查申请单回填业务类型/摘要，
     * 并按审批人类型计算处理人展示文本（引擎受理人优先）
     */
    private <T> void fillTaskInfo(PageData<T> page, java.util.function.Function<T, String> businessKeyGetter,
                                  java.util.function.Function<T, String> taskDefinitionKeyGetter,
                                  java.util.function.Function<T, String> assigneeGetter,
                                  java.util.function.BiConsumer<T, FlowableApply> applySetter,
                                  java.util.function.BiConsumer<T, String> assigneeTextSetter) {
        if (page == null || page.getRecords() == null) {
            return;
        }
        Map<String, List<FlowableNodeConfig>> nodeCache = new HashMap<>();
        for (T row : page.getRecords()) {
            String applyCode = businessKeyGetter.apply(row);
            if (applyCode == null || applyCode.isEmpty()) {
                continue;
            }
            FlowableApply param = new FlowableApply();
            param.setApplyCode(applyCode);
            FlowableApply apply = applyService.selectOneByEntity(param);
            if (apply == null) {
                continue;
            }
            applySetter.accept(row, apply);
            assigneeTextSetter.accept(row, calcAssigneeText(apply, taskDefinitionKeyGetter.apply(row), assigneeGetter.apply(row), nodeCache));
        }
    }

    /**
     * 按审批人类型计算处理人展示文本：引擎受理人优先；USER/ROLE/DEPT 取配置值，STARTER 取发起人，其余 '-'
     */
    private String calcAssigneeText(FlowableApply apply, String nodeKey, String assignee, Map<String, List<FlowableNodeConfig>> nodeCache) {
        if (assignee != null && !assignee.isEmpty()) {
            return assignee;
        }
        if (nodeKey == null || nodeKey.isEmpty()) {
            return "-";
        }
        List<FlowableNodeConfig> nodes = nodeCache.computeIfAbsent(apply.getDesignCode(),
                dc -> nodeConfigService.selectByDesignCode(dc));
        FlowableNodeConfig node = nodes.stream()
                .filter(n -> nodeKey.equals(n.getNodeKey())).findFirst().orElse(null);
        if (node == null || node.getAssigneeType() == null || node.getAssigneeType().isEmpty()) {
            return "-";
        }
        switch (node.getAssigneeType()) {
            case "USER":
            case "ROLE":
            case "DEPT":
                return node.getAssigneeValue() != null && !node.getAssigneeValue().isEmpty() ? node.getAssigneeValue() : "-";
            case "STARTER":
                return apply.getStartUserId() != null ? apply.getStartUserId() : "-";
            default:
                return "-";
        }
    }

    /**
     * 记录审批意见
     */
    private void recordApproval(String taskId, String procInsId, ApprovalAction action, String comment, String targetUserId) {
        try {
            FlowableApproval approval = new FlowableApproval();
            approval.setTaskId(taskId);
            approval.setProcInsId(procInsId != null ? procInsId : "");
            approval.setApproverId(IdentityContext.getUserCode());
            approval.setAction(action.name());
            approval.setComment(comment);
            approval.setTargetUserId(targetUserId);
            // 通过 taskId 查找关联的 apply
            if (procInsId != null) {
                FlowableApply applyParam = new FlowableApply();
                applyParam.setProcInsId(procInsId);
                FlowableApply apply = applyService.selectOneByEntity(applyParam);
                if (apply != null) {
                    approval.setApplyId(apply.getId());
                }
            }
            approvalService.insert(approval);
        } catch (Exception e) {
            log.error("记录审批意见失败: taskId={}, action={}", taskId, action, e);
        }
    }
}
