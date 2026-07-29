-- micro-flowable 建表脚本
-- 字符集 utf8mb4 + utf8mb4_unicode_ci

-- 1. 流程设计
CREATE TABLE IF NOT EXISTS `mdm_flowable_process_design` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `design_code` varchar(63) NOT NULL COMMENT '设计编码',
    `design_name` varchar(127) NOT NULL COMMENT '流程名称',
    `category` varchar(63) DEFAULT NULL COMMENT '流程分类',
    `xml_content` longtext NOT NULL COMMENT 'BPMN XML 内容',
    `form_key` varchar(127) DEFAULT NULL COMMENT '关联表单 key',
    `design_version` int NOT NULL DEFAULT 1 COMMENT '设计版本',
    `status` varchar(31) NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT/DEPLOYED/DISABLED',
    `deploy_id` varchar(63) DEFAULT NULL COMMENT '最近部署 ID',
    `proc_def_id` varchar(63) DEFAULT NULL COMMENT '最近流程定义 ID',
    `tenant_code` varchar(31) DEFAULT NULL COMMENT '租户编码',
    `sort` int DEFAULT 0 COMMENT '排序',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` varchar(31) DEFAULT NULL COMMENT '创建人',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `update_by` varchar(31) DEFAULT NULL COMMENT '修改人',
    `remark` varchar(255) DEFAULT NULL COMMENT '备注',
    `version` int DEFAULT 1 COMMENT '乐观锁',
    `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除 0=未删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_design_code_tenant` (`design_code`, `tenant_code`, `deleted`),
    KEY `idx_category` (`category`, `deleted`),
    KEY `idx_status` (`status`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流程设计';

-- 2. 节点配置
CREATE TABLE IF NOT EXISTS `mdm_flowable_node_config` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `design_id` bigint NOT NULL COMMENT '关联 process_design.id',
    `node_key` varchar(63) NOT NULL COMMENT '节点 ID（BPMN taskDefKey）',
    `node_name` varchar(127) NOT NULL COMMENT '节点名称',
    `node_type` varchar(31) NOT NULL COMMENT '节点类型：START/APPROVAL/CC/GATEWAY/END',
    `assignee_type` varchar(31) DEFAULT NULL COMMENT '审批人类型：USER/ROLE/DEPT/STARTER/SCRIPT',
    `assignee_value` varchar(511) DEFAULT NULL COMMENT '审批人配置值',
    `form_fields` text DEFAULT NULL COMMENT '表单字段权限 JSON',
    `order_num` int DEFAULT NULL COMMENT '节点顺序',
    `tenant_code` varchar(31) DEFAULT NULL COMMENT '租户编码',
    `sort` int DEFAULT 0 COMMENT '排序',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` varchar(31) DEFAULT NULL COMMENT '创建人',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `update_by` varchar(31) DEFAULT NULL COMMENT '修改人',
    `remark` varchar(255) DEFAULT NULL COMMENT '备注',
    `version` int DEFAULT 1 COMMENT '乐观锁',
    `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除 0=未删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_design_node` (`design_id`, `node_key`, `deleted`),
    KEY `idx_design_id` (`design_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='节点配置';

-- 3. 流程申请单
CREATE TABLE IF NOT EXISTS `mdm_flowable_apply` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `apply_code` varchar(63) NOT NULL COMMENT '申请单号',
    `design_code` varchar(63) NOT NULL COMMENT '关联流程设计编码',
    `proc_ins_id` varchar(63) DEFAULT NULL COMMENT 'flowable 流程实例 ID',
    `proc_def_id` varchar(63) DEFAULT NULL COMMENT '流程定义 ID',
    `business_type` varchar(63) DEFAULT NULL COMMENT '业务类型',
    `business_summary` varchar(511) DEFAULT NULL COMMENT '申请内容摘要',
    `business_data` text DEFAULT NULL COMMENT '业务表单数据 JSON',
    `start_user_id` varchar(31) NOT NULL COMMENT '发起人用户 ID',
    `status` varchar(31) NOT NULL DEFAULT 'RUNNING' COMMENT '状态：RUNNING/APPROVED/REJECTED/TERMINATED/WITHDRAWN',
    `tenant_code` varchar(31) DEFAULT NULL COMMENT '租户编码',
    `sort` int DEFAULT 0 COMMENT '排序',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` varchar(31) DEFAULT NULL COMMENT '创建人',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `update_by` varchar(31) DEFAULT NULL COMMENT '修改人',
    `remark` varchar(255) DEFAULT NULL COMMENT '备注',
    `version` int DEFAULT 1 COMMENT '乐观锁',
    `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除 0=未删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_apply_code_tenant` (`apply_code`, `tenant_code`, `deleted`),
    KEY `idx_proc_ins_id` (`proc_ins_id`),
    KEY `idx_start_user` (`start_user_id`, `deleted`),
    KEY `idx_status` (`status`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流程申请单';

-- 4. 审批意见
CREATE TABLE IF NOT EXISTS `mdm_flowable_approval` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `apply_id` bigint NOT NULL COMMENT '关联 apply.id',
    `proc_ins_id` varchar(63) NOT NULL COMMENT '流程实例 ID',
    `task_id` varchar(63) DEFAULT NULL COMMENT 'flowable 任务 ID',
    `node_key` varchar(63) DEFAULT NULL COMMENT '节点 key',
    `node_name` varchar(127) DEFAULT NULL COMMENT '节点名称',
    `approver_id` varchar(31) NOT NULL COMMENT '审批人用户 ID',
    `action` varchar(31) NOT NULL COMMENT '审批动作：APPROVE/REJECT/TRANSFER/DELEGATE/WITHDRAW/CLAIM',
    `comment` varchar(1023) DEFAULT NULL COMMENT '审批意见',
    `target_user_id` varchar(31) DEFAULT NULL COMMENT '目标用户',
    `tenant_code` varchar(31) DEFAULT NULL COMMENT '租户编码',
    `sort` int DEFAULT 0 COMMENT '排序',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` varchar(31) DEFAULT NULL COMMENT '创建人',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `update_by` varchar(31) DEFAULT NULL COMMENT '修改人',
    `remark` varchar(255) DEFAULT NULL COMMENT '备注',
    `version` int DEFAULT 1 COMMENT '乐观锁',
    `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除 0=未删除',
    PRIMARY KEY (`id`),
    KEY `idx_apply_id` (`apply_id`, `deleted`),
    KEY `idx_proc_ins_id` (`proc_ins_id`, `deleted`),
    KEY `idx_approver` (`approver_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审批意见';

-- 5. 异常日志
CREATE TABLE IF NOT EXISTS `mdm_flowable_error_log` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `error_type` varchar(63) NOT NULL COMMENT '异常类型：DEPLOY_ERROR/START_ERROR/APPROVE_ERROR/QUERY_ERROR/CALLBACK_ERROR',
    `proc_ins_id` varchar(63) DEFAULT NULL COMMENT '关联流程实例',
    `task_id` varchar(63) DEFAULT NULL COMMENT '关联任务',
    `apply_id` bigint DEFAULT NULL COMMENT '关联申请单',
    `client_method` varchar(127) DEFAULT NULL COMMENT 'client 调用方法',
    `request_data` text DEFAULT NULL COMMENT '请求参数 JSON',
    `error_message` varchar(1023) NOT NULL COMMENT '异常消息',
    `error_stack` text DEFAULT NULL COMMENT '异常堆栈',
    `occur_time` datetime NOT NULL COMMENT '发生时间',
    `handle_status` varchar(31) DEFAULT 'PENDING' COMMENT '处理状态：PENDING/RESOLVED/IGNORED',
    `tenant_code` varchar(31) DEFAULT NULL COMMENT '租户编码',
    `sort` int DEFAULT 0 COMMENT '排序',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` varchar(31) DEFAULT NULL COMMENT '创建人',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `update_by` varchar(31) DEFAULT NULL COMMENT '修改人',
    `remark` varchar(255) DEFAULT NULL COMMENT '备注',
    `version` int DEFAULT 1 COMMENT '乐观锁',
    `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除 0=未删除',
    PRIMARY KEY (`id`),
    KEY `idx_error_type` (`error_type`, `deleted`),
    KEY `idx_proc_ins_id` (`proc_ins_id`, `deleted`),
    KEY `idx_occur_time` (`occur_time`),
    KEY `idx_handle_status` (`handle_status`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='异常日志';
