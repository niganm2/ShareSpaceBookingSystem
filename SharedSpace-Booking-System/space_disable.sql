-- 为space表添加禁用相关字段
-- 用于管理员禁用共享空间功能

USE seat_booking;

-- 添加禁用开始时间
ALTER TABLE `space` ADD COLUMN `disabled_from` DATETIME DEFAULT NULL COMMENT '禁用开始时间' AFTER `status`;

-- 添加禁用结束时间
ALTER TABLE `space` ADD COLUMN `disabled_until` DATETIME DEFAULT NULL COMMENT '禁用结束时间' AFTER `disabled_from`;

-- 添加禁用原因
ALTER TABLE `space` ADD COLUMN `disabled_reason` VARCHAR(500) DEFAULT NULL COMMENT '禁用原因' AFTER `disabled_until`;
