package com.wkclz.micro.points.service;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.points.bean.entity.PointsWallet;
import com.wkclz.micro.points.bean.req.PointsTrialReq;
import com.wkclz.micro.points.bean.resp.PointsTrialResp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 积分试算服务单元测试（纯 Mockito，不依赖 Spring 上下文）
 * 验证 1:1 换算（1 积分 = 1 元）与入参两位小数 HALF_UP 归一
 */
@ExtendWith(MockitoExtension.class)
class PointsTrialServiceTest {

    @Mock
    private PointsWalletService walletService;

    @InjectMocks
    private PointsTrialService trialService;

    /**
     * 构造试算入参（tenantCode 固定，避免触发 IdentityContext 静态调用）
     */
    private PointsTrialReq req(String paymentAmount) {
        PointsTrialReq req = new PointsTrialReq();
        req.setTenantCode("T001");
        req.setUserCode("U001");
        req.setPaymentAmount(paymentAmount == null ? null : new BigDecimal(paymentAmount));
        return req;
    }

    /**
     * 构造钱包（设置可用积分）
     */
    private PointsWallet wallet(String availablePoints) {
        PointsWallet wallet = new PointsWallet();
        wallet.setTenantCode("T001");
        wallet.setUserCode("U001");
        wallet.setAvailablePoints(new BigDecimal(availablePoints));
        return wallet;
    }

    @Test
    void shouldDeductFullyWhenAvailableEnough() {
        // 用例 1 全额抵扣：available=10.50 >= paymentAmount=10.00 → deductAmount=10.00, requiredPoints=10.00
        when(walletService.getOrCreateWallet(anyString(), anyString())).thenReturn(wallet("10.50"));

        PointsTrialResp resp = trialService.trial(req("10.00"));

        assertEquals(0, resp.getDeductAmount().compareTo(new BigDecimal("10.00")));
        assertEquals(0, resp.getRequiredPoints().compareTo(new BigDecimal("10.00")));
        assertEquals(0, resp.getAvailablePoints().compareTo(new BigDecimal("10.50")));
    }

    @Test
    void shouldDeductPartiallyWhenAvailableNotEnough() {
        // 用例 2 部分抵扣：available=5.20 < paymentAmount=10.00 → deductAmount=5.20, requiredPoints=5.20
        when(walletService.getOrCreateWallet(anyString(), anyString())).thenReturn(wallet("5.20"));

        PointsTrialResp resp = trialService.trial(req("10.00"));

        assertEquals(0, resp.getDeductAmount().compareTo(new BigDecimal("5.20")));
        assertEquals(0, resp.getRequiredPoints().compareTo(new BigDecimal("5.20")));
    }

    @Test
    void shouldPassThroughTwoDecimalAmount() {
        // 用例 3 两位小数正常透传：available=100.00, paymentAmount=33.33 → deductAmount=33.33, requiredPoints=33.33
        when(walletService.getOrCreateWallet(anyString(), anyString())).thenReturn(wallet("100.00"));

        PointsTrialResp resp = trialService.trial(req("33.33"));

        assertEquals(0, resp.getDeductAmount().compareTo(new BigDecimal("33.33")));
        assertEquals(0, resp.getRequiredPoints().compareTo(new BigDecimal("33.33")));
        assertEquals(2, resp.getDeductAmount().scale());
    }

    @Test
    void shouldNormalizeOverTwoDecimalAmountWithHalfUp() {
        // 用例 4 超两位小数归一：paymentAmount=10.555 → HALF_UP 归一为 10.56 → deductAmount=10.56, requiredPoints=10.56
        when(walletService.getOrCreateWallet(anyString(), anyString())).thenReturn(wallet("20.00"));

        PointsTrialResp resp = trialService.trial(req("10.555"));

        assertEquals(0, resp.getDeductAmount().compareTo(new BigDecimal("10.56")));
        assertEquals(0, resp.getRequiredPoints().compareTo(new BigDecimal("10.56")));
        assertEquals(2, resp.getDeductAmount().scale());
    }

    @Test
    void shouldRejectNullPaymentAmount() {
        // 用例 5 paymentAmount 为空抛校验异常
        ValidationException ex = assertThrows(ValidationException.class, () -> trialService.trial(req(null)));
        assertEquals("paymentAmount 必须大于 0", ex.getMessage());
    }

    @Test
    void shouldRejectNonPositivePaymentAmount() {
        // 用例 5 加固：paymentAmount <= 0 抛校验异常
        ValidationException ex = assertThrows(ValidationException.class, () -> trialService.trial(req("0")));
        assertEquals("paymentAmount 必须大于 0", ex.getMessage());
    }

}
