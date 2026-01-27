package com.wkclz.micro.pay.spi;

import com.wkclz.micro.pay.pojo.entity.PayOrder;

public interface PayNoticeSpi {

    void payidNotice(PayOrder payOrder);

    void payTimeout(PayOrder payOrder);

    void refoundNotice(PayOrder payOrder);

}
