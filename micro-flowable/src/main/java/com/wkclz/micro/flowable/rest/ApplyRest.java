package com.wkclz.micro.flowable.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.core.identity.IdentityContext;
import com.wkclz.flowable.client.bean.req.ProcessStartReq;
import com.wkclz.flowable.client.bean.resp.ProcessInstanceResp;
import com.wkclz.flowable.client.config.FlowableClient;
import com.wkclz.micro.flowable.bean.entity.FlowableApply;
import com.wkclz.micro.flowable.bean.entity.FlowableProcessDesign;
import com.wkclz.micro.flowable.bean.enums.ApplyStatus;
import com.wkclz.micro.flowable.bean.req.ApplyCreateReq;
import com.wkclz.micro.flowable.bean.req.ApplyPageReq;
import com.wkclz.micro.flowable.bean.resp.ApplyCreateResp;
import com.wkclz.micro.flowable.bean.resp.ApplyInfoResp;
import com.wkclz.micro.flowable.bean.resp.ApplyPageResp;
import com.wkclz.micro.flowable.service.FlowableApplyService;
import com.wkclz.micro.flowable.service.FlowableProcessDesignService;
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

@Tag(name = "4.业务端-流程申请", description = "流程申请管理接口")
@RestController
@RequestMapping(Route.PREFIX)
@Validated
public class ApplyRest {

    private static final Logger log = LoggerFactory.getLogger(ApplyRest.class);

    @Autowired
    private FlowableApplyService applyService;
    @Autowired
    private FlowableProcessDesignService designService;
    @Autowired
    private FlowableClient flowableClient;

    @Operation(summary = "1.流程申请-发起")
    @PostMapping(Route.APPLY_CREATE)
    @Transactional(rollbackFor = Exception.class)
    public R<ApplyCreateResp> create(@Valid @RequestBody ApplyCreateReq req) {
        String userCode = IdentityContext.getUserCode();
        log.info("发起流程申请: designCode={}, user={}", req.getDesignCode(), userCode);

        // 查询设计
        FlowableProcessDesign designParam = new FlowableProcessDesign();
        designParam.setDesignCode(req.getDesignCode());
        FlowableProcessDesign design = designService.selectOneByEntity(designParam);
        if (design == null) {
            throw ValidationException.of("流程设计不存在: {}", req.getDesignCode());
        }

        // 创建申请单
        String applyCode = "AP" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        FlowableApply apply = new FlowableApply();
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

        R<ProcessInstanceResp> result = flowableClient.getInstance().start(startReq);

        // 回填流程实例 ID（insert 不回填 version，乐观锁需先查询再更新）
        FlowableApply dbApply = applyService.selectById(apply.getId());
        ProcessInstanceResp instanceResp = result.getData();
        dbApply.setProcInsId(instanceResp.getProcessInstanceId());
        dbApply.setProcDefId(instanceResp.getDefinitionKey());
        applyService.updateByIdSelective(dbApply);

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
        PageData<FlowableApply> page = applyService.getApplyPage(entity);
        return R.ok(page.convert(ApplyPageResp.class));
    }

    @Operation(summary = "3.流程申请-详情")
    @GetMapping(Route.APPLY_INFO)
    public R<ApplyInfoResp> info(@Valid IdReq req) {
        FlowableApply apply = applyService.selectById(req.getId());
        if (apply == null) {
            throw ValidationException.of("申请单不存在");
        }
        return R.ok(BeanUtil.cp(apply, ApplyInfoResp.class));
    }
}
