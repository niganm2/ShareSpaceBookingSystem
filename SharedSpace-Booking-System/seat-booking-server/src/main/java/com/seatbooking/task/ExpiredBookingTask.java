package com.seatbooking.task;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.seatbooking.entity.Booking;
import com.seatbooking.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
public class ExpiredBookingTask {

    @Autowired
    private BookingService bookingService;

    @Scheduled(cron = "0 * * * * ?")
    public void updateExpiredBookings() {
        LocalDateTime now = LocalDateTime.now();

        bookingService.update(new LambdaUpdateWrapper<Booking>()
                .eq(Booking::getStatus, "RESERVED")
                .lt(Booking::getEndTime, now.toLocalTime())
                .eq(Booking::getBookingDate, now.toLocalDate())
                .set(Booking::getStatus, "EXPIRED")
                .set(Booking::getUpdateTime, now));
    }

    @Scheduled(cron = "0 * * * * ?")
    public void autoCheckOutBookings() {
        LocalDateTime now = LocalDateTime.now();
        LocalTime currentTime = now.toLocalTime();

        bookingService.update(new LambdaUpdateWrapper<Booking>()
                .eq(Booking::getStatus, "CHECKED_IN")
                .le(Booking::getBookingDate, now.toLocalDate())
                .lt(Booking::getEndTime, currentTime.minusMinutes(10))
                .set(Booking::getStatus, "CHECKED_OUT")
                .set(Booking::getCheckOutTime, now)
                .set(Booking::getUpdateTime, now));
    }
}