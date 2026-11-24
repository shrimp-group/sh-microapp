package com.wkclz.micro.dag.helper;

import com.wkclz.micro.dag.bean.entity.DagProcessVersion;

import java.util.List;

/**
 * 版本辅助：流程版本选择规则（DagProcessService 聚合展示与 DagInstanceService 发起共用）
 */
public final class DagVersionHelper {

    private DagVersionHelper() {
    }

    /** 指定状态中 dagVersion 最大的版本，无匹配返回 null */
    public static DagProcessVersion maxVersionOf(List<DagProcessVersion> versions, String status) {
        DagProcessVersion max = null;
        for (DagProcessVersion v : versions) {
            if (!status.equals(v.getStatus())) {
                continue;
            }
            if (max == null || v.getDagVersion() >= max.getDagVersion()) {
                max = v;
            }
        }
        return max;
    }
}
