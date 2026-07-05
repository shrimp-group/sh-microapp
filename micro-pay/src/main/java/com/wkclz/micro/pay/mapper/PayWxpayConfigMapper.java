package com.wkclz.micro.pay.mapper;

import com.wkclz.micro.pay.bean.dto.PayWxpayConfigDto;
import com.wkclz.micro.pay.bean.entity.PayWxpayConfig;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table pay_wxpay_config (支付-微信支付配置) Mapper 接口，代码重新生成不覆盖
 */

@Mapper
public interface PayWxpayConfigMapper extends BaseMapper<PayWxpayConfig> {

    List<PayWxpayConfigDto> getWxpayConfigList(PayWxpayConfigDto param);

}

