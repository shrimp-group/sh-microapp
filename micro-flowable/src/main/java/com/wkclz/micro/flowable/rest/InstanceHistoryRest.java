package com.wkclz.micro.flowable.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.core.identity.IdentityContext;
import com.wkclz.flowable.client.bean.req.HistoryPageReq;
import com.wkclz.flowable.client.bean.req.InstanceIdReq;
import com.wkclz.flowable.client.bean.req.ProcessInstancePageReq;
import com.wkclz.flowable.client.bean.resp.*;
import com.wkclz.flowable.client.config.FlowableClient;
import com.wkclz.micro.flowable.bean.entity.FlowableApply;
import com.wkclz.micro.flowable.bean.entity.FlowableApproval;
import com.wkclz.micro.flowable.bean.enums.ApplyStatus;
import com.wkclz.micro.flowable.bean.enums.ApprovalAction;
import com.wkclz.micro.flowable.service.FlowableApplyService;
import com.wkclz.micro.flowable.service.FlowableApprovalService;
import com.wkclz.web.bean.IdReq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Tag(name = "6.业务端-实例历史", description = "流程实例/历史查询（透传）")
@RestController
@RequestMapping(Route.PREFIX)
@Validated
public class InstanceHistoryRest {


    @Resource
    private FlowableClient flowableClient;
    @Resource
    private FlowableApplyService applyService;
    @Resource
    private FlowableApprovalService approvalService;

    @Operation(summary = "1.流程实例-分页查询")
    @GetMapping(Route.INSTANCE_PAGE)
    public R<PageData<ProcessInstancePageResp>> instancePage(@Valid ProcessInstancePageReq req) {
        R<PageData<ProcessInstancePageResp>> result = flowableClient.getInstance().page(req);
        fillApplyInfo(result.getData(), ProcessInstancePageResp::getBusinessKey,
                (r, a) -> { r.setBusinessType(a.getBusinessType()); r.setBusinessSummary(a.getBusinessSummary()); });
        return result;
    }

    @Operation(summary = "2.流程实例-详情")
    @GetMapping(Route.INSTANCE_INFO)
    public R<ProcessInstanceResp> instanceInfo(@Valid IdReq req) {
        return flowableClient.getInstance().info(req);
    }

    @Operation(summary = "3.历史实例-分页查询")
    @GetMapping(Route.HISTORY_INSTANCE_PAGE)
    public R<PageData<HistoryInstancePageResp>> historyInstancePage(@Valid HistoryPageReq req) {
        R<PageData<HistoryInstancePageResp>> result = flowableClient.getHistory().instancePage(req);
        fillApplyInfo(result.getData(), HistoryInstancePageResp::getBusinessKey,
                (r, a) -> { r.setBusinessType(a.getBusinessType()); r.setBusinessSummary(a.getBusinessSummary()); });
        return result;
    }

    @Operation(summary = "4.历史任务-分页查询")
    @GetMapping(Route.HISTORY_TASK_PAGE)
    public R<PageData<HistoryTaskPageResp>> historyTaskPage(@Valid HistoryPageReq req) {
        return flowableClient.getHistory().taskPage(req);
    }

    @Operation(summary = "5.历史活动-列表")
    @GetMapping(Route.HISTORY_ACTIVITY_LIST)
    public R<List<HistoryActivityResp>> historyActivityList(@Valid InstanceIdReq req) {
        return flowableClient.getHistory().activityList(req);
    }

    /**
     * 按申请单号（businessKey）一次批量反查申请单，回填业务类型与摘要，查不到的行跳过
     */
    private <T> void fillApplyInfo(PageData<T> page, java.util.function.Function<T, String> businessKeyGetter,
                                   java.util.function.BiConsumer<T, FlowableApply> applySetter) {
        if (page == null || page.getRecords() == null) {
            return;
        }
        log.info("实例业务信息回填: rows={}", page.getRecords().size());
        Set<String> applyCodes = new HashSet<>();
        for (T row : page.getRecords()) {
            String applyCode = businessKeyGetter.apply(row);
            if (applyCode != null && !applyCode.isEmpty()) {
                applyCodes.add(applyCode);
            }
        }
        Map<String, FlowableApply> applyMap = applyService.selectByApplyCodes(applyCodes).stream()
                .collect(Collectors.toMap(FlowableApply::getApplyCode, a -> a, (a, b) -> a));
        for (T row : page.getRecords()) {
            FlowableApply apply = applyMap.get(businessKeyGetter.apply(row));
            if (apply != null) {
                applySetter.accept(row, apply);
            }
        }
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
        FlowableApply apply = applyService.updateStatusByProcInsId(req.getProcInsId(), ApplyStatus.WITHDRAWN.name());
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
