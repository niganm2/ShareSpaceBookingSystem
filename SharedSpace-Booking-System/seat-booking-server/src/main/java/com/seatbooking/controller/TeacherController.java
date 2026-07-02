package com.seatbooking.controller;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.seatbooking.common.Result;
import com.seatbooking.dto.BookingDTO;
import com.seatbooking.entity.Booking;
import com.seatbooking.entity.Space;
import com.seatbooking.entity.User;
import com.seatbooking.entity.Violation;
import com.seatbooking.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/teacher")
public class TeacherController {

    @Autowired
    private BookingService bookingService;
    @Autowired
    private SpaceService spaceService;
    @Autowired
    private UserService userService;
    @Autowired
    private ViolationService violationService;

    @GetMapping("/my-space")
    public Result<Space> getMyManagedSpace(@RequestAttribute Long userId) {
        User user = userService.getById(userId);
        if (!"TEACHER".equals(user.getRole()) || user.getManagedSpaceId() == null) return Result.error("您无权限管理自习室");
        return Result.success(spaceService.getById(user.getManagedSpaceId()));
    }

    @GetMapping("/today-bookings")
    public Result<List<BookingDTO>> getTodayBookings(@RequestAttribute Long userId) {
        User user = userService.getById(userId);
        if (!"TEACHER".equals(user.getRole()) || user.getManagedSpaceId() == null) return Result.error("您无权限管理自习室");
        return Result.success(bookingService.getBookingsBySpaceAndDate(user.getManagedSpaceId(), LocalDate.now()));
    }

    @GetMapping("/available-seats")
    public Result<Integer> getAvailableSeats(@RequestAttribute Long userId) {
        User user = userService.getById(userId);
        if (!"TEACHER".equals(user.getRole()) || user.getManagedSpaceId() == null) return Result.error("您无权限管理自习室");
        return Result.success(spaceService.countAvailableSeats(user.getManagedSpaceId()));
    }

    @PostMapping("/mark-violation")
    public Result<?> markViolation(@RequestAttribute Long userId, @RequestBody ViolationRequest request) {
        User teacher = userService.getById(userId);
        if (!"TEACHER".equals(teacher.getRole()) || teacher.getManagedSpaceId() == null) return Result.error("您无权限操作");
        Booking booking = bookingService.getById(request.getBookingId());
        if (booking == null) return Result.error("预约记录不存在");
        if (!booking.getSpaceId().equals(teacher.getManagedSpaceId())) return Result.error("该预约不在您管理的空间内");
        if (!"RESERVED".equals(booking.getStatus())) return Result.error("该预约状态无法标记违规");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime bookingStart = LocalDateTime.of(booking.getBookingDate(), booking.getStartTime());
        LocalDateTime bookingEnd = LocalDateTime.of(booking.getBookingDate(), booking.getEndTime());
        if (now.isBefore(bookingStart) || now.isAfter(bookingEnd)) return Result.error("当前时间不在预约时间段内");
        violationService.createViolation(booking.getUserId(), booking.getId(), request.getType() != null ? request.getType() : "NO_SHOW", request.getDescription() != null ? request.getDescription() : "老师标记违规", request.getPoints() != null ? request.getPoints() : 1);
        User student = userService.getById(booking.getUserId());
        if (student != null) {
            int newScore = Math.max(0, (student.getScore() != null ? student.getScore() : 100) - (request.getPoints() != null ? request.getPoints() : 1));
            userService.update(new LambdaUpdateWrapper<User>().eq(User::getId, student.getId()).set(User::getScore, newScore).set(User::getUpdateTime, now));
        }
        booking.setStatus("CANCELLED");
        booking.setCancelTime(now);
        booking.setCancelReason("老师标记违规：" + request.getDescription());
        bookingService.updateById(booking);
        return Result.success("违规标记成功");
    }

    @GetMapping("/check-violation-time/{bookingId}")
    public Result<Boolean> checkViolationTime(@RequestAttribute Long userId, @PathVariable Long bookingId) {
        User teacher = userService.getById(userId);
        if (!"TEACHER".equals(teacher.getRole()) || teacher.getManagedSpaceId() == null) return Result.error("您无权限操作");
        Booking booking = bookingService.getById(bookingId);
        if (booking == null || !booking.getSpaceId().equals(teacher.getManagedSpaceId()) || !"RESERVED".equals(booking.getStatus())) return Result.success(false);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime bookingStart = LocalDateTime.of(booking.getBookingDate(), booking.getStartTime());
        LocalDateTime bookingEnd = LocalDateTime.of(booking.getBookingDate(), booking.getEndTime());
        return Result.success(!now.isBefore(bookingStart) && !now.isAfter(bookingEnd));
    }
}

class ViolationRequest {
    private Long bookingId;
    private String type;
    private String description;
    private Integer points;
    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }
}