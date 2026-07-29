package com.wkclz.micro.flowable.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.core.identity.IdentityContext;
import com.wkclz.flowable.client.bean.req.ProcessStartReq;
import com.wkclz.flowable.client.bean.resp.ProcessInstanceResp;
import com.wkclz.micro.flowable.bean.entity.MdmFlowableApply;
import com.wkclz.micro.flowable.bean.entity.MdmFlowableProcessDesign;
import com.wkclz.micro.flowable.bean.enums.ApplyStatus;
import com.wkclz.micro.flowable.bean.enums.ErrorType;
import com.wkclz.micro.flowable.bean.req.ApplyCreateReq;
import com.wkclz.micro.flowable.bean.req.ApplyPageReq;
import com.wkclz.micro.flowable.bean.resp.ApplyCreateResp;
import com.wkclz.micro.flowable.bean.resp.ApplyInfoResp;
import com.wkclz.micro.flowable.bean.resp.ApplyPageResp;
import com.wkclz.micro.flowable.service.FlowableClientWrapper;
import com.wkclz.micro.flowable.service.MdmFlowableApplyService;
import com.wkclz.micro.flowable.service.MdmFlowableProcessDesignService;
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

import java.util.UUID;

@Tag(name = "业务端-流程申请", description = "流程申请管理接口")
@RestController
@RequestMapping(Route.PREFIX)
@Validated
public class ApplyRest {

    private static final Logger log = LoggerFactory.getLogger(ApplyRest.class);

    @Autowired
    private MdmFlowableApplyService applyService;
    @Autowired
    private MdmFlowableProcessDesignService designService;
    @Autowired
    private FlowableClientWrapper clientWrapper;

    @Operation(summary = "发起流程申请")
    @PostMapping(Route.APPLY_CREATE)
    @Transactional(rollbackFor = Exception.class)
    public R<ApplyCreateResp> create(@Valid @RequestBody ApplyCreateReq req) {
        String userCode = IdentityContext.getUserCode();
        log.info("发起流程申请: designCode={}, user={}", req.getDesignCode(), userCode);

        // 查询设计
        MdmFlowableProcessDesign designParam = new MdmFlowableProcessDesign();
        designParam.setDesignCode(req.getDesignCode());
        MdmFlowableProcessDesign design = designService.selectOneByEntity(designParam);
        if (design == null) {
            throw ValidationException.of("流程设计不存在: {}", req.getDesignCode());
        }

        // 创建申请单
        String applyCode = "AP" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        MdmFlowableApply apply = new MdmFlowableApply();
        apply.setApplyCode(applyCode);
        apply.setDesignCode(req.getDesignCode());
        apply.setBusinessType(req.getBusinessType());
        apply.setBusinessSummary(req.getBusinessSummary());
        apply.setBusinessData(req.getBusinessData());
        apply.setStartUserId(userCode);
        apply.setStatus(ApplyStatus.RUNNING.name());
        applyService.insert(apply);

        // 启动流程
        ProcessStartReq startReq = new ProcessStartReq();
        startReq.setDefinitionKey(design.getDesignCode());
        startReq.setBusinessKey(applyCode);
        startReq.setVariables(req.getVariables());

        R<ProcessInstanceResp> result = clientWrapper.call(
                ErrorType.START_ERROR, "ProcessInstanceClient#start", startReq,
                () -> clientWrapper.getClient().getInstance().start(startReq));

        // 回填流程实例 ID
        ProcessInstanceResp instanceResp = result.getData();
        MdmFlowableApply updateApply = new MdmFlowableApply();
        updateApply.setId(apply.getId());
        updateApply.setProcInsId(instanceResp.getProcessInstanceId());
        updateApply.setProcDefId(instanceResp.getDefinitionKey());
        updateApply.setVersion(apply.getVersion());
        applyService.updateByIdSelective(updateApply);

        ApplyCreateResp resp = new ApplyCreateResp();
        resp.setApplyCode(applyCode);
        resp.setProcInsId(instanceResp.getProcessInstanceId());
        return R.ok(resp);
    }

    @Operation(summary = "我的申请列表")
    @GetMapping(Route.APPLY_PAGE)
    public R<PageData<ApplyPageResp>> page(@Valid ApplyPageReq req) {
        MdmFlowableApply entity = BeanUtil.cp(req, MdmFlowableApply.class);
        entity.setStartUserId(IdentityContext.getUserCode());
        PageData<MdmFlowableApply> page = applyService.getApplyPage(entity);
        return R.ok(page.convert(ApplyPageResp.class));
    }

    @Operation(summary = "申请详情")
    @GetMapping(Route.APPLY_INFO)
    public R<ApplyInfoResp> info(@Valid IdReq req) {
        MdmFlowableApply apply = applyService.selectById(req.getId());
        if (apply == null) {
            throw ValidationException.of("申请单不存在");
        }
        return R.ok(BeanUtil.cp(apply, ApplyInfoResp.class));
    }
}
