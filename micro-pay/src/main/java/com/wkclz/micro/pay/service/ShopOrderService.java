package com.wkclz.micro.pay.service;

import com.wkclz.core.base.R;
import com.wkclz.core.enums.EnvType;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.iam.sdk.helper.SessionHelper;
import com.wkclz.micro.pay.helper.AlipayHelper;
import com.wkclz.micro.pay.helper.WxpayHelper;
import com.wkclz.micro.pay.bean.dto.OrderInfoForPay;
import com.wkclz.micro.pay.bean.dto.OrderPayResult;
import com.wkclz.micro.pay.bean.dto.PayOrderDto;
import com.wkclz.micro.pay.bean.entity.PayOrder;
import com.wkclz.micro.pay.bean.enums.PayMethod;
import com.wkclz.micro.pay.bean.enums.PayStatus;
import com.wkclz.micro.pay.bean.enums.TerminalType;
import com.wkclz.micro.pay.bean.req.PayOrderMockPayReq;
import com.wkclz.micro.pay.bean.req.PayOrderReq;
import com.wkclz.micro.pay.config.PayConfig;
import com.wkclz.micro.pay.spi.PayOrderSpi;
import com.wkclz.micro.points.bean.req.PointsConsumeReq;
import com.wkclz.micro.points.bean.req.PointsRefundReq;
import com.wkclz.micro.points.service.PointsConsumeService;
import com.wkclz.micro.points.service.PointsRefundService;
import com.wkclz.redis.helper.RedisIdGenerator;
import com.wkclz.spring.config.Sys;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Description Create by shrimp-gen
 *
 * @author wangkaicun
 * @table pay_order (支付-订单) 单表服务类，代码重新生成不覆盖. 只建议完成单表的逻辑，或主表为 pay_order 的逻辑. 其他逻辑放 custom 中
 */

@Slf4j
@Service
public class ShopOrderService {

    @Autowired
    private PayConfig payConfig;
    @Autowired(required = false)
    private PayOrderSpi payOrderSpi;
    @Resource
    private WxpayHelper wxpayHelper;
    @Resource
    private AlipayHelper alipayHelper;
    @Resource
    private PayOrderService payOrderService;
    @Autowired
    private RedisIdGenerator redisIdGenerator;
    @Autowired
    private PointsConsumeService consumeService;
    @Autowired
    private PointsRefundService pointsRefundService;


