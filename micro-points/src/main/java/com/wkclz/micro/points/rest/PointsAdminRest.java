package com.wkclz.micro.points.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.core.identity.IdentityContext;
import com.wkclz.micro.points.bean.entity.PointsConsumeRecord;
import com.wkclz.micro.points.bean.entity.PointsDeductionRecord;
import com.wkclz.micro.points.bean.entity.PointsEarnRecord;
import com.wkclz.micro.points.bean.entity.PointsWallet;
import com.wkclz.micro.points.bean.enums.PointsSourceType;
import com.wkclz.micro.points.bean.req.*;
import com.wkclz.micro.points.bean.resp.*;
import com.wkclz.micro.points.mapper.PointsConsumeRecordMapper;
import com.wkclz.micro.points.mapper.PointsDeductionRecordMapper;
import com.wkclz.micro.points.mapper.PointsEarnRecordMapper;
import com.wkclz.micro.points.service.PointsIssueService;
import com.wkclz.micro.points.service.PointsReconcileService;
import com.wkclz.micro.points.service.PointsWalletService;
import com.wkclz.mybatis.helper.PageQuery;
import com.wkclz.tool.utils.BeanUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 积分运营端 REST（按 userCode 查询/操作）
 * <p>
 * 提供能力：管理员手动发放积分、按 userCode 查询钱包/获取流水/消费流水、消费扣减明细（对账）、对账查询。
 * 查询类接口以入参 userCode 为准，租户编码取登录态（管理员所在租户）。
 * 管理员手动发放复用 {@link PointsIssueService}，强制 pointSourceType=ADMIN_ISSUE。
 * <p>
 * 注意：Route 常量（ADMIN_*）已含 "/admin" 前缀，本类使用 {@code @RequestMapping(Route.PREFIX)}
 * 即可拼出 /micro-points/admin/xxx 路径，避免重复拼接 "/admin"。
 *
 * @see Route
 * @see PointsIssueService
 * @see PointsReconcileService
 */
@Slf4j
@Tag(name = "2.积分运营端", description = "积分运营端管理接口（管理员手动发放/查询/对账）")
@RestController
@RequestMapping(Route.PREFIX)
public class PointsAdminRest {

    @Autowired
    private PointsIssueService issueService;
    @Autowired
    private PointsWalletService walletService;
    @Autowired
    private PointsEarnRecordMapper earnMapper;
    @Autowired
    private PointsConsumeRecordMapper consumeMapper;
    @Autowired
    private PointsDeductionRecordMapper deductionMapper;
    @Autowired
    private PointsReconcileService reconcileService;

    /**
     * 管理员手动发放积分
     * <p>
     * 复用发放服务，强制 pointSourceType=ADMIN_ISSUE，幂等键 ADMIN_ISSUE:sourceNo。
     * 租户编码取管理员登录态，createBy 由框架自动填充管理员账号。
     */
    @Operation(summary = "1.管理员手动发放积分", description = "管理员手动给用户发放积分（pointSourceType=ADMIN_ISSUE）")
    @PostMapping(Route.ADMIN_ISSUE)
    public R<PointsIssueResp> adminIssue(@Valid @RequestBody PointsIssueReq req) {
        // 强制来源类型为 ADMIN_ISSUE，由 REST 层控制，忽略入参传入值
        req.setPointSourceType(PointsSourceType.ADMIN_ISSUE.name());
        // 租户编码以管理员登录态为准
        req.setTenantCode(IdentityContext.getTenantCode());
        log.info("管理员手动发放积分, tenantCode={}, userCode={}, points={}, sourceNo={}",
                req.getTenantCode(), req.getUserCode(), req.getPoints(), req.getSourceNo());

        PointsIssueResp resp = issueService.issuePoints(req);
        return R.ok(resp);
    }

    /**
     * 按 userCode 查询用户钱包
     */
    @Operation(summary = "2.用户钱包查询", description = "按 userCode 查询用户钱包")
    @GetMapping(Route.ADMIN_WALLET)
    public R<PointsWalletResp> adminWallet(@Valid PointsWalletQueryReq req) {
        String tenantCode = IdentityContext.getTenantCode();
        log.info("运营端钱包查询, tenantCode={}, userCode={}", tenantCode, req.getUserCode());

        PointsWallet wallet = walletService.getOrCreateWallet(tenantCode, req.getUserCode());
        PointsWalletResp resp = new PointsWalletResp();
        resp.setUserCode(wallet.getUserCode());
        resp.setAvailablePoints(wallet.getAvailablePoints() == null ? 0 : wallet.getAvailablePoints());
        resp.setFrozenPoints(wallet.getFrozenPoints() == null ? 0 : wallet.getFrozenPoints());
        resp.setTotalEarnedPoints(wallet.getTotalEarnedPoints() == null ? 0 : wallet.getTotalEarnedPoints());
        return R.ok(resp);
    }

