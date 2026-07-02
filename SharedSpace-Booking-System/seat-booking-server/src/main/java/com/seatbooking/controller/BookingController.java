package com.seatbooking.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.seatbooking.annotation.OperationLog;
import com.seatbooking.common.Result;
import com.seatbooking.context.UserContext;
import com.seatbooking.dto.BookingDTO;
import com.seatbooking.entity.Booking;
import com.seatbooking.entity.User;
import com.seatbooking.service.BookingService;
import com.seatbooking.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/booking")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserService userService;

    @OperationLog("创建预约")
    @PostMapping("/create")
    public Result<Booking> create(@RequestBody BookingDTO bookingDTO, @RequestAttribute Long userId) {
        try {
            User user = userService.getById(userId);
            int score = user.getScore() != null ? user.getScore() : 100;
            if (score < 80) {
                String unlockTime = LocalDateTime.now()
                        .plusMonths(1)
                        .withDayOfMonth(1)
                        .withHour(0).withMinute(0).withSecond(0)
                        .format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm"));
                return Result.error("您的信用分低于80分，将于 " + unlockTime + " 解封");
            }
            Booking booking = bookingService.createBooking(userId, bookingDTO.getSeatId(), bookingDTO.getSpaceId(),
                    bookingDTO.getBookingDate(), bookingDTO.getStartTime().toString(), bookingDTO.getEndTime().toString());
            return Result.success("预约成功", booking);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @OperationLog("取消预约")
    @PostMapping("/cancel/{id}")
    public Result<Boolean> cancel(@PathVariable Long id, @RequestParam(required = false) String reason, @RequestAttribute Long userId) {
        try {
            boolean success = bookingService.cancelBooking(id, userId, reason);
            return Result.success("取消成功", success);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @OperationLog("预约签到")
    @PostMapping("/checkin/{id}")
    public Result<Boolean> checkIn(@PathVariable Long id, @RequestAttribute Long userId) {
        try {
            boolean success = bookingService.checkIn(id, userId);
            return Result.success("签到成功", success);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @OperationLog("预约签退")
    @PostMapping("/checkout/{id}")
    public Result<Boolean> checkOut(@PathVariable Long id, @RequestAttribute Long userId) {
        try {
            boolean success = bookingService.checkOut(id, userId);
            return Result.success("签退成功", success);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/list")
    public Result<IPage<BookingDTO>> list(@RequestParam(required = false) String status,
                                          @RequestParam(defaultValue = "1") Integer pageNum,
                                          @RequestParam(defaultValue = "10") Integer pageSize, @RequestAttribute Long userId) {
        return Result.success(bookingService.getBookingList(userId, status, pageNum, pageSize));
    }

    @GetMapping("/date/{date}/seat/{seatId}")
    public Result<List<BookingDTO>> getByDateAndSeat(@PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date, @PathVariable Long seatId) {
        return Result.success(bookingService.getBookingsByDateAndSeat(date, seatId));
    }

    @GetMapping("/date/{date}/space/{spaceId}")
    public Result<List<BookingDTO>> getByDateAndSpace(@PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date, @PathVariable Long spaceId) {
        return Result.success(bookingService.getBookingsBySpaceAndDate(spaceId, date));
    }

    @GetMapping("/date/{date}")
    public Result<List<BookingDTO>> getByUserAndDate(@PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date, @RequestAttribute Long userId) {
        return Result.success(bookingService.getBookingsByUserAndDate(userId, date));
    }

    @GetMapping("/{id}")
    public Result<BookingDTO> getById(@PathVariable Long id) {
        BookingDTO booking = bookingService.getBookingById(id);
        if (booking == null) return Result.error("预约记录不存在");
        return Result.success(booking);
    }

    @OperationLog("修改预约时间")
    @PutMapping("/{id}")
    public Result<?> updateBookingTime(@PathVariable Long id, @RequestBody Map<String, Object> params, @RequestAttribute Long userId) {
        try {
            bookingService.updateBookingTime(id, userId, (String) params.get("startTime"), (String) params.get("endTime"));
            return Result.success("修改成功");
        } catch (Exception e) {
            return Result.error("修改失败：" + e.getMessage());
        }
    }

    @OperationLog("删除预约")
    @DeleteMapping("/delete/{id}")
    public Result<?> deleteBooking(@PathVariable Long id, @RequestAttribute Long userId) {
        try {
            Booking booking = bookingService.getById(id);
            if (booking == null) return Result.error("预约记录不存在");
            if (!booking.getUserId().equals(userId)) return Result.error("只能删除自己的预约");
            LocalDateTime endTime = LocalDateTime.of(booking.getBookingDate(), booking.getEndTime());
            boolean isExpired = LocalDateTime.now().isAfter(endTime);
            if (!("CANCELLED".equals(booking.getStatus()) || "EXPIRED".equals(booking.getStatus()) || "CHECKED_OUT".equals(booking.getStatus()) || isExpired))
                return Result.error("只能删除已失效的预约");
            bookingService.removeById(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    @GetMapping("/recent7days")
    public Result<List<Map<String, Object>>> getRecent7Days() {
        return Result.success(bookingService.selectRecent7DaysCountByUserId(UserContext.getCurrentUserId()));
    }

    @GetMapping("/all/recent7days")
    public Result<List<Map<String, Object>>> getAllRecent7Days() {
        return Result.success(bookingService.getAllRecent7DaysCount());
    }

    @GetMapping("/seats-by-space")
    public Result<List<String>> getSeatsBySpaceName(@RequestParam String spaceName) {
        return Result.success(bookingService.getSeatNumbersBySpaceName(spaceName));
    }

    @GetMapping("/all")
    public Result<IPage<BookingDTO>> getAllBookings(@RequestParam(required = false) String spaceName,
                                                     @RequestParam(required = false) String seatNumber,
                                                     @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate bookingDate,
                                                     @RequestParam(required = false) String status,
                                                     @RequestParam(defaultValue = "1") Integer pageNum,
                                                     @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(bookingService.getAllBookings(spaceName, seatNumber, bookingDate, status, pageNum, pageSize));
    }
}