    /**
     * 模拟支付编排方法
     * 与 payWithOrderInfo 相同的流程：获取订单信息 → 校验 → 创建支付订单(PAYING) → SPI 更新订单为 PAYING → 模拟回调标记 PAID → SPI 更新订单为 PAID
     * 差异：不调用支付平台，支付完成后立即模拟回调
     */
    @Transactional(rollbackFor = Exception.class)
    public PayOrderDto mockPayWithOrderInfo(PayOrderMockPayReq req) {
        log.info("模拟支付, orderNo: {}, payMethod: {}", req.getOrderNo(), req.getPayMethod());

        // SPI 必须实现，否则无法获取订单信息
        if (payOrderSpi == null) {
            throw ValidationException.of("订单信息查询服务未配置，无法发起支付");
        }

        // 参数校验
        if (StringUtils.isBlank(req.getOrderNo())) {
            throw ValidationException.of("orderNo 订单号不能为空");
        }
        if (StringUtils.isBlank(req.getPayMethod())) {
            throw ValidationException.of("payMethod 支付方式不能为空");
        }

        // 通过 SPI 获取订单支付信息
        OrderInfoForPay orderInfo = payOrderSpi.getOrderInfoForPay(req.getOrderNo());
        if (orderInfo == null) {
            throw ValidationException.of("订单号 %s 不存在", req.getOrderNo());
        }
        log.info("获取订单信息成功, orderNo: {}, orderStatus: {}", req.getOrderNo(), orderInfo.getOrderStatus());

        // 校验订单状态可支付（NEW/PAYING/PAYERROR）
        if (!"NEW".equals(orderInfo.getOrderStatus())
            && !"PAYING".equals(orderInfo.getOrderStatus())
            && !"PAYERROR".equals(orderInfo.getOrderStatus())) {
            throw ValidationException.of("订单无法发起支付: %s, 状态异常: %s", req.getOrderNo(), orderInfo.getOrderStatus());
        }

        // 校验金额合法性
        if (orderInfo.getDiscountAmount() != null && orderInfo.getTotalAmount() != null
            && orderInfo.getDiscountAmount().compareTo(orderInfo.getTotalAmount()) > 0) {
            throw ValidationException.of("折扣金额不能大于总金额");
        }
        if (orderInfo.getPaymentAmount() != null && orderInfo.getPaymentAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw ValidationException.of("支付金额不能小于0");
        }

        // 将订单信息填充到 PayOrder
        PayOrder payOrder = new PayOrder();
        payOrder.setOrderNo(orderInfo.getOrderNo());
        payOrder.setUserCode(orderInfo.getUserCode());
        payOrder.setTenantCode(orderInfo.getTenantCode());
        payOrder.setTotalAmount(orderInfo.getTotalAmount());
        payOrder.setDiscountAmount(orderInfo.getDiscountAmount());
        payOrder.setPaymentAmount(orderInfo.getPaymentAmount());
        payOrder.setBody(orderInfo.getOrderDesc());
        payOrder.setDetail(orderInfo.getOrderDesc());
        payOrder.setPayMethod(req.getPayMethod());
        payOrder.setPoints(orderInfo.getPoints() != null ? orderInfo.getPoints() : 0);

        // 调用 createPayOrder 创建支付订单（复用历史订单处理、outTradeNo 生成、记录插入逻辑）
        // MOCK_PAY 分支会跳过支付平台调用，paramCheck 已适配 MOCK_PAY 跳过 terminalType
        PayOrderDto result = createPayOrder(payOrder, null, null);
        log.info("模拟支付订单创建成功, orderNo: {}, outTradeNo: {}, payStatus: {}", payOrder.getOrderNo(), payOrder.getOutTradeNo(), payOrder.getPayStatus());

        // 模拟支付回调步骤（外层 try-catch，失败时释放已扣减积分）
        try {
            // 通过 SPI 更新订单状态为支付中
            try {
                payOrderSpi.updateOrderToPaying(req.getOrderNo());
                log.info("订单状态已更新为 PAYING, orderNo: {}", req.getOrderNo());
            } catch (Exception e) {
                log.error("更新订单状态为 PAYING 失败, orderNo: {}", req.getOrderNo(), e);
            }

            // 模拟支付回调：标记支付成功
            // 幂等校验
            if (PayStatus.PAID.name().equals(result.getPayStatus()) || PayStatus.FINISHED.name().equals(result.getPayStatus())) {
                throw ValidationException.of("重复支付");
            }
            result.setPayStatus(PayStatus.PAID.name());
            result.setPayFlowNo(redisIdGenerator.generateIdWithPrefix("pay_flow_"));
            result.setPayTime(LocalDateTime.now());
            payOrderService.update(result);
            log.info("模拟支付回调成功, orderNo: {}, payFlowNo: {}", req.getOrderNo(), result.getPayFlowNo());

            // 通过 SPI 更新订单支付成功状态（模拟回调）
            try {
                OrderPayResult payResult = new OrderPayResult();
                payResult.setOrderNo(result.getOrderNo());
                payResult.setPayMethod(result.getPayMethod());
                payResult.setPayFlowNo(result.getPayFlowNo());
                payResult.setPayTime(result.getPayTime());
                payOrderSpi.updateOrderToPaid(payResult);
                log.info("模拟支付成功，订单状态已更新为 PAID, orderNo: {}", result.getOrderNo());
            } catch (Exception e) {
                log.error("模拟支付成功，更新订单状态失败, orderNo: {}", result.getOrderNo(), e);
            }
        } catch (Exception e) {
            // 模拟支付回调失败，释放已扣减的积分
            if (payOrder.getPoints() != null && payOrder.getPoints() > 0) {
                try {
                    consumeService.releaseConsume(payOrder.getOutTradeNo(), "模拟支付回调失败");
                    log.warn("模拟支付回调失败已释放积分, outTradeNo={}", payOrder.getOutTradeNo());
                } catch (Exception ex) {
                    log.error("模拟支付回调失败释放积分异常, outTradeNo={}", payOrder.getOutTradeNo(), ex);
                }
            }
            throw e;
        }

        return result;
    }


