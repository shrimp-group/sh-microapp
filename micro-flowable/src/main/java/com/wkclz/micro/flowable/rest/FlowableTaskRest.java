package com.wkclz.micro.flowable.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.core.identity.IdentityContext;
import com.wkclz.flowable.client.bean.req.TaskCompleteReq;
import com.wkclz.flowable.client.bean.req.TaskPageReq;
import com.wkclz.flowable.client.bean.resp.TaskPageResp;
import com.wkclz.flowable.client.bean.resp.TaskResp;
import com.wkclz.micro.flowable.bean.entity.FlowableApply;
import com.wkclz.micro.flowable.bean.entity.FlowableApproval;
import com.wkclz.micro.flowable.bean.enums.ApprovalAction;
import com.wkclz.micro.flowable.bean.enums.ErrorType;
import com.wkclz.micro.flowable.bean.req.ApprovalListReq;
import com.wkclz.micro.flowable.bean.resp.ApprovalResp;
import com.wkclz.micro.flowable.service.FlowableApplyService;
import com.wkclz.micro.flowable.service.FlowableApprovalService;
import com.wkclz.micro.flowable.service.FlowableClientWrapper;
import com.wkclz.tool.utils.BeanUtil;
import com.wkclz.web.bean.IdReq;
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

@Tag(name = "业务端-任务审批", description = "待办/已办/审批流转/审批意见")
@RestController
@RequestMapping(Route.PREFIX)
@Validated
public class FlowableTaskRest {

    private static final Logger log = LoggerFactory.getLogger(FlowableTaskRest.class);

    @Autowired
    private FlowableClientWrapper clientWrapper;
    @Autowired
    private FlowableApprovalService approvalService;
    @Autowired
    private FlowableApplyService applyService;

    @Operation(summary = "待办任务分页")
    @GetMapping(Route.TASK_TODO_PAGE)
    public R<PageData<TaskPageResp>> todoPage(@Valid TaskPageReq req) {
        return clientWrapper.call(ErrorType.QUERY_ERROR, "TaskClient#todoPage", req,
                () -> clientWrapper.getClient().getTask().todoPage(req));
    }

    @Operation(summary = "已办任务分页")
    @GetMapping(Route.TASK_DONE_PAGE)
    public R<PageData<TaskPageResp>> donePage(@Valid TaskPageReq req) {
        return clientWrapper.call(ErrorType.QUERY_ERROR, "TaskClient#donePage", req,
                () -> clientWrapper.getClient().getTask().donePage(req));
    }

    @Operation(summary = "任务详情")
    @GetMapping(Route.TASK_INFO)
    public R<TaskResp> taskInfo(@Valid IdReq req) {
        return clientWrapper.call(ErrorType.QUERY_ERROR, "TaskClient#info", req,
                () -> clientWrapper.getClient().getTask().info(req));
    }

    @Operation(summary = "完成任务（通过）")
    @PostMapping(Route.TASK_COMPLETE)
    @Transactional(rollbackFor = Exception.class)
    public R<Integer> complete(@Valid @RequestBody TaskCompleteReq req) {
        log.info("完成任务: taskId={}", req.getTaskId());
        R<Integer> result = clientWrapper.call(ErrorType.APPROVE_ERROR, "TaskClient#complete", req,
                () -> clientWrapper.getClient().getTask().complete(req));
        recordApproval(req.getTaskId(), null, ApprovalAction.APPROVE, req.getComment(), null);
        return result;
    }

    @Operation(summary = "认领任务")
    @PostMapping(Route.TASK_CLAIM)
    @Transactional(rollbackFor = Exception.class)
    public R<Integer> claim(@Valid @RequestBody IdReq req) {
        log.info("认领任务: taskId={}", req.getId());
        R<Integer> result = clientWrapper.call(ErrorType.APPROVE_ERROR, "TaskClient#claim", req,
                () -> clientWrapper.getClient().getTask().claim(req));
        recordApproval(String.valueOf(req.getId()), null, ApprovalAction.CLAIM, null, null);
        return result;
    }

