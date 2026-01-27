package com.wkclz.micro.pay.service;

import com.wkclz.core.base.R;
import com.wkclz.core.enums.EnvType;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.iam.sdk.helper.SessionHelper;
import com.wkclz.micro.pay.helper.AlipayHelper;
import com.wkclz.micro.pay.helper.WxpayHelper;
import com.wkclz.micro.pay.pojo.dto.PayOrderDto;
import com.wkclz.micro.pay.pojo.entity.PayOrder;
import com.wkclz.micro.pay.pojo.enums.PayMethod;
import com.wkclz.micro.pay.pojo.enums.PayStatus;
import com.wkclz.micro.pay.pojo.enums.TerminalType;
import com.wkclz.spring.config.Sys;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.List;

/**
 * Description Create by shrimp-gen
 *
 * @author wangkaicun
 * @table pay_order (支付-订单) 单表服务类，代码重新生成不覆盖. 只建议完成单表的逻辑，或主表为 pay_order 的逻辑. 其他逻辑放 custom 中
 */

@Slf4j
@Service
public class OrderService {

    @Resource
    private WxpayHelper wxpayHelper;
    @Resource
    private AlipayHelper alipayHelper;
    @Resource
    private PayOrderService payOrderService;

    @Transactional(rollbackFor = Exception.class)
    public PayOrderDto createPayOrder(PayOrder model, HttpServletRequest req, HttpServletResponse rep) {
        // 基础参数校验
        paramCheck(model);
        PayMethod payMethod = PayMethod.valueOf(model.getPayMethod());

        PayOrder lastOrder = new PayOrder();
        lastOrder.setOrderNo(model.getOrderNo());
        List<PayOrder> oldPayOrders = payOrderService.getActivePayOrder(lastOrder);
        if (CollectionUtils.isNotEmpty(oldPayOrders)) {
            if (oldPayOrders.size() > 1) {
                throw ValidationException.of("您的订单历史支付信息异常，请联系客服!");
            }
            lastOrder = oldPayOrders.get(0);

            // 若非新建状态，报错
            if (!PayStatus.PAYING.name().equals(lastOrder.getPayStatus())
                && !PayStatus.PAYERROR.name().equals(lastOrder.getPayStatus())) {
                throw ValidationException.of("您的订单历史支付状态异常，请联系客服!");
            }

            // 更换支付方式？取消！
            if (!model.getPayMethod().equals(lastOrder.getPayMethod())) {
                lastOrder.setPayStatus(PayStatus.CANCEL.name());
                payOrderService.update(lastOrder);

                // 向微信取消支付订单
                if (PayMethod.WX_PAY.name().equals(lastOrder.getPayMethod())) {
                    R result = wxpayHelper.payClose(lastOrder);
                    if (result.getCode() == 200) {
                        lastOrder.setPayMethod(null);
                        payOrderService.updateById(lastOrder);
                        lastOrder.setVersion(lastOrder.getVersion() + 1);
                    } else {
                        log.error("WX_PAY order cancel faild: {}", result.getMsg());
                    }
                }

                // 支付宝
                if (PayMethod.ALI_PAY.name().equals(lastOrder.getPayMethod())) {
                    R result = alipayHelper.payClose(lastOrder);
                    if (result.getCode() == 200) {
                        lastOrder.setPayMethod(null);
                        payOrderService.updateById(lastOrder);
                        lastOrder.setVersion(lastOrder.getVersion() + 1);
                    } else {
                        log.error("ALI_PAY order cancel faild: {}", result.getMsg());
                    }
                }

                // TODO 其他支付方式取消
            } else {
                // 不更换支付方式，一切保留
                model = PayOrder.copy(lastOrder, model);
            }

            // 更换 out_trade_no
            String outTradeNo = lastOrder.getOutTradeNo();
            if (outTradeNo.contains("-")) {
                String seqStr = outTradeNo.split("-")[1];
                int seq = Integer.parseInt(seqStr);
                outTradeNo = model.getOrderNo() + "-" + (seq + 1);
                model.setOutTradeNo(model.getOrderNo() + "-" + (seq + 1));
            } else {
                outTradeNo = model.getOrderNo() + "-1";
            }
            model.setOutTradeNo(outTradeNo);
        }
        model.setPayStatus(PayStatus.PAYING.name());

        // 新支付，或更换了支付方式
        if (model.getId() == null) {
            payOrderService.insert(model);
        } else {
            payOrderService.update(model);
        }

        // 支付宝
        if (PayMethod.ALI_PAY == payMethod) {
            return alipayHelper.pay(model, req, rep);
        }
        // 微信
        if (PayMethod.WX_PAY == payMethod) {
            return wxpayHelper.pay(model, req, rep);
        }
        if (PayMethod.MOCK_PAY == payMethod) {
            // TODO 模拟支付，修改数据
            return PayOrderDto.copy(model);
        }
        // 银联
        if (PayMethod.UNION_PAY == payMethod) {
            // 有需求再继续
        }
        throw ValidationException.of("暂未支持的支付方式及途径");
    }


