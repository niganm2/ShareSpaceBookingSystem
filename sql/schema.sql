CREATE DATABASE IF NOT EXISTS seat_booking DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE seat_booking;

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（加密存储）',
    real_name VARCHAR(50) COMMENT '真实姓名',
    student_id VARCHAR(20) COMMENT '学号',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    role VARCHAR(20) NOT NULL DEFAULT 'STUDENT' COMMENT '角色：STUDENT-学生，TEACHER-教师，ADMIN-管理员',
    score INT DEFAULT 100 COMMENT '信用分，初始100分，低于80分本月无法预约',
    managed_space_id BIGINT COMMENT '管辖的空间ID（教师专属）',
    is_verified INT DEFAULT 0 COMMENT '教师认证状态：0-未认证，1-已认证',
    deleted INT DEFAULT 0 COMMENT '逻辑删除：0-正常，1-已删除',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_role (role),
    INDEX idx_managed_space_id (managed_space_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE IF NOT EXISTS space (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '空间ID',
    name VARCHAR(100) NOT NULL COMMENT '空间名称',
    location VARCHAR(200) COMMENT '位置',
    capacity INT DEFAULT 0 COMMENT '容纳人数',
    description TEXT COMMENT '描述',
    status INT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    disabled_from DATETIME DEFAULT NULL COMMENT '禁用开始时间',
    disabled_until DATETIME DEFAULT NULL COMMENT '禁用结束时间',
    disabled_reason VARCHAR(500) DEFAULT NULL COMMENT '禁用原因',
    deleted INT DEFAULT 0 COMMENT '逻辑删除：0-正常，1-已删除',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间',
    INDEX idx_name (name),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='共享空间表';

CREATE TABLE IF NOT EXISTS seat (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '座位ID',
    space_id BIGINT NOT NULL COMMENT '所属空间ID',
    seat_number VARCHAR(20) NOT NULL COMMENT '座位号',
    type VARCHAR(20) DEFAULT 'NORMAL' COMMENT '座位类型：NORMAL-普通，VIP-VIP座位',
    status INT DEFAULT 1 COMMENT '状态：0-禁用，1-可用',
    deleted INT DEFAULT 0 COMMENT '逻辑删除：0-正常，1-已删除',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间',
    INDEX idx_space_id (space_id),
    INDEX idx_seat_number (seat_number),
    INDEX idx_status (status),
    UNIQUE KEY uk_space_seat (space_id, seat_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='座位表';

CREATE TABLE IF NOT EXISTS booking (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '预约ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    seat_id BIGINT NOT NULL COMMENT '座位ID',
    space_id BIGINT NOT NULL COMMENT '空间ID',
    booking_date DATE NOT NULL COMMENT '预约日期',
    start_time TIME NOT NULL COMMENT '开始时间',
    end_time TIME NOT NULL COMMENT '结束时间',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING-待签到，CHECKED_IN-已签到，COMPLETED-已完成，CANCELLED-已取消，EXPIRED-已过期',
    check_in_time DATETIME COMMENT '签到时间',
    check_out_time DATETIME COMMENT '签退时间',
    cancel_time DATETIME COMMENT '取消时间',
    cancel_reason VARCHAR(500) COMMENT '取消原因',
    deleted INT DEFAULT 0 COMMENT '逻辑删除：0-正常，1-已删除',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_seat_id (seat_id),
    INDEX idx_space_id (space_id),
    INDEX idx_booking_date (booking_date),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约表';

CREATE TABLE IF NOT EXISTS violation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '违规记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    booking_id BIGINT COMMENT '关联预约ID',
    type VARCHAR(50) NOT NULL COMMENT '违规类型',
    description TEXT COMMENT '违规描述',
    points INT NOT NULL DEFAULT 0 COMMENT '扣分数',
    deleted INT DEFAULT 0 COMMENT '逻辑删除：0-正常，1-已删除',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_booking_id (booking_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='违规记录表';

CREATE TABLE IF NOT EXISTS sys_operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    user_id BIGINT COMMENT '用户ID',
    username VARCHAR(50) COMMENT '用户名',
    operation VARCHAR(100) NOT NULL COMMENT '操作描述',
    method VARCHAR(100) COMMENT '请求方法',
    params TEXT COMMENT '请求参数',
    cost_time BIGINT COMMENT '耗时(毫秒)',
    ip VARCHAR(50) COMMENT 'IP地址',
    status INT DEFAULT 1 COMMENT '状态：0-失败，1-成功',
    error_msg TEXT COMMENT '错误信息',
    deleted INT DEFAULT 0 COMMENT '逻辑删除：0-正常，1-已删除',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_operation (operation),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

CREATE TABLE IF NOT EXISTS teacher_verification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '认证ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    phone VARCHAR(20) NOT NULL COMMENT '手机号',
    verification_code VARCHAR(10) NOT NULL COMMENT '认证码',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING-待审核，APPROVED-已通过，REJECTED-已拒绝',
    feedback TEXT COMMENT '审核反馈',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师认证表';

INSERT INTO sys_user (username, password, real_name, role, is_verified, deleted, create_time, update_time) 
VALUES ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', '管理员', 'ADMIN', 1, 0, NOW(), NOW());