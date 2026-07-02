package com.seatbooking.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.seatbooking.entity.User;

public interface UserService extends IService<User> {

    User login(String username, String password);

    User register(String username, String password, String realName, String studentId, String phone, String email, String role);

    User getUserByUsername(String username);

    boolean updatePassword(Long userId, String oldPassword, String newPassword);

    Integer getUserScore(Long userId);

    boolean updateUserScore(Long userId, Integer score);
}