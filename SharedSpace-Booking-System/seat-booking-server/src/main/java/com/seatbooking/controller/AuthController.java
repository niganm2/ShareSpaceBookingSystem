package com.seatbooking.controller;

import com.seatbooking.common.Result;
import com.seatbooking.dto.LoginDTO;
import com.seatbooking.entity.User;
import com.seatbooking.service.UserService;
import com.seatbooking.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO loginDTO) {
        User user = userService.login(loginDTO.getUsername(), loginDTO.getPassword());
        if (user == null) {
            return Result.error("用户名或密码错误");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", user);

        return Result.success(data);
    }

    @PostMapping("/register")
    public Result<User> register(@Valid @RequestBody User user) {
        try {
            User registeredUser = userService.register(
                    user.getUsername(),
                    user.getPassword(),
                    user.getRealName(),
                    user.getStudentId(),
                    user.getPhone(),
                    user.getEmail(),
                    user.getRole() != null ? user.getRole() : "STUDENT"
            );
            registeredUser.setPassword(null);
            return Result.success("注册成功", registeredUser);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/change-password")
    public Result<?> changePassword(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");

        User user = userService.getUserByUsername(username);
        if (user == null) {
            return Result.error("用户不存在");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return Result.error("原密码错误");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userService.updateById(user);

        return Result.success("密码修改成功");
    }
}