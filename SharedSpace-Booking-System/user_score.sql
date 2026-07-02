-- 为sys_user表添加score字段
-- 用于学生信用分系统，初始100分，违规扣分

USE seat_booking;

-- 添加学生信用分字段，默认100分
ALTER TABLE `sys_user` ADD COLUMN `score` INT DEFAULT 100 COMMENT '信用分，初始100分，低于80分本月无法预约' AFTER `managed_space_id`;
