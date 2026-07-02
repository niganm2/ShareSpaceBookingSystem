package com.seatbooking.controller;

import com.seatbooking.annotation.OperationLog;
import com.seatbooking.common.Result;
import com.seatbooking.dto.ChatRequest;
import com.seatbooking.dto.ChatResponse;
import com.seatbooking.entity.User;
import com.seatbooking.service.AiService;
import com.seatbooking.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Autowired
    private AiService aiService;

    @Autowired
    private UserService userService;

    @PostMapping("/chat")
    public Result<ChatResponse> chat(@RequestBody ChatRequest request, @RequestAttribute Long userId) {
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        if (user.getScore() < 80 && !"ADMIN".equals(user.getRole()) && !"TEACHER".equals(user.getRole())) {
            ChatResponse response = new ChatResponse();
            response.setIntent("blocked");
            response.setReply("您的信誉分低于80分，暂无法使用AI预约助手功能。信誉分将在每月1号自动恢复至100分。");
            response.setSuccess(false);
            return Result.success(response);
        }

        ChatResponse response = aiService.chat(userId, user.getRole(), request);
        return Result.success(response);
    }
}