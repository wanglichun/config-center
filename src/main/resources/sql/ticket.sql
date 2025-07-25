-- 工单表
CREATE TABLE IF NOT EXISTS `ticket` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '工单ID',
  `data_id` bigint(20) NOT NULL COMMENT '关联数据ID',
  `title` varchar(255) NOT NULL COMMENT '工单标题',
  `phase` varchar(50) NOT NULL COMMENT '工单阶段',
  `applicator` varchar(100) NOT NULL COMMENT '申请人',
  `operator` varchar(100) DEFAULT NULL COMMENT '操作人',
  `create_time` bigint(20) NOT NULL COMMENT '创建时间戳',
  `update_time` bigint(20) NOT NULL COMMENT '更新时间戳',
  `old_data` text COMMENT '旧数据(JSON格式)',
  `new_data` text COMMENT '新数据(JSON格式)',
  PRIMARY KEY (`id`),
  KEY `idx_data_id` (`data_id`),
  KEY `idx_applicator` (`applicator`),
  KEY `idx_operator` (`operator`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_phase` (`phase`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单表';

-- 插入示例数据
INSERT INTO `ticket` (`data_id`, `title`, `phase`, `applicator`, `operator`, `create_time`, `update_time`, `old_data`, `new_data`) VALUES
(1, '配置项修改申请', 'PENDING', 'user1', NULL, UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000, '{"key":"old_value"}', '{"key":"new_value"}'),
(2, '新增配置项申请', 'APPROVED', 'user2', 'admin', UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000, NULL, '{"key":"new_config","value":"test"}'),
(3, '删除配置项申请', 'REJECTED', 'user3', 'admin', UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000, '{"key":"to_delete"}', NULL); 