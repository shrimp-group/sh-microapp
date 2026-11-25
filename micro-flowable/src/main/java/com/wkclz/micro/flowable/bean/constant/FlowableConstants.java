package com.wkclz.micro.flowable.bean.constant;

/**
 * micro-flowable 模块常量
 */
public final class FlowableConstants {

    private FlowableConstants() {
    }

    // ========== 业务编码前缀（redisIdGenerator 生成） ==========

    /** 申请单编码前缀 */
    public static final String APPLY_CODE_PREFIX = "apply_";

    /** 流程设计编码前缀 */
    public static final String DESIGN_CODE_PREFIX = "flow_";

    /** 流程分类编码前缀 */
    public static final String CATEGORY_CODE_PREFIX = "flow_cat_";

    // ========== 审批人 ==========

    /** 空审批人占位符 */
    public static final String EMPTY_ASSIGNEE_PLACEHOLDER = "-";

    /** 审批人类型：指定用户 */
    public static final String ASSIGNEE_TYPE_USER = "USER";

    /** 审批人类型：角色 */
    public static final String ASSIGNEE_TYPE_ROLE = "ROLE";

    /** 审批人类型：部门 */
    public static final String ASSIGNEE_TYPE_DEPT = "DEPT";

    /** 审批人类型：流程发起人 */
    public static final String ASSIGNEE_TYPE_STARTER = "STARTER";
}
