package com.wkclz.micro.flowable.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.core.identity.IdentityContext;
import com.wkclz.flowable.client.bean.req.HistoryPageReq;
import com.wkclz.flowable.client.bean.req.ProcessInstancePageReq;
import com.wkclz.flowable.client.bean.resp.*;
import com.wkclz.flowable.client.config.FlowableClient;
import com.wkclz.micro.flowable.bean.entity.FlowableApply;
import com.wkclz.micro.flowable.bean.entity.FlowableApproval;
import com.wkclz.micro.flowable.bean.enums.ApprovalAction;
import com.wkclz.micro.flowable.service.FlowableApplyService;
import com.wkclz.micro.flowable.service.FlowableApprovalService;
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

@Tag(name = "6.业务端-实例历史", description = "流程实例/历史查询（透传）")
@RestController
@RequestMapping(Route.PREFIX)
@Validated
public class InstanceHistoryRest {

    private static final Logger log = LoggerFactory.getLogger(InstanceHistoryRest.class);

    @Autowired
    private FlowableClient flowableClient;
    @Autowired
    private FlowableApplyService applyService;
    @Autowired
    private FlowableApprovalService approvalService;

    @Operation(summary = "1.流程实例-分页查询")
    @GetMapping(Route.INSTANCE_PAGE)
    public R<PageData<ProcessInstancePageResp>> instancePage(@Valid ProcessInstancePageReq req) {
        return flowableClient.getInstance().page(req);
    }

    @Operation(summary = "2.流程实例-详情")
    @GetMapping(Route.INSTANCE_INFO)
    public R<ProcessInstanceResp> instanceInfo(@Valid IdReq req) {
        return flowableClient.getInstance().info(req);
    }

    @Operation(summary = "3.历史实例-分页查询")
    @GetMapping(Route.HISTORY_INSTANCE_PAGE)
    public R<PageData<HistoryInstancePageResp>> historyInstancePage(@Valid HistoryPageReq req) {
        return flowableClient.getHistory().instancePage(req);
    }

    @Operation(summary = "4.历史任务-分页查询")
    @GetMapping(Route.HISTORY_TASK_PAGE)
    public R<PageData<HistoryTaskPageResp>> historyTaskPage(@Valid HistoryPageReq req) {
        return flowableClient.getHistory().taskPage(req);
    }

    @Operation(summary = "5.历史活动-列表")
    @GetMapping(Route.HISTORY_ACTIVITY_LIST)
    public R<List<HistoryActivityResp>> historyActivityList(@Valid IdReq req) {
        return flowableClient.getHistory().activityList(req);
    }

    @Operation(summary = "6.流程实例-撤回")
    @PostMapping(Route.INSTANCE_WITHDRAW)
    @Transactional(rollbackFor = Exception.class)
    public R<Integer> withdraw(@Valid @RequestBody com.wkclz.micro.flowable.bean.req.InstanceWithdrawReq req) {
        log.info("撤回流程: procInsId={}", req.getProcInsId());
        com.wkclz.flowable.client.bean.req.InstanceWithdrawReq clientReq = new com.wkclz.flowable.client.bean.req.InstanceWithdrawReq();
        clientReq.setProcessInstanceId(req.getProcInsId());
        clientReq.setReason(req.getComment());
        R<Integer> result = flowableClient.getInstance().withdraw(clientReq);
        // 更新申请单状态
        FlowableApply applyParam = new FlowableApply();
        applyParam.setProcInsId(req.getProcInsId());
        FlowableApply apply = applyService.selectOneByEntity(applyParam);
        if (apply != null) {
            FlowableApply updateApply = new FlowableApply();
            updateApply.setId(apply.getId());
            updateApply.setStatus("WITHDRAWN");
            updateApply.setVersion(apply.getVersion());
            applyService.updateByIdSelective(updateApply);
        }
        // 补写撤回审批意见
        try {
            FlowableApproval approval = new FlowableApproval();
            approval.setProcInsId(req.getProcInsId());
            approval.setApproverId(IdentityContext.getUserCode());
            approval.setAction(ApprovalAction.WITHDRAW.name());
            approval.setComment(req.getComment());
            if (apply != null) {
                approval.setApplyId(apply.getId());
            }
            approvalService.insert(approval);
        } catch (Exception e) {
            log.error("记录撤回意见失败: procInsId={}", req.getProcInsId(), e);
        }
        return result;
    }
}
