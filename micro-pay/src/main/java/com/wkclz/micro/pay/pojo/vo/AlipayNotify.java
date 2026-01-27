package com.wkclz.micro.pay.pojo.vo;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author shrimp
 */
@Data
public class AlipayNotify implements Serializable {

    @JSONField(name = "gmt_create")
    private Date gmtCreate;
    private String charset;
    @JSONField(name = "seller_email")
    private String sellerEmail;
    private String subject;
    private String sign;
    private String body;
    @JSONField(name = "buyer_id")
    private String buyerId;
    @JSONField(name = "invoice_amount")
    private BigDecimal invoiceAmount;
    @JSONField(name = "notify_id")
    private String notifyId;
    @JSONField(name = "fund_bill_list")
    private List<Map<String, Object>> fundBillList;
    @JSONField(name = "notify_type")
    private String notifyType;
    @JSONField(name = "trade_status")
    private String tradeStatus;
    @JSONField(name = "receipt_amount")
    private BigDecimal receiptAmount;
    @JSONField(name = "buyer_pay_amount")
    private BigDecimal buyerPayAmount;
    @JSONField(name = "app_id")
    private String appId;
    @JSONField(name = "sign_type")
    private String signType;
    @JSONField(name = "seller_id")
    private String sellerId;
    @JSONField(name = "gmt_payment")
    private Date gmtPayment;
    @JSONField(name = "notify_time")
    private Date notifyTime;
    private String version;
    @JSONField(name = "out_trade_no")
    private String outTradeNo;
    @JSONField(name = "total_amount")
    private BigDecimal totalAmount;
    @JSONField(name = "trade_no")
    private String tradeNo;
    @JSONField(name = "auth_app_id")
    private String authAppId;
    @JSONField(name = "buyer_logon_id")
    private String buyerLogonId;
    @JSONField(name = "point_amount")
    private BigDecimal pointAmount;




}
