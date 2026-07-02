package com.seatbooking.controller;

import com.seatbooking.common.Result;
import com.seatbooking.entity.TeacherVerification;
import com.seatbooking.entity.User;
import com.seatbooking.service.TeacherVerificationService;
import com.seatbooking.service.UserService;
import com.seatbooking.context.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/teacher/verification")
public class TeacherVerificationController {

    @Autowired
    private TeacherVerificationService teacherVerificationService;
    @Autowired
    private UserService userService;

    @PostMapping("/submit")
    public Result<?> submitVerification(@RequestBody TeacherVerification verification) {
        try {
            User user = userService.getById(verification.getUserId());
            if (user == null) return Result.error("用户不存在");
            if (!"TEACHER".equals(user.getRole())) return Result.error("只有教师用户才能提交认证");
            if (user.getIsVerified() != null && user.getIsVerified() == 1) return Result.error("您已通过认证");
            TeacherVerification existing = teacherVerificationService.getVerificationByUserId(verification.getUserId());
            if (existing != null && ("PENDING".equals(existing.getReviewStatus()) || "APPROVED".equals(existing.getReviewStatus())))
                return Result.error("已提交认证申请");
            if (!verification.getRealName().equals(user.getRealName()) || !verification.getPhone().equals(user.getPhone()))
                return Result.error("信息不匹配");
            return Result.success("认证提交成功", teacherVerificationService.submitVerification(verification.getUserId(), verification.getRealName(), verification.getPhone(), verification.getVerificationCode()));
        } catch (Exception e) {
            return Result.error("认证提交失败：" + e.getMessage());
        }
    }

    @GetMapping("/status")
    public Result<?> getVerificationStatus() {
        try {
            Long userId = UserContext.getCurrentUserId();
            if (userId == null) return Result.error("用户未登录");
            User user = userService.getById(userId);
            if (user == null) return Result.error("用户不存在");
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("isVerified", user.getIsVerified() != null && user.getIsVerified() == 1);
            result.put("realName", user.getRealName());
            result.put("phone", user.getPhone());
            TeacherVerification verification = teacherVerificationService.getVerificationByUserId(userId);
            result.put("hasSubmitted", verification != null);
            result.put("reviewStatus", verification != null ? verification.getReviewStatus() : null);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取认证状态失败：" + e.getMessage());
        }
    }

    @GetMapping("/feedback")
    public Result<List<TeacherVerification>> getTeacherFeedback() {
        try {
            Long userId = UserContext.getCurrentUserId();
            String role = UserContext.getCurrentRole();
            if (userId == null || !"TEACHER".equals(role)) return Result.error("权限不足");
            TeacherVerification verification = teacherVerificationService.getVerificationByUserId(userId);
            return Result.success(verification != null ? java.util.Collections.singletonList(verification) : new java.util.ArrayList<>());
        } catch (Exception e) {
            return Result.error("获取认证反馈失败：" + e.getMessage());
        }
    }

    @GetMapping("/list")
    public Result<List<TeacherVerification>> getAllVerifications() {
        try {
            return Result.success(teacherVerificationService.getAllVerifications());
        } catch (Exception e) {
            return Result.error("获取认证列表失败：" + e.getMessage());
        }
    }

    @PostMapping("/review")
    public Result<?> reviewVerification(@RequestBody Map<String, Object> params) {
        try {
            Long verificationId = Long.parseLong(params.get("verificationId").toString());
            String reviewStatus = params.get("reviewStatus").toString();
            String rejectReason = params.get("rejectReason") != null ? params.get("rejectReason").toString() : null;
            TeacherVerification verification = teacherVerificationService.getById(verificationId);
            if (verification == null) return Result.error("认证记录不存在");
            verification.setReviewStatus(reviewStatus);
            verification.setRejectReason("REJECTED".equals(reviewStatus) ? rejectReason : null);
            teacherVerificationService.updateById(verification);
            if ("APPROVED".equals(reviewStatus)) {
                User user = userService.getById(verification.getUserId());
                if (user != null) {
                    user.setIsVerified(1);
                    userService.updateById(user);
                }
            }
            return Result.success("审核完成");
        } catch (Exception e) {
            return Result.error("审核失败：" + e.getMessage());
        }
    }
}