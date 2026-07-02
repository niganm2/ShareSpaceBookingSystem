package com.seatbooking.controller;

import com.seatbooking.common.Result;
import com.seatbooking.entity.User;
import com.seatbooking.service.BookingService;
import com.seatbooking.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher-space")
public class TeacherSpaceController {

    @Autowired
    private UserService userService;
    @Autowired
    private BookingService bookingService;

    @GetMapping("/my-space")
    public Result<?> getMySpace(HttpServletRequest request) {
        Long userId = Long.parseLong(request.getAttribute("userId").toString());
        User user = userService.getById(userId);
        if (user.getManagedSpaceId() == null) return Result.error("你还没有绑定管理的自习室");
        return Result.success(user);
    }

    @GetMapping("/today-bookings")
    public Result<?> getTodayBookings(HttpServletRequest request) {
        Long userId = Long.parseLong(request.getAttribute("userId").toString());
        User user = userService.getById(userId);
        if (user.getManagedSpaceId() == null) return Result.error("未绑定自习室");
        return Result.success(bookingService.getBookingsBySpaceAndDate(user.getManagedSpaceId(), LocalDate.now()));
    }

    @GetMapping("/recent7days")
    public Result<?> getRecent7Days(HttpServletRequest request) {
        try {
            Long userId = Long.parseLong(request.getAttribute("userId").toString());
            User user = userService.getById(userId);
            if (user.getManagedSpaceId() == null) return Result.error("未绑定自习室");
            return Result.success(bookingService.getRecent7DaysBySpaceId(user.getManagedSpaceId()));
        } catch (Exception e) {
            return Result.error("服务器错误");
        }
    }
}