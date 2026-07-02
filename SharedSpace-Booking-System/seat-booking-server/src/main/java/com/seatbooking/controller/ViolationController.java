package com.seatbooking.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.seatbooking.common.Result;
import com.seatbooking.dto.ViolationDetailDTO;
import com.seatbooking.entity.Booking;
import com.seatbooking.entity.User;
import com.seatbooking.entity.Violation;
import com.seatbooking.mapper.ViolationMapper;
import com.seatbooking.service.BookingService;
import com.seatbooking.service.UserService;
import com.seatbooking.service.ViolationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/violation")
public class ViolationController {

    @Autowired
    private ViolationService violationService;
    @Autowired
    private UserService userService;
    @Autowired
    private BookingService bookingService;
    @Autowired
    private ViolationMapper violationMapper;

    @GetMapping("/list")
    public Result<List<Violation>> list(@RequestAttribute Long userId) {
        return Result.success(violationService.list(new LambdaQueryWrapper<Violation>().eq(Violation::getUserId, userId).eq(Violation::getDeleted, 0).orderByDesc(Violation::getCreateTime)));
    }

    @GetMapping("/my-count")
    public Result<Map<String, Object>> getMyViolationCount(@RequestAttribute Long userId) {
        User user = userService.getById(userId);
        Long count = violationService.count(new LambdaQueryWrapper<Violation>().eq(Violation::getUserId, userId).eq(Violation::getDeleted, 0));
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        result.put("score", user.getScore() != null ? user.getScore() : 100);
        return Result.success(result);
    }

    @GetMapping("/teacher-count")
    public Result<Long> getTeacherViolationCount(@RequestAttribute Long userId) {
        User teacher = userService.getById(userId);
        if (!"TEACHER".equals(teacher.getRole()) || teacher.getManagedSpaceId() == null) return Result.error("您无权限操作");
        List<Long> bookingIds = bookingService.list(new LambdaQueryWrapper<Booking>().eq(Booking::getSpaceId, teacher.getManagedSpaceId()).eq(Booking::getDeleted, 0).select(Booking::getId)).stream().map(Booking::getId).collect(Collectors.toList());
        if (bookingIds.isEmpty()) return Result.success(0L);
        return Result.success(violationService.count(new LambdaQueryWrapper<Violation>().in(Violation::getBookingId, bookingIds).eq(Violation::getDeleted, 0)));
    }

    @GetMapping("/admin-count")
    public Result<Long> getAdminViolationCount(@RequestAttribute Long userId) {
        User admin = userService.getById(userId);
        if (!"ADMIN".equals(admin.getRole())) return Result.error("无权限");
        return Result.success(violationService.count(new LambdaQueryWrapper<Violation>().eq(Violation::getDeleted, 0)));
    }

    @GetMapping("/my-score")
    public Result<Map<String, Object>> getMyScore(@RequestAttribute Long userId) {
        Integer score = userService.getUserScore(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("score", score);
        result.put("canBook", score >= 80);
        return Result.success(result);
    }

    @GetMapping("/admin/all-details")
    public Result<List<ViolationDetailDTO>> getAllViolationDetails(@RequestAttribute Long userId) {
        User admin = userService.getById(userId);
        if (!"ADMIN".equals(admin.getRole())) return Result.error("无权限");
        return Result.success(violationMapper.selectAllViolationDetails());
    }
}