    /**
     * 基于 SPI 的支付编排方法
     * 通过 PayOrderSpi 获取订单信息 → 校验 → 填充 PayOrder → 发起支付 → 更新订单状态
     */
    @Transactional(rollbackFor = Exception.class)
    public PayOrderDto payWithOrderInfo(PayOrderReq req, HttpServletRequest httpReq, HttpServletResponse httpRep) {
        log.info("发起支付, orderNo: {}, payMethod: {}, terminalType: {}", req.getOrderNo(), req.getPayMethod(), req.getTerminalType());

        // SPI 必须实现，否则无法获取订单信息
        if (payOrderSpi == null) {
            throw ValidationException.of("订单信息查询服务未配置，无法发起支付");
        }

        // 参数校验
        if (StringUtils.isBlank(req.getOrderNo())) {
            throw ValidationException.of("orderNo 订单号不能为空");
        }
        if (StringUtils.isBlank(req.getPayMethod())) {
            throw ValidationException.of("payMethod 支付方式不能为空");
        }
        if (StringUtils.isBlank(req.getTerminalType())) {
            throw ValidationException.of("terminalType 终端类型不能为空");
        }

        // 通过 SPI 获取订单支付信息
        OrderInfoForPay orderInfo = payOrderSpi.getOrderInfoForPay(req.getOrderNo());
        if (orderInfo == null) {
            throw ValidationException.of("订单号 %s 不存在", req.getOrderNo());
        }
        log.info("获取订单信息成功, orderNo: {}, orderStatus: {}", req.getOrderNo(), orderInfo.getOrderStatus());

        // 校验订单状态可支付（NEW/PAYING/PAYERROR）
        if (!"NEW".equals(orderInfo.getOrderStatus())
            && !"PAYING".equals(orderInfo.getOrderStatus())
            && !"PAYERROR".equals(orderInfo.getOrderStatus())) {
            throw ValidationException.of("订单无法发起支付: %s, 状态异常: %s", req.getOrderNo(), orderInfo.getOrderStatus());
        }

        // 校验金额合法性
        if (orderInfo.getDiscountAmount() != null && orderInfo.getTotalAmount() != null
            && orderInfo.getDiscountAmount().compareTo(orderInfo.getTotalAmount()) > 0) {
            throw ValidationException.of("折扣金额不能大于总金额");
        }
        if (orderInfo.getPaymentAmount() != null && orderInfo.getPaymentAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw ValidationException.of("支付金额不能小于0");
        }

        // 将订单信息填充到 PayOrder
        PayOrder payOrder = new PayOrder();
        payOrder.setOrderNo(orderInfo.getOrderNo());
        payOrder.setUserCode(orderInfo.getUserCode());
        payOrder.setTenantCode(orderInfo.getTenantCode());
        payOrder.setTotalAmount(orderInfo.getTotalAmount());
        payOrder.setDiscountAmount(orderInfo.getDiscountAmount());
        payOrder.setPaymentAmount(orderInfo.getPaymentAmount());
        payOrder.setBody(orderInfo.getOrderDesc());
        payOrder.setDetail(orderInfo.getOrderDesc());
        payOrder.setPayMethod(req.getPayMethod());
        payOrder.setTerminalType(req.getTerminalType());
        payOrder.setPoints(orderInfo.getPoints() != null ? orderInfo.getPoints() : 0);

        // 调用 createPayOrder 发起支付
        PayOrderDto result = createPayOrder(payOrder, httpReq, httpRep);
        log.info("支付订单创建成功, orderNo: {}, outTradeNo: {}, payStatus: {}", payOrder.getOrderNo(), payOrder.getOutTradeNo(), payOrder.getPayStatus());

        // 通过 SPI 更新订单状态为支付中
        try {
            payOrderSpi.updateOrderToPaying(req.getOrderNo());
            log.info("订单状态已更新为 PAYING, orderNo: {}", req.getOrderNo());
        } catch (Exception e) {
            log.error("更新订单状态为 PAYING 失败, orderNo: {}", req.getOrderNo(), e);
        }

        return result;
    }

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

        // 积分消费（在 outTradeNo 生成且 payOrder 持久化之后、调用支付 helper 之前）
        // 使用 outTradeNo 作为 consume 的 orderNo，保证重试支付时幂等键不冲突
        if (model.getPoints() != null && model.getPoints() > 0) {
            PointsConsumeReq consumeReq = new PointsConsumeReq();
            consumeReq.setTenantCode(model.getTenantCode());
            consumeReq.setUserCode(model.getUserCode());
            consumeReq.setPoints(model.getPoints());
            consumeReq.setOrderNo(model.getOutTradeNo());
            consumeReq.setReason("订单支付");
            log.info("支付前积分消费开始, outTradeNo={}, userCode={}, points={}",
                    model.getOutTradeNo(), model.getUserCode(), model.getPoints());
            consumeService.consume(consumeReq);
            log.info("支付前积分消费成功, outTradeNo={}, userCode={}, points={}",
                    model.getOutTradeNo(), model.getUserCode(), model.getPoints());
        }

