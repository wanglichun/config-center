-- 灰度发布相关表结构

-- 灰度发布计划表
CREATE TABLE `gray_release_plan` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '灰度发布计划ID',
  `plan_name` varchar(100) NOT NULL COMMENT '发布计划名称',
  `app_name` varchar(100) NOT NULL COMMENT '应用名称',
  `environment` varchar(50) NOT NULL COMMENT '环境',
  `group_name` varchar(100) NOT NULL COMMENT '配置组',
  `config_keys` text NOT NULL COMMENT '配置键列表(JSON格式)',
  `gray_strategy` varchar(50) NOT NULL DEFAULT 'IP_WHITELIST' COMMENT '灰度策略(IP_WHITELIST/USER_WHITELIST/PERCENTAGE/CANARY)',
  `gray_rules` text NOT NULL COMMENT '灰度规则(JSON格式)',
  `rollout_percentage` int DEFAULT '0' COMMENT '发布百分比(0-100)',
  `status` varchar(20) NOT NULL DEFAULT 'DRAFT' COMMENT '状态(DRAFT/ACTIVE/PAUSED/COMPLETED/FAILED/ROLLBACK)',
  `start_time` bigint DEFAULT NULL COMMENT '开始时间',
  `end_time` bigint DEFAULT NULL COMMENT '结束时间',
  `auto_complete` tinyint NOT NULL DEFAULT '0' COMMENT '是否自动完成(0否 1是)',
  `auto_rollback` tinyint NOT NULL DEFAULT '0' COMMENT '是否自动回滚(0否 1是)',
  `rollback_condition` text DEFAULT NULL COMMENT '回滚条件(JSON格式)',
  `description` varchar(500) DEFAULT NULL COMMENT '发布描述',
  `creator` varchar(50) NOT NULL COMMENT '创建者',
  `approver` varchar(50) DEFAULT NULL COMMENT '审批者',
  `approval_time` bigint DEFAULT NULL COMMENT '审批时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建者',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新者',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志(0正常 1删除)',
  PRIMARY KEY (`id`),
  KEY `idx_app_env` (`app_name`,`environment`),
  KEY `idx_status` (`status`),
  KEY `idx_creator` (`creator`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='灰度发布计划表';

-- 灰度发布详情表
CREATE TABLE `gray_release_detail` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '灰度发布详情ID',
  `plan_id` bigint NOT NULL COMMENT '发布计划ID',
  `config_id` bigint NOT NULL COMMENT '配置项ID',
  `config_key` varchar(200) NOT NULL COMMENT '配置键',
  `old_value` text COMMENT '旧值',
  `new_value` text COMMENT '新值',
  `gray_value` text COMMENT '灰度值',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态(PENDING/GRAY/PUBLISHED/ROLLBACK)',
  `gray_start_time` bigint DEFAULT NULL COMMENT '灰度开始时间',
  `publish_time` bigint DEFAULT NULL COMMENT '全量发布时间',
  `rollback_time` bigint DEFAULT NULL COMMENT '回滚时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建者',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新者',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志(0正常 1删除)',
  PRIMARY KEY (`id`),
  KEY `idx_plan_id` (`plan_id`),
  KEY `idx_config_id` (`config_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='灰度发布详情表';

-- 灰度发布日志表
CREATE TABLE `gray_release_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `plan_id` bigint NOT NULL COMMENT '发布计划ID',
  `detail_id` bigint DEFAULT NULL COMMENT '发布详情ID',
  `operation_type` varchar(50) NOT NULL COMMENT '操作类型(CREATE/START/PAUSE/RESUME/COMPLETE/ROLLBACK/CANCEL)',
  `operation_desc` varchar(500) DEFAULT NULL COMMENT '操作描述',
  `operator` varchar(50) NOT NULL COMMENT '操作者',
  `operate_time` bigint NOT NULL COMMENT '操作时间',
  `client_ip` varchar(50) DEFAULT NULL COMMENT '客户端IP',
  `success` tinyint NOT NULL DEFAULT '1' COMMENT '是否成功(0失败 1成功)',
  `error_msg` text DEFAULT NULL COMMENT '错误信息',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建者',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新者',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志(0正常 1删除)',
  PRIMARY KEY (`id`),
  KEY `idx_plan_id` (`plan_id`),
  KEY `idx_operation_type` (`operation_type`),
  KEY `idx_operator` (`operator`),
  KEY `idx_operate_time` (`operate_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='灰度发布日志表';

-- 灰度发布监控表
CREATE TABLE `gray_release_monitor` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '监控ID',
  `plan_id` bigint NOT NULL COMMENT '发布计划ID',
  `metric_name` varchar(100) NOT NULL COMMENT '指标名称',
  `metric_value` decimal(10,4) NOT NULL COMMENT '指标值',
  `threshold_value` decimal(10,4) DEFAULT NULL COMMENT '阈值',
  `status` varchar(20) NOT NULL DEFAULT 'NORMAL' COMMENT '状态(NORMAL/WARNING/CRITICAL)',
  `collect_time` bigint NOT NULL COMMENT '采集时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建者',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新者',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志(0正常 1删除)',
  PRIMARY KEY (`id`),
  KEY `idx_plan_id` (`plan_id`),
  KEY `idx_metric_name` (`metric_name`),
  KEY `idx_collect_time` (`collect_time`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='灰度发布监控表';

-- 灰度发布实例表 (记录哪些实例获取了灰度配置)
CREATE TABLE `gray_release_instance` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `plan_id` bigint NOT NULL COMMENT '发布计划ID',
  `instance_id` varchar(100) NOT NULL COMMENT '实例ID',
  `instance_ip` varchar(50) NOT NULL COMMENT '实例IP',
  `instance_port` int DEFAULT NULL COMMENT '实例端口',
  `app_name` varchar(100) NOT NULL COMMENT '应用名称',
  `environment` varchar(50) NOT NULL COMMENT '环境',
  `is_gray` tinyint NOT NULL DEFAULT '0' COMMENT '是否灰度实例(0否 1是)',
  `first_access_time` bigint DEFAULT NULL COMMENT '首次访问时间',
  `last_access_time` bigint DEFAULT NULL COMMENT '最后访问时间',
  `access_count` bigint NOT NULL DEFAULT '0' COMMENT '访问次数',
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态(ACTIVE/INACTIVE)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建者',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新者',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志(0正常 1删除)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_plan_instance` (`plan_id`,`instance_id`,`del_flag`),
  KEY `idx_instance_ip` (`instance_ip`),
  KEY `idx_app_env` (`app_name`,`environment`),
  KEY `idx_is_gray` (`is_gray`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='灰度发布实例表';

COMMIT; 