package com.wkclz.micro.dag.helper;

import com.wkclz.micro.dag.bean.constant.DagConstants;
import com.wkclz.micro.dag.bean.entity.DagNodeInstance;
import com.wkclz.micro.dag.config.DagProperties;

/**
 * 租约辅助：租约时长规则（DagScheduler CAS 领取与 DagWorkerExecutor 续期共用，保证同一规则）
 */
public final class DagLeaseHelper {

    private DagLeaseHelper() {
    }

    /**
     * 租约时长：无超时节点用 fixedMs；有超时节点取 max(fixedMs, timeout + 余量)，
     * 保证租约恒覆盖超时窗口（超时由 worker Future.get 强制），避免超时瞬间与故障转移重派撞车
     */
    public static long leaseDuration(DagProperties properties, DagNodeInstance node) {
        long fixedMs = properties.getLease().getFixedMs();
        // timeout_ms<=0 与 null 同义：不限时，租约用兜底时长
        if (node.getTimeoutMs() == null || node.getTimeoutMs() <= 0) {
            return fixedMs;
        }
        return Math.max(fixedMs, node.getTimeoutMs() + DagConstants.LEASE_MARGIN_MS);
    }
}
