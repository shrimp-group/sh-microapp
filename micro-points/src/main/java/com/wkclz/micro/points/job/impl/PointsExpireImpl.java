package com.wkclz.micro.points.job.impl;

import com.wkclz.micro.points.bean.entity.PointsEarnRecord;
import com.wkclz.micro.points.bean.req.PointsConsumeReq;
import com.wkclz.micro.points.bean.resp.PointsConsumeResp;
import com.wkclz.micro.points.mapper.PointsEarnRecordMapper;
import com.wkclz.micro.points.service.PointsConsumeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 积分过期定时任务（spec §5.6, §13.7 US-EX-001/002）
 * <p>
 * 调度方式（弱依赖）：
 * - 两个 handler 互斥（通过 @ConditionalOnClass / @ConditionalOnMissingClass 控制），避免重复触发
 * <p>
 * 流程：分页扫描过期且可用的获取流水（expire_time &lt; now AND available_points &gt; 0），
 * 对每条调用 {@link PointsConsumeService#consume} 模拟消费流程
 * （reason=积分过期, order_no=EXPIRY+flowNo），触发冻结→异步扣减链路，保证数据一致。
 * <p>
 * 过期与消费冻结的竞态（spec §13.7 US-EX-002）：
 * 已冻结的过期积分，过期消费会因钱包余额校验失败跳过该流水，不报错。
 * 等异步扣减后该获取流水 available=0，过期不再处理。
 *
 * @see PointsConsumeService#consume(PointsConsumeReq)
 */
@Slf4j
@Service
public class PointsExpireImpl {


    @Autowired
    private PointsEarnRecordMapper earnMapper;
    @Autowired
    private PointsConsumeService consumeService;

    /** 过期消费单据号前缀（spec §5.6: order_no=EXPIRY+flowNo） */
    static final String EXPIRY_ORDER_NO_PREFIX = "EXPIRY";

    /** 分页大小：每页 100 条，避免一次加载太多 */
    static final int PAGE_LIMIT = 100;

    /**
     * 执行过期积分处理（核心逻辑，由具体 handler 调用）。
     * <p>
     * Handler 名称：pointsExpireHandler（spec §13.7 US-EX-001）。
     * 单条过期处理失败不影响其他条；余额不足为正常情况，跳过不报错。
     */
    public void doExecute() {
        log.info("积分过期定时任务启动");
        LocalDateTime now = LocalDateTime.now();
        int offset = 0;
        int processedCount = 0;
        int skippedCount = 0;

        while (true) {
            // 分页扫描过期且可用的获取流水
            List<PointsEarnRecord> expiredList = earnMapper.selectExpiredAvailable(now, offset, PAGE_LIMIT);
            if (expiredList == null || expiredList.isEmpty()) {
                log.info("扫描过期流水结束, 当前页无数据, offset={}", offset);
                break;
            }

            log.info("扫描到过期流水, 本页数量={}, offset={}", expiredList.size(), offset);

            for (PointsEarnRecord earn : expiredList) {
                try {
                    // 构造过期消费请求：points=可用积分, orderNo=EXPIRY+flowNo
                    PointsConsumeReq req = new PointsConsumeReq();
                    req.setTenantCode(earn.getTenantCode());
                    req.setUserCode(earn.getUserCode());
                    req.setPoints(earn.getAvailablePoints());
                    req.setReason("积分过期：" + earn.getFlowNo());
                    req.setOrderNo(EXPIRY_ORDER_NO_PREFIX + earn.getFlowNo());

                    PointsConsumeResp resp = consumeService.consume(req);
                    log.info("过期处理成功, earnFlowNo={}, userCode={}, points={}, consumeFlowNo={}",
                            earn.getFlowNo(), earn.getUserCode(), earn.getAvailablePoints(), resp.getFlowNo());
                    processedCount++;
                } catch (Exception e) {
                    // 余额不足是正常情况（spec §13.7 US-EX-002）：已冻结的过期积分，
                    // 过期消费会因钱包余额校验失败跳过。此为正确行为，不报错；
                    // 等异步扣减后该获取流水 available=0，过期不再处理。
                    log.warn("过期处理失败, earnFlowNo={}, userCode={}, points={}, 原因: {}",
                            earn.getFlowNo(), earn.getUserCode(), earn.getAvailablePoints(), e.getMessage());
                    skippedCount++;
                }
            }

            // 不满一页表示已到末页
            if (expiredList.size() < PAGE_LIMIT) {
                log.info("扫描过期流水结束, 已到末页, offset={}", offset);
                break;
            }
            offset += PAGE_LIMIT;
        }

        log.info("积分过期定时任务完成, 处理成功={}, 跳过={}", processedCount, skippedCount);
    }

}
