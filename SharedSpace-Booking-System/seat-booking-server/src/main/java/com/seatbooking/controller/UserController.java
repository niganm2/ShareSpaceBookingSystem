package com.seatbooking.controller;

import com.seatbooking.annotation.OperationLog;
import com.seatbooking.common.Result;
import com.seatbooking.entity.User;
import com.seatbooking.service.UserService;
import com.seatbooking.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @OperationLog("用户登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody User loginUser) {
        try {
            User user = userService.login(loginUser.getUsername(), loginUser.getPassword());
            if (user == null) {
                return Result.error("用户名或密码错误");
            }

            String loginRole = loginUser.getRole();
            String realRole = user.getRole();

            if (!loginRole.equals(realRole)) {
                return Result.error("用户身份不匹配，请重新选择！");
            }

            String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("user", user);
            return Result.success(data);
        } catch (Exception e) {
            return Result.error("登录失败：" + e.getMessage());
        }
    }

    @OperationLog("用户注册")
    @PostMapping("/register")
    public Result<?> register(@RequestBody User registerUser) {
        try {
            User existUser = userService.getUserByUsername(registerUser.getUsername());
            if (existUser != null) {
                return Result.error("用户名已存在");
            }

            String studentIdOrJobNo = registerUser.getStudentId();

            User newUser = userService.register(
                    registerUser.getUsername(),
                    registerUser.getPassword(),
                    registerUser.getRealName(),
                    studentIdOrJobNo,
                    registerUser.getPhone(),
                    registerUser.getEmail(),
                    registerUser.getRole()
            );

            return Result.success("注册成功", newUser);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("注册失败：" + e.getMessage());
        }
    }

    @OperationLog("修改密码")
    @PostMapping("/update-password")
    public Result<?> updatePassword(@RequestBody Map<String, String> params) {
        try {
            Long userId = Long.parseLong(params.get("userId"));
            String oldPassword = params.get("oldPassword");
            String newPassword = params.get("newPassword");

            boolean result = userService.updatePassword(userId, oldPassword, newPassword);
            return Result.success("密码修改成功", result);
        } catch (Exception e) {
            return Result.error("密码修改失败：" + e.getMessage());
        }
    }
}