# 校园自习室共享空间预约管理系统

基于 Spring Boot + Vue.js 构建的校园自习室共享空间预约管理系统，为师生提供便捷的座位预约服务。

## 功能特性

- **学生端**: 座位预约、取消预约、签到签退、违规记录查看、个人中心
- **教师端**: 管辖自习室管理、预约记录查询、可用座位统计、AI智能助手
- **管理员端**: 空间管理、用户管理、违规审核、教师认证、操作日志

## 技术栈

- **后端**: Java Spring Boot 2.7.x + MyBatis-Plus + MySQL + Redis + JWT
- **前端**: Vue.js 3 + Element Plus + Axios
- **AI助手**: DeepSeek API

## 项目结构

```
├── SharedSpace-Booking-System/    # 项目后端代码
├── shared-space-frontend/         # 项目前端代码
├── sql/                           # 数据库建表语句
└── .gitignore                     # 项目配置文件
```

## 快速开始

### 环境要求

- JDK 1.8+
- Node.js 14+
- MySQL 5.7+

### 后端启动

```bash
cd SharedSpace-Booking-System
mvn spring-boot:run
```

### 前端启动

```bash
cd shared-space-frontend
npm install
npm run serve
```

### 数据库配置

1. 创建数据库 `seat_booking`
2. 执行 `sql/schema.sql` 创建表结构
3. 修改 `application-dev.yml` 配置数据库连接信息

## 默认账号

- 管理员：admin / admin123