        // 调用支付 helper（包裹 try-catch，失败时补偿已扣减积分）
        try {
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
        } catch (Exception e) {
            // 支付失败补偿：释放已扣减的积分
            if (model.getPoints() != null && model.getPoints() > 0) {
                try {
                    consumeService.releaseConsume(model.getOutTradeNo(), "支付失败");
                    log.warn("支付失败已释放积分, outTradeNo={}", model.getOutTradeNo());
                } catch (Exception ex) {
                    log.error("支付失败释放积分异常, outTradeNo={}", model.getOutTradeNo(), ex);
                }
            }
            throw e;
        }
        // 银联
        if (PayMethod.UNION_PAY == payMethod) {
            // 有需求再继续
        }
        throw ValidationException.of("暂未支持的支付方式及途径");
    }


    public String managerPayRefund(PayOrder entity, BigDecimal refundAmount, String refundNo, String reason, String subOrderNo) {
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

        // 防御性初始化 refundedAmount / points
        if (payOrder.getRefundedAmount() == null) {
            payOrder.setRefundedAmount(BigDecimal.ZERO);
        }
        if (payOrder.getPoints() == null) {
            payOrder.setPoints(0);
        }

        // 申请退款
        PayMethod payMethod = EnumUtils.getEnum(PayMethod.class, payOrder.getPayMethod());
        String reasonStr = (reason == null || reason.isBlank()) ? "发起退款" : reason;

        boolean isSubOrderRefund = StringUtils.isNotBlank(subOrderNo);

        // ===== 总单退款分支（isSubOrderRefund == false） =====
        if (!isSubOrderRefund) {
            log.info("总单退款开始, outTradeNo={}, payMethod={}, points={}", payOrder.getOutTradeNo(), payMethod, payOrder.getPoints());

            // 积分全单回退：releaseConsume 处理 FROZEN 释放 + DEDUCTED 退回（不重复调用 pointsRefundService.refund，避免重复退款）
            if (payOrder.getPoints() != null && payOrder.getPoints() > 0) {
                if (payConfig.getPointsRefundOnRefundEnable() == 1) {
                    log.info("总单退款-积分回退开始, outTradeNo={}, points={}", payOrder.getOutTradeNo(), payOrder.getPoints());
                    try {
                        consumeService.releaseConsume(payOrder.getOutTradeNo(), reasonStr);
                        log.info("总单退款-积分回退成功, outTradeNo={}", payOrder.getOutTradeNo());
                    } catch (Exception e) {
                        log.error("总单退款-积分回退失败, outTradeNo={}", payOrder.getOutTradeNo(), e);
                        throw e;
                    }
                } else {
                    log.info("退款积分退还开关关闭，跳过积分退还, outTradeNo={}", payOrder.getOutTradeNo());
                }
            }

            // 支付通道退款
            if (payMethod == PayMethod.MOCK_PAY) {
                // 模拟直接修改订单状态
                payOrder.setPayStatus(PayStatus.REFUNDED.name());
                payOrder.setRefundedAmount(payOrder.getPaymentAmount());
                payOrderService.update(payOrder);
                log.info("模拟支付总单退款完成, outTradeNo={}", payOrder.getOutTradeNo());
                return "模拟支付订单退款已完成";
            }
            if (payMethod == PayMethod.WX_PAY) {
                // 向微信发起退款申请（总单退款）
                String result = wxpayHelper.wxTradeRefund(payOrder, null, refundNo, reasonStr);
                payOrder.setRefundedAmount(payOrder.getPaymentAmount());
                payOrderService.update(payOrder);
                log.info("微信支付总单退款已发起, outTradeNo={}", payOrder.getOutTradeNo());
                return result;
            }
            throw ValidationException.of("暂未支持的退款方式，请联系商家");
        }

        // ===== 子单退款分支（isSubOrderRefund == true） =====
        log.info("子单退款开始, outTradeNo={}, subOrderNo={}, refundAmount={}, refundNo={}", payOrder.getOutTradeNo(), subOrderNo, refundAmount, refundNo);

        // SPI 必须实现
        if (payOrderSpi == null) {
            throw ValidationException.of("订单交互服务未配置");
        }

        // refundNo 自动生成
        if (refundNo == null || refundNo.isBlank()) {
            refundNo = payOrder.getOutTradeNo() + "-R" + System.currentTimeMillis();
            log.info("子单退款 refundNo 自动生成, outTradeNo={}, refundNo={}", payOrder.getOutTradeNo(), refundNo);
        }

        // 通过 SPI 查询子单
        OrderInfoForPay subOrderInfo = payOrderSpi.getOrderInfoForPay(subOrderNo);
        if (subOrderInfo == null) {
            throw ValidationException.of("子单 %s 不存在", subOrderNo);
        }
        Integer subOrderPoints = subOrderInfo.getPoints() != null ? subOrderInfo.getPoints() : 0;
        log.info("子单退款-子单积分, subOrderNo={}, subOrderPoints={}", subOrderNo, subOrderPoints);

        // 校验退款金额不超额
        if (refundAmount != null) {
            BigDecimal totalAfterRefund = payOrder.getRefundedAmount().add(refundAmount);
            if (totalAfterRefund.compareTo(payOrder.getPaymentAmount()) > 0) {
                throw ValidationException.of("退款金额超出可退金额，已退：%s，本次退：%s，订单总额：%s",
                        payOrder.getRefundedAmount(), refundAmount, payOrder.getPaymentAmount());
            }
        }

        // 调用积分退款（仅当开关开启且 subOrderPoints > 0）
        if (subOrderPoints > 0) {
            if (payConfig.getPointsRefundOnRefundEnable() == 1) {
                PointsRefundReq req = new PointsRefundReq();
                req.setTenantCode(payOrder.getTenantCode());
                req.setUserCode(payOrder.getUserCode());
                req.setPoints(subOrderPoints);
                req.setReason(reasonStr);
                // 关键：用 outTradeNo 而非 orderNo，与消费时一致（消费幂等键以 outTradeNo 为 orderNo）
                req.setOrderNo(payOrder.getOutTradeNo());
                req.setRefundNo(refundNo);
                log.info("子单退款-积分退款调用开始, outTradeNo={}, refundNo={}, subOrderPoints={}",
                        payOrder.getOutTradeNo(), refundNo, subOrderPoints);
                try {
                    pointsRefundService.refund(req);
                    log.info("子单退款-积分退款成功, outTradeNo={}, refundNo={}", payOrder.getOutTradeNo(), refundNo);
                } catch (Exception e) {
                    log.error("子单退款-积分退款失败, outTradeNo={}, refundNo={}", payOrder.getOutTradeNo(), refundNo, e);
                    throw e;
                }
            } else {
                log.info("退款积分退还开关关闭，跳过积分退还, outTradeNo={}, subOrderNo={}, subOrderPoints={}",
                        payOrder.getOutTradeNo(), subOrderNo, subOrderPoints);
            }
        }

        // 支付通道退款
        if (payMethod == PayMethod.MOCK_PAY) {
            // 模拟支付：直接修改状态（按业务简化处理：子单退也置 REFUNDED）
            payOrder.setPayStatus(PayStatus.REFUNDED.name());
            payOrder.setRefundedAmount(payOrder.getRefundedAmount().add(refundAmount));
            payOrderService.update(payOrder);
            log.info("模拟支付子单退款完成, outTradeNo={}, subOrderNo={}, refundedAmount={}", payOrder.getOutTradeNo(), subOrderNo, payOrder.getRefundedAmount());
            return "模拟支付订单退款已完成";
        }
        if (payMethod == PayMethod.WX_PAY) {
            // 向微信发起退款申请
            String result = wxpayHelper.wxTradeRefund(payOrder, refundAmount, refundNo, reasonStr);
            payOrder.setRefundedAmount(payOrder.getRefundedAmount().add(refundAmount));
            payOrderService.update(payOrder);
            log.info("微信支付子单退款已发起, outTradeNo={}, subOrderNo={}, refundedAmount={}", payOrder.getOutTradeNo(), subOrderNo, payOrder.getRefundedAmount());
            return result;
        }
        throw ValidationException.of("暂未支持的退款方式，请联系商家");
    }


    private void paramCheck(PayOrder payOrder) {
        payOrder.setTenantCode(SessionHelper.getTenantCode());
        if (payOrder.getDiscountAmount() == null) {
            payOrder.setDiscountAmount(BigDecimal.ZERO);
        }
        if (payOrder.getPaymentAmount() == null) {
            payOrder.setPaymentAmount(BigDecimal.ZERO);
        }

        if (!EnumUtils.isValidEnum(PayMethod.class, payOrder.getPayMethod())) {
            throw ValidationException.of("不支持的支付方式!");
        }

        if (PayMethod.MOCK_PAY.name().equals(payOrder.getPayMethod())) {
            // 模拟支付需要检测的内容
            if (Sys.getCurrentEnv() == EnvType.PROD) {
                throw ValidationException.of("非法的支付方式!");
            }
        } else {
            // 真实支付需要检测的内容
            if (!SessionHelper.getUserCode().equals(payOrder.getUserCode())) {
                throw ValidationException.of("下单人和付款人不一致!");
            }
            // terminalType 检查
            if (!EnumUtils.isValidEnum(TerminalType.class, payOrder.getTerminalType())) {
                throw ValidationException.of("不支持的终端类型!");
            }
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

