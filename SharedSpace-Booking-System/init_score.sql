-- 学生信用分系统数据库迁移
-- 执行此脚本前请确保已备份数据库

USE seat_booking;

-- 1. 检查并添加score字段（如果不存在）
SET @dbname = DATABASE();
SET @tablename = 'sys_user';
SET @columnname = 'score';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = @dbname 
    AND TABLE_NAME = @tablename 
    AND COLUMN_NAME = @columnname
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE `', @tablename, '` ADD COLUMN `', @columnname, '` INT DEFAULT 100 COMMENT ''信用分'' AFTER `status`')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 2. 初始化所有用户的分数为100（如果score为NULL）
UPDATE `sys_user` SET `score` = 100 WHERE `score` IS NULL;

-- 3. 验证更新
SELECT id, username, real_name, role, score FROM `sys_user`;