    public String managerPayRefund(PayOrder entity, String reason) {
        if (entity == null || entity.getOrderNo() == null) {
            throw ValidationException.of("orderNo 不能为空");
        }
        PayOrder payOrder = payOrderService.selectOneByEntity(entity);
        if (payOrder == null) {
            throw ValidationException.of("订单不存在");
        }
        PayStatus payStatus = EnumUtils.getEnum(PayStatus.class, payOrder.getPayStatus());
        if (payStatus != PayStatus.PAID && payStatus != PayStatus.FINISHED) {
            throw ValidationException.of("订单在 {} 情况下不可退", payStatus.getValue());
        }

        // 申请退款
        PayMethod payMethod = EnumUtils.getEnum(PayMethod.class, payOrder.getPayMethod());

        if (payMethod == PayMethod.MOCK_PAY) {
            // 模拟直接修改订单状态
            payOrder.setPayStatus(PayStatus.REFUNDED.name());
            payOrderService.update(payOrder);
            return "模拟支付订单退款已完成";
        }
        if (payMethod == PayMethod.WX_PAY) {
            // 向微信发起退款申请
            return wxpayHelper.wxTradeRefund(payOrder, reason);
        }
        throw ValidationException.of("暂未支持的退款方式，请联系商家");
    }


    private void paramCheck(PayOrder payOrder) {
        payOrder.setTenantCode(SessionHelper.getTenantCode());

        Assert.notNull(payOrder.getUserCode(), "userCode 不能为空!");
        Assert.notNull(payOrder.getOrderNo(), "orderNo 不能为空!");
        Assert.notNull(payOrder.getPayMethod(), "payMethod 不能为空!");
        Assert.notNull(payOrder.getTerminalType(), "terminalType 不能为空!");

        Assert.notNull(payOrder.getTotalAmount(), "totalAmount 不能为空!");
        Assert.notNull(payOrder.getBody(), "body 不能为空!");
        Assert.notNull(payOrder.getDetail(), "detail 不能为空!");

        if (!SessionHelper.getUserCode().equals(payOrder.getUserCode())) {
            throw ValidationException.of("下单人和付款人不一致!");
        }

        if (!EnumUtils.isValidEnum(PayMethod.class, payOrder.getPayMethod())) {
            throw ValidationException.of("不支持的支付方式!");
        }

        if (Sys.getCurrentEnv() == EnvType.PROD && PayMethod.MOCK_PAY.name().equals(payOrder.getPayMethod())) {
            throw ValidationException.of("非法的支付方式!");
        }

        if (!EnumUtils.isValidEnum(TerminalType.class, payOrder.getTerminalType())) {
            throw ValidationException.of("不支持的终端类型!");
        }

        // 金额合法性检查
        if (payOrder.getDiscountAmount() == null && payOrder.getPaymentAmount() == null) {
            payOrder.setDiscountAmount(BigDecimal.ZERO);
            payOrder.setPaymentAmount(payOrder.getTotalAmount());
        }
        if (payOrder.getDiscountAmount() != null && payOrder.getPaymentAmount() == null) {
            payOrder.setPaymentAmount(payOrder.getTotalAmount().subtract(payOrder.getDiscountAmount()));
        }
        if (payOrder.getDiscountAmount() == null && payOrder.getPaymentAmount() != null) {
            payOrder.setDiscountAmount(payOrder.getTotalAmount().subtract(payOrder.getPaymentAmount()));
        }

        // 只为语法 不报错。。
        Assert.notNull(payOrder.getDiscountAmount(), "discountAmount 不能为空!");
        Assert.notNull(payOrder.getPaymentAmount(), "paymentAmount 不能为空!");

        if (payOrder.getDiscountAmount().compareTo(payOrder.getTotalAmount()) > 0) {
            throw ValidationException.of("折扣金额不能大于总金额");
        }
        if (payOrder.getDiscountAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw ValidationException.of("折扣金额不能小于0");
        }
        if (payOrder.getPaymentAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw ValidationException.of("支付金额不能小于0");
        }

        // 初始化状态
        payOrder.setPayStatus(PayStatus.PAYING.name());
        payOrder.setOutTradeNo(payOrder.getOrderNo());
    }


}

