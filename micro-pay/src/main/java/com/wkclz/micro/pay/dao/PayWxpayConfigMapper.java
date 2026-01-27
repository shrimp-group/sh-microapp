package com.wkclz.micro.pay.dao;

import com.wkclz.micro.pay.pojo.dto.PayWxpayConfigDto;
import com.wkclz.micro.pay.pojo.entity.PayWxpayConfig;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table pay_wxpay_config (支付-微信支付配置) DAO 接口，代码重新生成不覆盖
 */

@Mapper
public interface PayWxpayConfigMapper extends BaseMapper<PayWxpayConfig> {

    List<PayWxpayConfigDto> getWxpayConfigList(PayWxpayConfigDto param);

}

