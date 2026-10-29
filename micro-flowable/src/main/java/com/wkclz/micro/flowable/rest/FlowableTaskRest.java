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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
        TaskResp task = getTaskInfo(req.getTaskId());
        R<Integer> result = flowableClient.getTask().complete(req);
        recordApproval(req.getTaskId(), null, ApprovalAction.APPROVE, req.getComment(), null, task);
        return result;
    }

    @Operation(summary = "5.任务审批-认领")
    @PostMapping(Route.TASK_CLAIM)
    @Transactional(rollbackFor = Exception.class)
    public R<Integer> claim(@Valid @RequestBody TaskIdReq req) {
        log.info("认领任务: taskId={}", req.getTaskId());
        TaskResp task = getTaskInfo(req.getTaskId());
        R<Integer> result = flowableClient.getTask().claim(req);
        recordApproval(req.getTaskId(), null, ApprovalAction.CLAIM, null, null, task);
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
        TaskResp task = getTaskInfo(req.getTaskId());
        R<Integer> result = flowableClient.getTask().reject(clientReq);
        recordApproval(req.getTaskId(), null, ApprovalAction.REJECT, req.getComment(), null, task);
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
        TaskResp task = getTaskInfo(req.getTaskId());
        R<Integer> result = flowableClient.getTask().transfer(clientReq);
        recordApproval(req.getTaskId(), null, ApprovalAction.TRANSFER, req.getComment(), req.getTargetUserId(), task);
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
        TaskResp task = getTaskInfo(req.getTaskId());
        R<Integer> result = flowableClient.getTask().delegate(clientReq);
        recordApproval(req.getTaskId(), null, ApprovalAction.DELEGATE, req.getComment(), req.getTargetUserId(), task);
        return result;
    }

    /**
     * 回填任务行业务信息：申请单号、设计编码各一次批量查询，回填业务类型/摘要，
     * 处理人展示文本按审批人类型计算（引擎受理人优先）
     */
    private <T> void fillTaskInfo(PageData<T> page, java.util.function.Function<T, String> businessKeyGetter,
                                  java.util.function.Function<T, String> taskDefinitionKeyGetter,
                                  java.util.function.Function<T, String> assigneeGetter,
                                  java.util.function.BiConsumer<T, FlowableApply> applySetter,
                                  java.util.function.BiConsumer<T, String> assigneeTextSetter) {
        if (page == null || page.getRecords() == null) {
            return;
        }
        log.info("任务业务信息回填: rows={}", page.getRecords().size());
        Set<String> applyCodes = page.getRecords().stream()
                .map(businessKeyGetter)
                .filter(c -> c != null && !c.isEmpty())
                .collect(Collectors.toSet());
        Map<String, FlowableApply> applyMap = applyService.selectByApplyCodes(applyCodes).stream()
                .collect(Collectors.toMap(FlowableApply::getApplyCode, a -> a, (a, b) -> a));
        Set<String> designCodes = applyMap.values().stream()
                .map(FlowableApply::getDesignCode)
                .filter(c -> c != null && !c.isEmpty())
                .collect(Collectors.toSet());
        Map<String, List<FlowableNodeConfig>> nodeMap = nodeConfigService.selectByDesignCodes(designCodes).stream()
                .filter(n -> n.getDesignCode() != null)
                .collect(Collectors.groupingBy(FlowableNodeConfig::getDesignCode));
        for (T row : page.getRecords()) {
            FlowableApply apply = applyMap.get(businessKeyGetter.apply(row));
            if (apply == null) {
                continue;
            }
            applySetter.accept(row, apply);
            assigneeTextSetter.accept(row, nodeConfigService.calcAssigneeText(
                    taskDefinitionKeyGetter.apply(row), assigneeGetter.apply(row), apply.getStartUserId(),
                    nodeMap.get(apply.getDesignCode())));
        }
    }

    /**
     * 记录审批意见
     */
    private void recordApproval(String taskId, String procInsId, ApprovalAction action, String comment, String targetUserId, TaskResp task) {
        try {
            FlowableApproval approval = new FlowableApproval();
            approval.setTaskId(taskId);
            approval.setProcInsId(procInsId != null ? procInsId : "");
            approval.setApproverId(IdentityContext.getUserCode());
            approval.setAction(action.name());
            approval.setComment(comment);
            approval.setTargetUserId(targetUserId);
            if (task != null) {
                approval.setNodeKey(task.getTaskDefinitionKey());
                approval.setNodeName(task.getTaskName());
            }
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

    /**
     * 查询任务详情用于审批意见落库节点信息，失败或任务不存在时返回 null，不阻断主流程
     */
    private TaskResp getTaskInfo(String taskId) {
        try {
            TaskIdReq taskIdReq = new TaskIdReq();
            taskIdReq.setTaskId(taskId);
            return flowableClient.getTask().info(taskIdReq).getData();
        } catch (Exception e) {
            log.error("查询任务详情失败: taskId={}", taskId, e);
            return null;
        }
    }
}
