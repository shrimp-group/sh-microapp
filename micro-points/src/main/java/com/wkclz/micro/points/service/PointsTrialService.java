package com.wkclz.micro.points.service;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.core.identity.IdentityContext;
import com.wkclz.micro.points.bean.entity.PointsWallet;
import com.wkclz.micro.points.bean.req.PointsTrialReq;
import com.wkclz.micro.points.bean.resp.PointsTrialResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 积分试算服务
 * <p>
 * 按 1:1 换算（1 积分 = 1 元），试算可抵扣金额。
 * <p>
 * 试算为<b>只读</b>操作：不修改任何数据，不获取用户锁，不开启事务。
 */
@Slf4j
@Service
public class PointsTrialService {

    @Autowired
    private PointsWalletService walletService;

    /**
     * 试算积分可抵扣金额。
     * <p>
     * 规则（1:1 换算）：
     * <ul>
     *   <li>available >= paymentAmount：全额抵扣，deductAmount=paymentAmount, requiredPoints=paymentAmount</li>
     *   <li>available &lt; paymentAmount：部分抵扣，deductAmount=available, requiredPoints=available</li>
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
            tenantCode = IdentityContext.getTenantCode();
        }

        // 获取钱包（只读查询，不修改）
        PointsWallet wallet = walletService.getOrCreateWallet(tenantCode, req.getUserCode());
        BigDecimal available = wallet.getAvailablePoints() == null ? BigDecimal.ZERO : wallet.getAvailablePoints();

        // 按 1:1 换算：可用积分即可抵扣现金
        BigDecimal deductAmount;
        BigDecimal requiredPoints;
        if (available.compareTo(req.getPaymentAmount()) >= 0) {
            // 可全额抵扣
            deductAmount = req.getPaymentAmount();
            requiredPoints = req.getPaymentAmount();
            log.info("试算结果：全额抵扣, userCode={}, available={}, deductAmount={}, requiredPoints={}",
                    req.getUserCode(), available, deductAmount, requiredPoints);
        } else {
            // 部分抵扣
            deductAmount = available;
            requiredPoints = available;
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
