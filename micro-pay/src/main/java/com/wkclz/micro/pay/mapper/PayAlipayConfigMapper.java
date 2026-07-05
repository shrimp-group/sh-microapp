package com.wkclz.micro.pay.mapper;

import com.wkclz.micro.pay.bean.dto.PayAlipayConfigDto;
import com.wkclz.micro.pay.bean.entity.PayAlipayConfig;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table pay_alipay_config (支付-支付宝配置) Mapper 接口，代码重新生成不覆盖
 */

@Mapper
public interface PayAlipayConfigMapper extends BaseMapper<PayAlipayConfig> {

    List<PayAlipayConfigDto> getAlipayConfigList(PayAlipayConfigDto param);

}