    /**
     * 按 userCode 查询获取流水分页
     */
    @Operation(summary = "3.获取流水分页", description = "按 userCode 分页查询获取流水")
    @GetMapping(Route.ADMIN_EARN_PAGE)
    public R<PageData<PointsEarnRecordResp>> adminEarnPage(PointsEarnPageReq req) {
        String tenantCode = IdentityContext.getTenantCode();
        log.info("运营端获取流水分页, tenantCode={}, userCode={}, current={}, size={}",
                tenantCode, req.getUserCode(), req.getCurrent(), req.getSize());

        PointsEarnRecord query = BeanUtil.cp(req, PointsEarnRecord.class);
        // 租户编码以管理员登录态为准，userCode 取入参
        query.setTenantCode(tenantCode);
        query.setUserCode(req.getUserCode());
        PageData<PointsEarnRecord> page = PageQuery.page(query, earnMapper::selectByEntity);
        PageData<PointsEarnRecordResp> newPage = page.convert(PointsEarnRecordResp.class);
        return R.ok(newPage);
    }

    /**
     * 按 userCode 查询消费流水分页
     */
    @Operation(summary = "4.消费流水分页", description = "按 userCode 分页查询消费流水")
    @GetMapping(Route.ADMIN_CONSUME_PAGE)
    public R<PageData<PointsConsumeRecordResp>> adminConsumePage(PointsConsumePageReq req) {
        String tenantCode = IdentityContext.getTenantCode();
        log.info("运营端消费流水分页, tenantCode={}, userCode={}, current={}, size={}",
                tenantCode, req.getUserCode(), req.getCurrent(), req.getSize());

        PointsConsumeRecord query = BeanUtil.cp(req, PointsConsumeRecord.class);
        query.setTenantCode(tenantCode);
        query.setUserCode(req.getUserCode());
        PageData<PointsConsumeRecord> page = PageQuery.page(query, consumeMapper::selectByEntity);
        PageData<PointsConsumeRecordResp> newPage = page.convert(PointsConsumeRecordResp.class);
        return R.ok(newPage);
    }

    /**
     * 消费扣减明细分页（对账）
     * <p>
     * 查询用户消费流水（分页），对每条关联查询 COMPLETED 扣减动作记录
     * （earn_flow_no 非空），用于人工对账页面展示。
     */
    @Operation(summary = "5.消费扣减明细", description = "按 userCode 查询消费流水及其关联扣减动作记录（对账）")
    @GetMapping(Route.ADMIN_CONSUME_DEDUCTION_PAGE)
    public R<List<PointsConsumeDeductionResp>> adminConsumeDeductionPage(PointsConsumePageReq req) {
        String tenantCode = IdentityContext.getTenantCode();
        log.info("运营端消费扣减明细查询, tenantCode={}, userCode={}, current={}, size={}",
                tenantCode, req.getUserCode(), req.getCurrent(), req.getSize());

        // 1. 分页查询消费流水
        PointsConsumeRecord query = BeanUtil.cp(req, PointsConsumeRecord.class);
        query.setTenantCode(tenantCode);
        query.setUserCode(req.getUserCode());
        PageData<PointsConsumeRecord> page = PageQuery.page(query, consumeMapper::selectByEntity);
        List<PointsConsumeRecord> consumeRecords = page.getRecords();
        if (consumeRecords == null || consumeRecords.isEmpty()) {
            log.info("运营端消费扣减明细查询无消费流水, tenantCode={}, userCode={}", tenantCode, req.getUserCode());
            return R.ok(Collections.emptyList());
        }

        // 2. 对每条消费流水关联查询扣减动作记录
        List<PointsConsumeDeductionResp> result = new ArrayList<>(consumeRecords.size());
        for (PointsConsumeRecord consume : consumeRecords) {
            List<PointsDeductionRecord> deductions = deductionMapper.selectCompletedActionsByOrderNo(
                    tenantCode, consume.getOrderNo());
            if (deductions == null) {
                deductions = Collections.emptyList();
            }
            // 已扣减总额 = COMPLETED 动作记录 deduction_points 之和
            Integer deductedSum = 0;
            for (PointsDeductionRecord d : deductions) {
                if (d.getDeductionPoints() != null) {
                    deductedSum += d.getDeductionPoints();
                }
            }

            PointsConsumeDeductionResp resp = new PointsConsumeDeductionResp();
            resp.setConsumeFlowNo(consume.getFlowNo());
            resp.setConsumeTime(consume.getConsumeTime());
            resp.setPoints(consume.getPoints() == null ? 0 : consume.getPoints());
            resp.setOrderNo(consume.getOrderNo());
            resp.setStatus(consume.getStatus());
            resp.setDeductions(deductions);
            resp.setDeductedSum(deductedSum);
            result.add(resp);
        }
        log.info("运营端消费扣减明细查询完成, tenantCode={}, userCode={}, 消费流水数={}",
                tenantCode, req.getUserCode(), result.size());
        return R.ok(result);
    }

    /**
     * 对账查询
     * <p>
     * 核对用户消费流水与扣减记录一致性，返回每条消费流水的对账结果。
     */
    @Operation(summary = "6.对账查询", description = "核对消费流水与扣减记录一致性")
    @GetMapping(Route.ADMIN_RECONCILE)
    public R<List<PointsReconcileResp>> adminReconcile(PointsReconcileReq req) {
        // 租户编码以管理员登录态为准
        req.setTenantCode(IdentityContext.getTenantCode());
        log.info("运营端对账查询, tenantCode={}, userCode={}, startTime={}, endTime={}",
                req.getTenantCode(), req.getUserCode(), req.getStartTime(), req.getEndTime());

        List<PointsReconcileResp> result = reconcileService.reconcile(req);
        return R.ok(result);
    }

}
