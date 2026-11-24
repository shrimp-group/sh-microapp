package com.wkclz.micro.dag.helper;

/**
 * 键规则辅助：instanceCode:nodeCode 组合键（本机任务登记与中断广播 payload 共用格式）
 */
public final class DagKeys {

    private DagKeys() {
    }

    /** 实例+节点组合键：instanceCode:nodeCode */
    public static String instanceNodeKey(String instanceCode, String nodeCode) {
        return instanceCode + ":" + nodeCode;
    }
}
