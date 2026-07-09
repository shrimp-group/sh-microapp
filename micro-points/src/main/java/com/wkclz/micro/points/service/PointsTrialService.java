package com.wkclz.micro.points.service;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.iam.contract.context.PrincipalContext;
import com.wkclz.micro.points.PointsConstants;
import com.wkclz.micro.points.bean.entity.PointsWallet;
import com.wkclz.micro.points.bean.req.PointsTrialReq;
import com.wkclz.micro.points.bean.resp.PointsTrialResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 积分试算服务
 * <p>
 * 按 100:1 换算（100 积分 = 1 元），试算可抵扣金额。
 * <p>
 * 试算为<b>只读</b>操作：不修改任何数据，不获取用户锁，不开启事务。
 *
 * @see PointsConstants#POINTS_TO_CASH_RATE
 */
@Slf4j
@Service
public class PointsTrialService {

    /** 积分与现金换算比例（100 积分 = 1 元） */
    private static final BigDecimal RATE = BigDecimal.valueOf(PointsConstants.POINTS_TO_CASH_RATE);

    @Autowired
    private PointsWalletService walletService;

    /**
     * 试算积分可抵扣金额。
     * <p>
     * 规则（spec §5.2）：
     * <ul>
     *   <li>available/100 >= paymentAmount：全额抵扣，deductAmount=paymentAmount, requiredPoints=paymentAmount*100</li>
     *   <li>available/100 < paymentAmount：部分抵扣，deductAmount=floor(available/100), requiredPoints=available-available%100</li>
     * </ul>
     *
     * @param req 试算入参（userCode/paymentAmount）
     * @return 试算结果（availablePoints/deductAmount/requiredPoints）
     */
    public PointsTrialResp trial(PointsTrialReq req) {
        log.info("积分试算开始, userCode={}, paymentAmount={}", req.getUserCode(), req.getPaymentAmount());

        // 参数校验
        if (req.getUserCode() == null || req.getUserCode().isBlank()) {
            throw ValidationException.of("userCode 不能为空");
        }
        if (req.getPaymentAmount() == null || req.getPaymentAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw ValidationException.of("paymentAmount 必须大于 0");
        }

        // 解析租户编码：入参优先，为空时从登录态获取
        String tenantCode = req.getTenantCode();
        if (tenantCode == null || tenantCode.isBlank()) {
            tenantCode = PrincipalContext.getTenantCode();
        }

        // 获取钱包（只读查询，不修改）
        PointsWallet wallet = walletService.getOrCreateWallet(tenantCode, req.getUserCode());
        Integer available = wallet.getAvailablePoints() == null ? 0 : wallet.getAvailablePoints();

        // 按 100:1 换算，floor(available / 100) 为可全额使用的现金
        BigDecimal availablePointsBd = BigDecimal.valueOf(available);
        BigDecimal availableCash = availablePointsBd.divide(RATE, 0, RoundingMode.FLOOR);

        BigDecimal deductAmount;
        Integer requiredPoints;
        if (availableCash.compareTo(req.getPaymentAmount()) >= 0) {
            // 可全额抵扣
            deductAmount = req.getPaymentAmount();
            requiredPoints = req.getPaymentAmount().multiply(RATE).intValue();
            log.info("试算结果：全额抵扣, userCode={}, available={}, deductAmount={}, requiredPoints={}",
                    req.getUserCode(), available, deductAmount, requiredPoints);
        } else {
            // 部分抵扣
            deductAmount = availableCash;
            requiredPoints = available - (available % PointsConstants.POINTS_TO_CASH_RATE);
            log.info("试算结果：部分抵扣, userCode={}, available={}, deductAmount={}, requiredPoints={}",
                    req.getUserCode(), available, deductAmount, requiredPoints);
        }

        PointsTrialResp resp = new PointsTrialResp();
        resp.setAvailablePoints(available);
        resp.setDeductAmount(deductAmount);
        resp.setRequiredPoints(requiredPoints);
        return resp;
    }

}
