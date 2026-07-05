package com.wkclz.micro.points.job;

import com.wkclz.micro.points.bean.req.PointsConsumeReq;
import com.wkclz.micro.points.config.PointConfig;
import com.wkclz.micro.points.job.impl.PointsExpireImpl;
import com.wkclz.micro.points.service.PointsConsumeService;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 积分过期定时任务（spec §5.6, §13.7 US-EX-001/002）
 * <p>
 * 调度方式（弱依赖）：
 * - 主应用引入 sh-xxljob 时：{@link XxlJobHandler} 激活，由 XxlJob 调度触发 handler=pointsExpireHandler
 * - 主应用未引入 sh-xxljob 时：{@link ScheduleHandler} 激活，由 Spring @Scheduled 兜底，cron 默认每天 02:00
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
public class PointsExpireJob {



    @Component
    @ConditionalOnClass(name = "com.xxl.job.core.handler.annotation.XxlJob")
    @ConditionalOnProperty(name = "micro.points.expire.useXxlJob", havingValue = "true", matchIfMissing = true)
    public static class XxlJobHandler {

        @Autowired
        private PointConfig pointConfig;
        @Autowired
        private PointsExpireImpl pointsExpireImpl;

        @XxlJob("pointsExpireHandler")
        public void execute() {
            if (pointConfig.getPointsExpireEnabled() != 1) {
                return;
            }
            pointsExpireImpl.doExecute();
        }
    }

    @Component
    @ConditionalOnMissingClass("com.xxl.job.core.handler.annotation.XxlJob")
    public static class ScheduleHandler {

        @Autowired
        private PointConfig pointConfig;
        @Autowired
        private PointsExpireImpl pointsExpireImpl;

        @Scheduled(cron = "12 12 0 * * ?")
        public void execute() {
            if (pointConfig.getPointsExpireEnabled() != 1) {
                return;
            }
            pointsExpireImpl.doExecute();
        }
    }
}
