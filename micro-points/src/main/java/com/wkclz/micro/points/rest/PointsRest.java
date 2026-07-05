package com.wkclz.micro.points.rest;

import com.wkclz.core.base.PageData;
import com.wkclz.core.base.R;
import com.wkclz.iam.sdk.helper.SessionHelper;
import com.wkclz.micro.points.bean.entity.PointsConsumeRecord;
import com.wkclz.micro.points.bean.entity.PointsEarnRecord;
import com.wkclz.micro.points.bean.entity.PointsWallet;
import com.wkclz.micro.points.bean.req.PointsConsumePageReq;
import com.wkclz.micro.points.bean.req.PointsEarnPageReq;
import com.wkclz.micro.points.bean.req.PointsTrialReq;
import com.wkclz.micro.points.bean.resp.PointsConsumeRecordResp;
import com.wkclz.micro.points.bean.resp.PointsEarnRecordResp;
import com.wkclz.micro.points.bean.resp.PointsTrialResp;
import com.wkclz.micro.points.bean.resp.PointsWalletResp;
import com.wkclz.micro.points.mapper.PointsConsumeRecordMapper;
import com.wkclz.micro.points.mapper.PointsEarnRecordMapper;
import com.wkclz.micro.points.service.PointsTrialService;
import com.wkclz.micro.points.service.PointsWalletService;
import com.wkclz.mybatis.helper.PageQuery;
import com.wkclz.tool.utils.BeanUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 积分 C 端 REST（基于登录 userCode，只读）
 * <p>
 * 提供能力：钱包查询、获取流水分页、消费流水分页。
 * 所有接口基于登录态 userCode（SessionHelper），不涉及任何写操作、幂等检测或用户锁。
 *
 * @see Route
 */
@Slf4j
@Tag(name = "1.积分C端", description = "积分 C 端查询接口（基于登录态，只读）")
@RestController
@RequestMapping(Route.PREFIX)
public class PointsRest {

    @Autowired
    private PointsWalletService walletService;
    @Autowired
    private PointsTrialService trialService;
    @Autowired
    private PointsEarnRecordMapper earnMapper;
    @Autowired
    private PointsConsumeRecordMapper consumeMapper;

    /**
     * 钱包查询
     * <p>
     * 基于登录态 userCode 查询当前用户钱包余额（可用积分、冻结积分、历史总额）。
     */
    @Operation(summary = "1.钱包查询", description = "基于登录态查询当前用户钱包余额")
    @GetMapping(Route.CUSTOM_WALLET)
    public R<PointsWalletResp> wallet() {
        String tenantCode = SessionHelper.getTenantCode();
        String userCode = SessionHelper.getUserCode();
        log.info("C端钱包查询, tenantCode={}, userCode={}", tenantCode, userCode);

        PointsWallet wallet = walletService.getOrCreateWallet(tenantCode, userCode);
        PointsWalletResp resp = new PointsWalletResp();
        resp.setUserCode(wallet.getUserCode());
        resp.setAvailablePoints(wallet.getAvailablePoints() == null ? 0 : wallet.getAvailablePoints());
        resp.setFrozenPoints(wallet.getFrozenPoints() == null ? 0 : wallet.getFrozenPoints());
        resp.setTotalEarnedPoints(wallet.getTotalEarnedPoints() == null ? 0 : wallet.getTotalEarnedPoints());
        return R.ok(resp);
    }

    /**
     * 获取流水分页
     * <p>
     * 基于登录态 userCode 分页查询当前用户积分获取流水（不限使用状态）。
     */
    @Operation(summary = "2.获取流水分页", description = "基于登录态分页查询当前用户积分获取流水")
    @GetMapping(Route.CUSTOM_EARN_PAGE)
    public R<PageData<PointsEarnRecordResp>> earnPage(PointsEarnPageReq req) {
        String tenantCode = SessionHelper.getTenantCode();
        String userCode = SessionHelper.getUserCode();
        log.info("C端获取流水分页, tenantCode={}, userCode={}, current={}, size={}",
                tenantCode, userCode, req.getCurrent(), req.getSize());

        PointsEarnRecord query = BeanUtil.cp(req, PointsEarnRecord.class);
        // 强制以登录态为准，忽略入参中的 userCode/tenantCode
        query.setTenantCode(tenantCode);
        query.setUserCode(userCode);
        PageData<PointsEarnRecord> page = PageQuery.page(query, earnMapper::selectByEntity);
        PageData<PointsEarnRecordResp> newPage = page.convert(PointsEarnRecordResp.class);
        return R.ok(newPage);
    }

    /**
     * 消费流水分页
     * <p>
     * 基于登录态 userCode 分页查询当前用户积分消费流水。
     */
    @Operation(summary = "3.消费流水分页", description = "基于登录态分页查询当前用户积分消费流水")
    @GetMapping(Route.CUSTOM_CONSUME_PAGE)
    public R<PageData<PointsConsumeRecordResp>> consumePage(PointsConsumePageReq req) {
        String tenantCode = SessionHelper.getTenantCode();
        String userCode = SessionHelper.getUserCode();
        log.info("C端消费流水分页, tenantCode={}, userCode={}, current={}, size={}",
                tenantCode, userCode, req.getCurrent(), req.getSize());

        PointsConsumeRecord query = BeanUtil.cp(req, PointsConsumeRecord.class);
        // 强制以登录态为准，忽略入参中的 userCode/tenantCode
        query.setTenantCode(tenantCode);
        query.setUserCode(userCode);
        PageData<PointsConsumeRecord> page = PageQuery.page(query, consumeMapper::selectByEntity);
        PageData<PointsConsumeRecordResp> newPage = page.convert(PointsConsumeRecordResp.class);
        return R.ok(newPage);
    }

    /**
     * 积分试算
     * <p>
     * 基于登录态 userCode 试算积分可抵扣金额。只读操作，不修改任何数据。
     * 入参仅需 paymentAmount，tenantCode/userCode 由登录态自动填充。
     */
    @Operation(summary = "4.积分试算", description = "基于登录态试算积分可抵扣金额（只读）")
    @GetMapping(Route.CUSTOM_TRIAL)
    public R<PointsTrialResp> trial(PointsTrialReq req) {
        String tenantCode = SessionHelper.getTenantCode();
        String userCode = SessionHelper.getUserCode();
        log.info("C端积分试算, tenantCode={}, userCode={}, paymentAmount={}",
                tenantCode, userCode, req.getPaymentAmount());

        // 强制以登录态为准，忽略入参中的 tenantCode/userCode
        req.setTenantCode(tenantCode);
        req.setUserCode(userCode);
        PointsTrialResp resp = trialService.trial(req);
        return R.ok(resp);
    }

}
