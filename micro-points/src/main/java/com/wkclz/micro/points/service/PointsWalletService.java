package com.wkclz.micro.points.service;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.points.bean.entity.PointsWallet;
import com.wkclz.micro.points.mapper.PointsWalletMapper;
import com.wkclz.mybatis.service.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

/**
 * 积分钱包服务
 * <p>
 * 维护钱包三态（可用/冻结/历史总额），按 (tenant_code, user_code) 唯一。
 * 首次发放/查询时按需创建钱包（available=0, frozen=0, total_earned=0）。
 * <p>
 * 本服务的写方法（addAvailable/freeze/releaseFrozen）不加 @Transactional，
 * 由调用方（发放/消费/回退/异步扣减服务）统一管理事务，以保证多表操作的原子性。
 *
 * @see PointsWallet
 */
@Slf4j
@Service
public class PointsWalletService extends BaseService<PointsWallet, PointsWalletMapper> {

    /**
     * 按需获取或创建钱包。
     * <p>
     * 首次给用户发放积分时自动创建钱包（available=0, frozen=0, total_earned=0）。
     * 并发创建依赖 uk(tenant_code, user_code) 索引，insert 冲突时重新查询。
     *
     * @param tenantCode 租户编码
     * @param userCode   用户编码
     * @return 钱包实体（必定非空）
     */
    public PointsWallet getOrCreateWallet(String tenantCode, String userCode) {
        if (tenantCode == null || tenantCode.isBlank()) {
            throw ValidationException.of("租户编码不能为空");
        }
        if (userCode == null || userCode.isBlank()) {
            throw ValidationException.of("用户编码不能为空");
        }
        PointsWallet wallet = mapper.selectByUserCode(tenantCode, userCode);
        if (wallet != null) {
            return wallet;
        }
        log.info("钱包不存在，自动创建, tenantCode={}, userCode={}", tenantCode, userCode);
        PointsWallet newWallet = new PointsWallet();
        newWallet.setTenantCode(tenantCode);
        newWallet.setUserCode(userCode);
        newWallet.setAvailablePoints(0);
        newWallet.setFrozenPoints(0);
        newWallet.setTotalEarnedPoints(0);
        try {
            mapper.insert(newWallet);
            log.info("钱包创建成功, tenantCode={}, userCode={}, id={}", tenantCode, userCode, newWallet.getId());
        } catch (DuplicateKeyException e) {
            // 并发创建：唯一索引冲突，重新查询已创建的钱包
            log.info("钱包并发创建冲突，重新查询, tenantCode={}, userCode={}", tenantCode, userCode);
            wallet = mapper.selectByUserCode(tenantCode, userCode);
            if (wallet == null) {
                throw ValidationException.of("钱包创建失败，请重试");
            }
            return wallet;
        }
        return newWallet;
    }

    /**
     * 累加可用积分与历史总额。
     * <p>
     * 用于发放/管理员发放/回退：available += points, total_earned += points。
     *
     * @param tenantCode 租户编码
     * @param userCode   用户编码
     * @param points     积分数（正数）
     */
    public void addAvailable(String tenantCode, String userCode, Integer points) {
        log.info("钱包累加可用积分, tenantCode={}, userCode={}, points={}", tenantCode, userCode, points);
        PointsWallet wallet = getOrCreateWallet(tenantCode, userCode);
        Integer newAvailable = wallet.getAvailablePoints() + points;
        Integer newTotal = wallet.getTotalEarnedPoints() + points;
        int rows = mapper.updatePointsByVersion(
                wallet.getId(), newAvailable, wallet.getFrozenPoints(), newTotal, wallet.getVersion());
        if (rows < 1) {
            log.warn("钱包累加乐观锁失败, walletId={}, version={}", wallet.getId(), wallet.getVersion());
            throw ValidationException.of("钱包更新冲突，请重试");
        }
    }

    /**
     * 冻结积分：校验余额后将可用转为冻结。
     * <p>
     * 用于消费：available -= points, frozen += points。
     *
     * @param tenantCode 租户编码
     * @param userCode   用户编码
     * @param points     积分数（正数）
     */
    public void freeze(String tenantCode, String userCode, Integer points) {
        log.info("钱包冻结积分, tenantCode={}, userCode={}, points={}", tenantCode, userCode, points);
        PointsWallet wallet = getOrCreateWallet(tenantCode, userCode);
        if (wallet.getAvailablePoints() < points) {
            log.warn("可用积分不足, userCode={}, available={}, need={}",
                    userCode, wallet.getAvailablePoints(), points);
            throw ValidationException.of("可用积分不足");
        }
        Integer newAvailable = wallet.getAvailablePoints() - points;
        Integer newFrozen = wallet.getFrozenPoints() + points;
        int rows = mapper.updatePointsByVersion(
                wallet.getId(), newAvailable, newFrozen, wallet.getTotalEarnedPoints(), wallet.getVersion());
        if (rows < 1) {
            log.warn("钱包冻结乐观锁失败, walletId={}, version={}", wallet.getId(), wallet.getVersion());
            throw ValidationException.of("钱包更新冲突，请重试");
        }
    }

    /**
     * 释放冻结积分：将冻结转为可用。
     * <p>
     * 用于异步扣减完成（frozen -= points）或消费回滚。
     *
     * @param tenantCode 租户编码
     * @param userCode   用户编码
     * @param points     积分数（正数）
     */
    public void releaseFrozen(String tenantCode, String userCode, Integer points) {
        log.info("钱包释放冻结积分, tenantCode={}, userCode={}, points={}", tenantCode, userCode, points);
        PointsWallet wallet = getOrCreateWallet(tenantCode, userCode);
        if (wallet.getFrozenPoints() < points) {
            log.warn("冻结积分不足, userCode={}, frozen={}, need={}",
                    userCode, wallet.getFrozenPoints(), points);
            throw ValidationException.of("冻结积分不足");
        }
        Integer newFrozen = wallet.getFrozenPoints() - points;
        int rows = mapper.updatePointsByVersion(
                wallet.getId(), wallet.getAvailablePoints(), newFrozen, wallet.getTotalEarnedPoints(), wallet.getVersion());
        if (rows < 1) {
            log.warn("钱包释放冻结乐观锁失败, walletId={}, version={}", wallet.getId(), wallet.getVersion());
            throw ValidationException.of("钱包更新冲突，请重试");
        }
    }

}
