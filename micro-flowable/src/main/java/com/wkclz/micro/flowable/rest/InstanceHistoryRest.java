package com.wkclz.micro.flowable.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.flowable.client.bean.req.HistoryPageReq;
import com.wkclz.flowable.client.bean.req.ProcessInstancePageReq;
import com.wkclz.flowable.client.bean.resp.*;
import com.wkclz.micro.flowable.bean.entity.MdmFlowableApply;
import com.wkclz.micro.flowable.bean.enums.ErrorType;
import com.wkclz.micro.flowable.service.FlowableClientWrapper;
import com.wkclz.micro.flowable.service.MdmFlowableApplyService;
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

@Tag(name = "业务端-实例历史", description = "流程实例/历史查询（透传）")
@RestController
@RequestMapping(Route.PREFIX)
@Validated
public class InstanceHistoryRest {

    private static final Logger log = LoggerFactory.getLogger(InstanceHistoryRest.class);

    @Autowired
    private FlowableClientWrapper clientWrapper;
    @Autowired
    private MdmFlowableApplyService applyService;

    @Operation(summary = "流程实例分页")
    @GetMapping(Route.INSTANCE_PAGE)
    public R<PageData<ProcessInstancePageResp>> instancePage(@Valid ProcessInstancePageReq req) {
        return clientWrapper.call(ErrorType.QUERY_ERROR, "ProcessInstanceClient#page", req,
                () -> clientWrapper.getClient().getInstance().page(req));
    }

    @Operation(summary = "流程实例详情")
    @GetMapping(Route.INSTANCE_INFO)
    public R<ProcessInstanceResp> instanceInfo(@Valid IdReq req) {
        return clientWrapper.call(ErrorType.QUERY_ERROR, "ProcessInstanceClient#info", req,
                () -> clientWrapper.getClient().getInstance().info(req));
    }

    @Operation(summary = "历史流程实例分页")
    @GetMapping(Route.HISTORY_INSTANCE_PAGE)
    public R<PageData<HistoryInstancePageResp>> historyInstancePage(@Valid HistoryPageReq req) {
        return clientWrapper.call(ErrorType.QUERY_ERROR, "HistoryClient#instancePage", req,
                () -> clientWrapper.getClient().getHistory().instancePage(req));
    }

    @Operation(summary = "历史任务分页")
    @GetMapping(Route.HISTORY_TASK_PAGE)
    public R<PageData<HistoryTaskPageResp>> historyTaskPage(@Valid HistoryPageReq req) {
        return clientWrapper.call(ErrorType.QUERY_ERROR, "HistoryClient#taskPage", req,
                () -> clientWrapper.getClient().getHistory().taskPage(req));
    }

    @Operation(summary = "历史活动列表")
    @GetMapping(Route.HISTORY_ACTIVITY_LIST)
    public R<List<HistoryActivityResp>> historyActivityList(@Valid IdReq req) {
        return clientWrapper.call(ErrorType.QUERY_ERROR, "HistoryClient#activityList", req,
                () -> clientWrapper.getClient().getHistory().activityList(req));
    }

    @Operation(summary = "撤回流程")
    @PostMapping(Route.INSTANCE_WITHDRAW)
    @Transactional(rollbackFor = Exception.class)
    public R<Integer> withdraw(@Valid @RequestBody com.wkclz.micro.flowable.bean.req.InstanceWithdrawReq req) {
        log.info("撤回流程: procInsId={}", req.getProcInsId());
        com.wkclz.flowable.client.bean.req.InstanceWithdrawReq clientReq = new com.wkclz.flowable.client.bean.req.InstanceWithdrawReq();
        clientReq.setProcessInstanceId(req.getProcInsId());
        clientReq.setReason(req.getComment());
        R<Integer> result = clientWrapper.call(ErrorType.APPROVE_ERROR, "ProcessInstanceClient#withdraw", clientReq,
                () -> clientWrapper.getClient().getInstance().withdraw(clientReq));
        // 更新申请单状态
        MdmFlowableApply applyParam = new MdmFlowableApply();
        applyParam.setProcInsId(req.getProcInsId());
        MdmFlowableApply apply = applyService.selectOneByEntity(applyParam);
        if (apply != null) {
            MdmFlowableApply updateApply = new MdmFlowableApply();
            updateApply.setId(apply.getId());
            updateApply.setStatus("WITHDRAWN");
            updateApply.setVersion(apply.getVersion());
            applyService.updateByIdSelective(updateApply);
        }
        return result;
    }
}
