-- 移除环境相关字段的数据库迁移脚本

-- 1. 移除config_item表中的app_name和environment字段
ALTER TABLE `config_item` DROP COLUMN `app_name`;
ALTER TABLE `config_item` DROP COLUMN `environment`;

-- 2. 更新config_item表的唯一索引
DROP INDEX `uk_config` ON `config_item`;
CREATE UNIQUE KEY `uk_config` (`group_name`,`config_key`,`del_flag`);

-- 3. 移除config_item表中的app_env索引
DROP INDEX `idx_app_env` ON `config_item`;

-- 4. 移除config_history表中的app_name和environment字段
ALTER TABLE `config_history` DROP COLUMN `app_name`;
ALTER TABLE `config_history` DROP COLUMN `environment`;

-- 5. 移除config_history表中的app_env索引
DROP INDEX `idx_app_env` ON `config_history`;

-- 6. 删除environment_info表（如果不再需要）
DROP TABLE IF EXISTS `environment_info`;

-- 7. 删除app_info表（如果不再需要）
DROP TABLE IF EXISTS `app_info`; 