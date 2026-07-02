package com.seatbooking.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.seatbooking.entity.User;
import com.seatbooking.mapper.UserMapper;
import com.seatbooking.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@CacheConfig(cacheNames = "user")
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public User login(String username, String password) {
        User user = getUserByUsername(username);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            user.setPassword(null);
            return user;
        }
        return null;
    }

    @Override
    public User register(String username, String password, String realName, String studentId, String phone, String email, String role) {
        User existUser = getUserByUsername(username);
        if (existUser != null) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRealName(realName);
        user.setStudentId(studentId);
        user.setPhone(phone);
        user.setEmail(email);
        user.setRole(role);
        user.setScore(100);
        user.setDeleted(0);
        user.setCreateTime(LocalDateTime.now());

        save(user);
        user.setPassword(null);
        return user;
    }

    @Override
    public User getUserByUsername(String username) {
        return getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .eq(User::getDeleted, 0));
    }

    @Override
    public boolean updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        return updateById(user);
    }

    @Override
    @Cacheable(key = "'score_' + #userId")
    public Integer getUserScore(Long userId) {
        User user = getById(userId);
        return user != null && user.getScore() != null ? user.getScore() : 100;
    }

    @Override
    @CacheEvict(key = "'score_' + #userId")
    public boolean updateUserScore(Long userId, Integer score) {
        return update(new LambdaUpdateWrapper<User>()
                .eq(User::getId, userId)
                .set(User::getScore, score)
                .set(User::getUpdateTime, LocalDateTime.now()));
    }
}