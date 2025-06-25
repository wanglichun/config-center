-- 配置中心数据库初始化脚本
-- 创建数据库
CREATE DATABASE IF NOT EXISTS config_center DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE config_center;

-- 用户表
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `department` varchar(100) DEFAULT NULL COMMENT '部门',
  `role` varchar(20) NOT NULL DEFAULT 'VIEWER' COMMENT '角色(ADMIN/DEVELOPER/VIEWER)',
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态(ACTIVE/INACTIVE/LOCKED)',
  `last_login_time` bigint DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) DEFAULT NULL COMMENT '最后登录IP',
  `avatar` varchar(500) DEFAULT NULL COMMENT '头像URL',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建者',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新者',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志(0正常 1删除)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_email` (`email`),
  KEY `idx_status` (`status`),
  KEY `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 配置项表
CREATE TABLE `config_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置项ID',
  `app_name` varchar(100) NOT NULL COMMENT '应用名称',
  `environment` varchar(50) NOT NULL COMMENT '环境(dev/test/prod)',
  `group_name` varchar(100) NOT NULL COMMENT '配置组',
  `config_key` varchar(200) NOT NULL COMMENT '配置键',
  `config_value` text COMMENT '配置值',
  `data_type` varchar(20) NOT NULL DEFAULT 'STRING' COMMENT '数据类型(STRING/NUMBER/BOOLEAN/JSON)',
  `description` varchar(500) DEFAULT NULL COMMENT '配置描述',
  `version` bigint NOT NULL DEFAULT '1' COMMENT '版本号',
  `encrypted` tinyint NOT NULL DEFAULT '0' COMMENT '是否加密(0否 1是)',
  `tags` varchar(200) DEFAULT NULL COMMENT '标签',
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态(ACTIVE/INACTIVE)',
  `zk_path` varchar(500) DEFAULT NULL COMMENT 'ZooKeeper路径',
  `last_publish_time` bigint DEFAULT NULL COMMENT '最后发布时间',
  `publisher` varchar(50) DEFAULT NULL COMMENT '发布者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建者',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新者',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志(0正常 1删除)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config` (`app_name`,`environment`,`group_name`,`config_key`,`del_flag`),
  KEY `idx_app_env` (`app_name`,`environment`),
  KEY `idx_group` (`group_name`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_version` (`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='配置项表';

-- 配置历史表
CREATE TABLE `config_history` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '历史记录ID',
  `config_id` bigint NOT NULL COMMENT '配置项ID',
  `app_name` varchar(100) NOT NULL COMMENT '应用名称',
  `environment` varchar(50) NOT NULL COMMENT '环境',
  `group_name` varchar(100) NOT NULL COMMENT '配置组',
  `config_key` varchar(200) NOT NULL COMMENT '配置键',
  `old_value` text COMMENT '旧值',
  `new_value` text COMMENT '新值',
  `version` bigint NOT NULL COMMENT '版本号',
  `operation_type` varchar(20) NOT NULL COMMENT '操作类型(CREATE/UPDATE/DELETE/PUBLISH/ROLLBACK)',
  `change_reason` varchar(500) DEFAULT NULL COMMENT '变更原因',
  `operator` varchar(50) NOT NULL COMMENT '操作者',
  `operate_time` bigint NOT NULL COMMENT '操作时间',
  `client_ip` varchar(50) DEFAULT NULL COMMENT '客户端IP',
  `rollback` tinyint NOT NULL DEFAULT '0' COMMENT '是否回滚(0否 1是)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建者',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新者',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志(0正常 1删除)',
  PRIMARY KEY (`id`),
  KEY `idx_config_id` (`config_id`),
  KEY `idx_app_env` (`app_name`,`environment`),
  KEY `idx_operator` (`operator`),
  KEY `idx_operate_time` (`operate_time`),
  KEY `idx_operation_type` (`operation_type`),
  KEY `idx_version` (`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='配置历史表';

-- 应用表
CREATE TABLE `app_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '应用ID',
  `app_name` varchar(100) NOT NULL COMMENT '应用名称',
  `app_desc` varchar(500) DEFAULT NULL COMMENT '应用描述',
  `owner` varchar(50) DEFAULT NULL COMMENT '负责人',
  `email` varchar(100) DEFAULT NULL COMMENT '负责人邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '负责人电话',
  `git_url` varchar(500) DEFAULT NULL COMMENT 'Git仓库地址',
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态(ACTIVE/INACTIVE)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建者',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新者',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志(0正常 1删除)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_name` (`app_name`,`del_flag`),
  KEY `idx_owner` (`owner`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='应用信息表';

-- 环境表
CREATE TABLE `environment_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '环境ID',
  `env_name` varchar(50) NOT NULL COMMENT '环境名称',
  `env_desc` varchar(200) DEFAULT NULL COMMENT '环境描述',
  `env_order` int NOT NULL DEFAULT '0' COMMENT '环境顺序',
  `color` varchar(20) DEFAULT NULL COMMENT '环境标识颜色',
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态(ACTIVE/INACTIVE)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建者',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新者',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志(0正常 1删除)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_env_name` (`env_name`,`del_flag`),
  KEY `idx_env_order` (`env_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='环境信息表';

-- 配置组表
CREATE TABLE `config_group` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置组ID',
  `app_name` varchar(100) NOT NULL COMMENT '应用名称',
  `group_name` varchar(100) NOT NULL COMMENT '配置组名称',
  `group_desc` varchar(200) DEFAULT NULL COMMENT '配置组描述',
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态(ACTIVE/INACTIVE)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建者',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新者',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志(0正常 1删除)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_group` (`app_name`,`group_name`,`del_flag`),
  KEY `idx_app_name` (`app_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='配置组表';

-- 插入初始数据
-- 默认管理员用户
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `email`, `role`, `status`, `create_by`, `update_by`) 
VALUES ('admin', '$2a$10$7JB720yubVSEi/k4sFNr.B2fHlYrm2XZpHLzPo/RbxLy5Hh8JZjiq', '系统管理员', 'admin@example.com', 'ADMIN', 'ACTIVE', 'system', 'system');

-- 默认环境
INSERT INTO `environment_info` (`env_name`, `env_desc`, `env_order`, `color`, `create_by`, `update_by`) VALUES
('dev', '开发环境', 1, '#409EFF', 'system', 'system'),
('test', '测试环境', 2, '#E6A23C', 'system', 'system'),
('prod', '生产环境', 3, '#F56C6C', 'system', 'system');

-- 示例应用
INSERT INTO `app_info` (`app_name`, `app_desc`, `owner`, `email`, `create_by`, `update_by`) 
VALUES ('demo-app', '示例应用', 'admin', 'admin@example.com', 'system', 'system');

-- 示例配置组
INSERT INTO `config_group` (`app_name`, `group_name`, `group_desc`, `create_by`, `update_by`) VALUES
('demo-app', 'database', '数据库配置', 'system', 'system'),
('demo-app', 'redis', 'Redis配置', 'system', 'system'),
('demo-app', 'common', '通用配置', 'system', 'system');

-- 示例配置项
INSERT INTO `config_item` (`app_name`, `environment`, `group_name`, `config_key`, `config_value`, `data_type`, `description`, `zk_path`, `create_by`, `update_by`) VALUES
('demo-app', 'dev', 'database', 'jdbc.url', 'jdbc:mysql://localhost:3306/demo_dev', 'STRING', '数据库连接URL', '/configs/demo-app/dev/database/jdbc.url', 'system', 'system'),
('demo-app', 'dev', 'database', 'jdbc.username', 'root', 'STRING', '数据库用户名', '/configs/demo-app/dev/database/jdbc.username', 'system', 'system'),
('demo-app', 'dev', 'database', 'jdbc.password', 'root123', 'STRING', '数据库密码', '/configs/demo-app/dev/database/jdbc.password', 'system', 'system'),
('demo-app', 'dev', 'redis', 'host', 'localhost', 'STRING', 'Redis主机', '/configs/demo-app/dev/redis/host', 'system', 'system'),
('demo-app', 'dev', 'redis', 'port', '6379', 'NUMBER', 'Redis端口', '/configs/demo-app/dev/redis/port', 'system', 'system'),
('demo-app', 'dev', 'common', 'app.version', '1.0.0', 'STRING', '应用版本', '/configs/demo-app/dev/common/app.version', 'system', 'system'),
('demo-app', 'dev', 'common', 'debug.enabled', 'true', 'BOOLEAN', '调试模式', '/configs/demo-app/dev/common/debug.enabled', 'system', 'system');

COMMIT; 