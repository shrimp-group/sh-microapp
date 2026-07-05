package com.wkclz.micro.points.mapper;

import com.wkclz.micro.points.bean.entity.PointsWallet;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 积分钱包 Mapper
 * @table points_wallet (积分钱包)
 * @author sh-microapp
 */
@Mapper
public interface PointsWalletMapper extends BaseMapper<PointsWallet> {

    /**
     * 按 tenant_code + user_code 查询钱包
     */
    PointsWallet selectByUserCode(@Param("tenantCode") String tenantCode, @Param("userCode") String userCode);

    /**
     * 乐观锁更新积分（available/frozen/total_earned）
     */
    int updatePointsByVersion(@Param("id") Long id,
                             @Param("availablePoints") Integer availablePoints,
                             @Param("frozenPoints") Integer frozenPoints,
                             @Param("totalEarnedPoints") Integer totalEarnedPoints,
                             @Param("version") Integer version);

}