    @Operation(summary = "取消认领")
    @PostMapping(Route.TASK_UNCLAIM)
    public R<Integer> unclaim(@Valid @RequestBody IdReq req) {
        log.info("取消认领: taskId={}", req.getId());
        return clientWrapper.call(ErrorType.APPROVE_ERROR, "TaskClient#unclaim", req,
                () -> clientWrapper.getClient().getTask().unclaim(req));
    }

    @Operation(summary = "审批意见时间线")
    @GetMapping(Route.APPROVAL_LIST)
    public R<List<ApprovalResp>> approvalList(@Valid ApprovalListReq req) {
        FlowableApproval param = new FlowableApproval();
        if (req.getProcInsId() != null) { param.setProcInsId(req.getProcInsId()); }
        if (req.getApplyId() != null) { param.setApplyId(req.getApplyId()); }
        List<FlowableApproval> list = approvalService.selectByEntity(param);
        return R.ok(BeanUtil.cp(list, ApprovalResp.class));
    }

    @Operation(summary = "驳回任务")
    @PostMapping(Route.TASK_REJECT)
    @Transactional(rollbackFor = Exception.class)
    public R<Integer> reject(@Valid @RequestBody com.wkclz.micro.flowable.bean.req.TaskRejectReq req) {
        log.info("驳回任务: taskId={}", req.getTaskId());
        com.wkclz.flowable.client.bean.req.TaskRejectReq clientReq = new com.wkclz.flowable.client.bean.req.TaskRejectReq();
        clientReq.setTaskId(req.getTaskId());
        clientReq.setComment(req.getComment());
        clientReq.setTargetNodeKey(req.getTargetNodeKey());
        R<Integer> result = clientWrapper.call(ErrorType.APPROVE_ERROR, "TaskClient#reject", clientReq,
                () -> clientWrapper.getClient().getTask().reject(clientReq));
        recordApproval(req.getTaskId(), null, ApprovalAction.REJECT, req.getComment(), null);
        return result;
    }

    @Operation(summary = "转办任务")
    @PostMapping(Route.TASK_TRANSFER)
    @Transactional(rollbackFor = Exception.class)
    public R<Integer> transfer(@Valid @RequestBody com.wkclz.micro.flowable.bean.req.TaskTransferReq req) {
        log.info("转办任务: taskId={}, targetUserId={}", req.getTaskId(), req.getTargetUserId());
        com.wkclz.flowable.client.bean.req.TaskTransferReq clientReq = new com.wkclz.flowable.client.bean.req.TaskTransferReq();
        clientReq.setTaskId(req.getTaskId());
        clientReq.setTargetUserId(req.getTargetUserId());
        clientReq.setComment(req.getComment());
        R<Integer> result = clientWrapper.call(ErrorType.APPROVE_ERROR, "TaskClient#transfer", clientReq,
                () -> clientWrapper.getClient().getTask().transfer(clientReq));
        recordApproval(req.getTaskId(), null, ApprovalAction.TRANSFER, req.getComment(), req.getTargetUserId());
        return result;
    }

    @Operation(summary = "委派任务")
    @PostMapping(Route.TASK_DELEGATE)
    @Transactional(rollbackFor = Exception.class)
    public R<Integer> delegate(@Valid @RequestBody com.wkclz.micro.flowable.bean.req.TaskDelegateReq req) {
        log.info("委派任务: taskId={}, targetUserId={}", req.getTaskId(), req.getTargetUserId());
        com.wkclz.flowable.client.bean.req.TaskDelegateReq clientReq = new com.wkclz.flowable.client.bean.req.TaskDelegateReq();
        clientReq.setTaskId(req.getTaskId());
        clientReq.setTargetUserId(req.getTargetUserId());
        clientReq.setComment(req.getComment());
        R<Integer> result = clientWrapper.call(ErrorType.APPROVE_ERROR, "TaskClient#delegate", clientReq,
                () -> clientWrapper.getClient().getTask().delegate(clientReq));
        recordApproval(req.getTaskId(), null, ApprovalAction.DELEGATE, req.getComment(), req.getTargetUserId());
        return result;
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
