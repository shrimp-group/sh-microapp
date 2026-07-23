package com.wkclz.micro.pay.bean.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.wkclz.core.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table pay_order (支付-订单) 重新生成代码会覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class PayOrder extends BaseEntity {

    /**
     * 租户编码
     */
    @Schema(description = "租户编码")
    private String tenantCode;

    /**
     * 用户编码
     */
    @Schema(description = "用户编码")
    private String userCode;

    /**
     * 支付订单号
     */
    @Schema(description = "支付订单号")
    private String outTradeNo;

    /**
     * 订单号
     */
    @Schema(description = "订单号")
    private String orderNo;

    /**
     * 【平台】总金额
     */
    @Schema(description = "【平台】总金额")
    private BigDecimal totalAmount;

    /**
     * 【平台】优惠金额
     */
    @Schema(description = "【平台】优惠金额")
    private BigDecimal discountAmount;

    /**
     * 【平台】支付金额
     */
    @Schema(description = "【平台】支付金额")
    private BigDecimal paymentAmount;

    /**
     * 支付状态
     */
    @Schema(description = "支付状态")
    private String payStatus;

    /**
     * 支付方式
     */
    @Schema(description = "支付方式")
    private String payMethod;

    /**
     * 支付流水号
     */
    @Schema(description = "支付流水号")
    private String payFlowNo;

    /**
     * 支付时间
     */
    @Schema(description = "支付时间")
    private LocalDateTime payTime;

    /**
     * 开发票状态(-1不可开发票,0未开发票,1已开发票2已过期)
     */
    @Schema(description = "开发票状态(-1不可开发票,0未开发票,1已开发票2已过期)")
    private Integer invoiceStatus;

    /**
     * 商品描述
     */
    @Schema(description = "商品描述")
    private String body;

    /**
     * 商品详情
     */
    @Schema(description = "商品详情")
    private String detail;

    /**
     * 终端类型
     */
    @Schema(description = "终端类型")
    private String terminalType;

    /**
     * 公众号/小程序ID
     */
    @Schema(description = "公众号/小程序ID")
    private String appid;

    /**
     * 商户号
     */
    @Schema(description = "商户号")
    private String mchId;

    /**
     * 设备号
     */
    @Schema(description = "设备号")
    private String deviceInfo;

    /**
     * 随机字符串
     */
    @Schema(description = "随机字符串")
    private String nonceStr;

    /**
     * 签名
     */
    @Schema(description = "签名")
    private String sign;

    /**
     * 签名类型
     */
    @Schema(description = "签名类型")
    private String signType;

    /**
     * 微信openId
     */
    @Schema(description = "微信openId")
    private String openId;

    /**
     * 交易类型:JSAPI,NATIVE,APP
     */
    @Schema(description = "交易类型:JSAPI,NATIVE,APP")
    private String tradeType;

    /**
     * 付款银行
     */
    @Schema(description = "付款银行")
    private String bankType;

    /**
     * 订单金额
     */
    @Schema(description = "订单金额")
    private BigDecimal totalFee;

    /**
     * 应结订单金额
     */
    @Schema(description = "应结订单金额")
    private BigDecimal settlementTotalFee;

    /**
     * 货币种类
     */
    @Schema(description = "货币种类")
    private String feeType;

    /**
     * 现金支付金额
     */
    @Schema(description = "现金支付金额")
    private BigDecimal cashFee;

    /**
     * 现金支付货币类型
     */
    @Schema(description = "现金支付货币类型")
    private String cashFeeType;

    /**
     * 总代金券金额
     */
    @Schema(description = "总代金券金额")
    private BigDecimal couponFee;

    /**
     * 代金券使用数量
     */
    @Schema(description = "代金券使用数量")
    private Integer couponCount;

    /**
     * 代金券JSON
     */
    @Schema(description = "代金券JSON")
    private String coupon;

    /**
     * 是否关注公众账号
     */
    @Schema(description = "是否关注公众账号	")
    private Integer isSubscribe;

    /**
     * 本次支付使用的积分数量
     */
    @Schema(description = "本次支付使用的积分数量")
    private Integer points = 0;

    /**
     * 已退款现金金额累计
     */
    @Schema(description = "已退款现金金额累计")
    private BigDecimal refundedAmount = BigDecimal.ZERO;


    public static PayOrder copy(PayOrder source, PayOrder target) {
        if (target == null ) { target = new PayOrder();}
        if (source == null) { return target; }
        target.setId(source.getId());
        target.setTenantCode(source.getTenantCode());
        target.setUserCode(source.getUserCode());
        target.setOutTradeNo(source.getOutTradeNo());
        target.setOrderNo(source.getOrderNo());
        target.setTotalAmount(source.getTotalAmount());
        target.setDiscountAmount(source.getDiscountAmount());
        target.setPaymentAmount(source.getPaymentAmount());
        target.setPayStatus(source.getPayStatus());
        target.setPayMethod(source.getPayMethod());
        target.setPayFlowNo(source.getPayFlowNo());
        target.setPayTime(source.getPayTime());
        target.setInvoiceStatus(source.getInvoiceStatus());
        target.setBody(source.getBody());
        target.setDetail(source.getDetail());
        target.setTerminalType(source.getTerminalType());
        target.setAppid(source.getAppid());
        target.setMchId(source.getMchId());
        target.setDeviceInfo(source.getDeviceInfo());
        target.setNonceStr(source.getNonceStr());
        target.setSign(source.getSign());
        target.setSignType(source.getSignType());
        target.setOpenId(source.getOpenId());
        target.setTradeType(source.getTradeType());
        target.setBankType(source.getBankType());
        target.setTotalFee(source.getTotalFee());
        target.setSettlementTotalFee(source.getSettlementTotalFee());
        target.setFeeType(source.getFeeType());
        target.setCashFee(source.getCashFee());
        target.setCashFeeType(source.getCashFeeType());
        target.setCouponFee(source.getCouponFee());
        target.setCouponCount(source.getCouponCount());
        target.setCoupon(source.getCoupon());
        target.setIsSubscribe(source.getIsSubscribe());
        target.setPoints(source.getPoints());
        target.setRefundedAmount(source.getRefundedAmount());
        target.setSort(source.getSort());
        target.setCreateTime(source.getCreateTime());
        target.setCreateBy(source.getCreateBy());
        target.setUpdateTime(source.getUpdateTime());
        target.setUpdateBy(source.getUpdateBy());
        target.setRemark(source.getRemark());
        target.setVersion(source.getVersion());
        return target;
    }

    public static PayOrder copyIfNotNull(PayOrder source, PayOrder target) {
        if (target == null ) { target = new PayOrder();}
        if (source == null) { return target; }
        if (source.getId() != null) { target.setId(source.getId()); }
        if (source.getTenantCode() != null) { target.setTenantCode(source.getTenantCode()); }
        if (source.getUserCode() != null) { target.setUserCode(source.getUserCode()); }
        if (source.getOutTradeNo() != null) { target.setOutTradeNo(source.getOutTradeNo()); }
        if (source.getOrderNo() != null) { target.setOrderNo(source.getOrderNo()); }
        if (source.getTotalAmount() != null) { target.setTotalAmount(source.getTotalAmount()); }
        if (source.getDiscountAmount() != null) { target.setDiscountAmount(source.getDiscountAmount()); }
        if (source.getPaymentAmount() != null) { target.setPaymentAmount(source.getPaymentAmount()); }
        if (source.getPayStatus() != null) { target.setPayStatus(source.getPayStatus()); }
        if (source.getPayMethod() != null) { target.setPayMethod(source.getPayMethod()); }
        if (source.getPayFlowNo() != null) { target.setPayFlowNo(source.getPayFlowNo()); }
        if (source.getPayTime() != null) { target.setPayTime(source.getPayTime()); }
        if (source.getInvoiceStatus() != null) { target.setInvoiceStatus(source.getInvoiceStatus()); }
        if (source.getBody() != null) { target.setBody(source.getBody()); }
        if (source.getDetail() != null) { target.setDetail(source.getDetail()); }
        if (source.getTerminalType() != null) { target.setTerminalType(source.getTerminalType()); }
        if (source.getAppid() != null) { target.setAppid(source.getAppid()); }
        if (source.getMchId() != null) { target.setMchId(source.getMchId()); }
        if (source.getDeviceInfo() != null) { target.setDeviceInfo(source.getDeviceInfo()); }
        if (source.getNonceStr() != null) { target.setNonceStr(source.getNonceStr()); }
        if (source.getSign() != null) { target.setSign(source.getSign()); }
        if (source.getSignType() != null) { target.setSignType(source.getSignType()); }
        if (source.getOpenId() != null) { target.setOpenId(source.getOpenId()); }
        if (source.getTradeType() != null) { target.setTradeType(source.getTradeType()); }
        if (source.getBankType() != null) { target.setBankType(source.getBankType()); }
        if (source.getTotalFee() != null) { target.setTotalFee(source.getTotalFee()); }
        if (source.getSettlementTotalFee() != null) { target.setSettlementTotalFee(source.getSettlementTotalFee()); }
        if (source.getFeeType() != null) { target.setFeeType(source.getFeeType()); }
        if (source.getCashFee() != null) { target.setCashFee(source.getCashFee()); }
        if (source.getCashFeeType() != null) { target.setCashFeeType(source.getCashFeeType()); }
        if (source.getCouponFee() != null) { target.setCouponFee(source.getCouponFee()); }
        if (source.getCouponCount() != null) { target.setCouponCount(source.getCouponCount()); }
        if (source.getCoupon() != null) { target.setCoupon(source.getCoupon()); }
        if (source.getIsSubscribe() != null) { target.setIsSubscribe(source.getIsSubscribe()); }
        if (source.getPoints() != null) { target.setPoints(source.getPoints()); }
        if (source.getRefundedAmount() != null) { target.setRefundedAmount(source.getRefundedAmount()); }
        if (source.getSort() != null) { target.setSort(source.getSort()); }
        if (source.getCreateTime() != null) { target.setCreateTime(source.getCreateTime()); }
        if (source.getCreateBy() != null) { target.setCreateBy(source.getCreateBy()); }
        if (source.getUpdateTime() != null) { target.setUpdateTime(source.getUpdateTime()); }
        if (source.getUpdateBy() != null) { target.setUpdateBy(source.getUpdateBy()); }
        if (source.getRemark() != null) { target.setRemark(source.getRemark()); }
        if (source.getVersion() != null) { target.setVersion(source.getVersion()); }
        return target;
    }

}

