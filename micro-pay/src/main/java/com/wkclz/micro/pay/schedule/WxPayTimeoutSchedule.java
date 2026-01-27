package com.wkclz.micro.pay.schedule;

import com.wkclz.micro.pay.config.PayConfig;
import com.wkclz.micro.pay.dao.PayOrderMapper;
import com.wkclz.micro.pay.pojo.dto.PayOrderDto;
import com.wkclz.micro.pay.pojo.entity.PayOrder;
import com.wkclz.micro.pay.spi.PayNoticeSpi;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.ZoneId;
import java.util.List;

@Slf4j
@Component
public class WxPayTimeoutSchedule {

    private static final List<String> FOR_TIMEOUT_STATUS = List.of("NEW", "PAYING");

    @Resource
    private PayConfig payConfig;
    @Autowired(required = false)
    private PayNoticeSpi payNoticeSpi;
    @Resource
    private PayOrderMapper payOrderMapper;

    @Scheduled(fixedDelay = 300_000, initialDelay = 38_000)
    public void wxOrderPayStatusSync() {
        Integer enable = payConfig.getPayTimeoutCancelEnable();
        if (enable == null || enable != 1) {
            return;
        }

        // 超时时间
        int minute = payConfig.getPayTimeoutCancelMinute();
        minute = Math.max(minute, 10);
        minute = Math.min(minute, 1440);

        PayOrderDto param = new PayOrderDto();
        param.setTimeoutMinute(minute);

        List<PayOrder> orders = payOrderMapper.getTimeoutPayingOrders(param);
        if (CollectionUtils.isEmpty(orders)) {
            return;
        }

        long now = System.currentTimeMillis();

        for (PayOrder order : orders) {
            if (!FOR_TIMEOUT_STATUS.contains(order.getPayStatus())) {
                continue;
            }

            long createTime = order.getCreateTime().atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();
            if (now - createTime <  minute * 60_000) {
                continue;
            }
            order.setPayStatus("TIMEOUT_CANCEL");
            payOrderMapper.updateByIdSelective(order);

            if (payNoticeSpi != null) {
                payNoticeSpi.payTimeout(order);
            }

            log.info("订单支付超时取消，订单号: {}", order.getOrderNo());
        }
    }


}
