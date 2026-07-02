package com.seatbooking.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.seatbooking.dto.BookingDTO;
import com.seatbooking.entity.Booking;
import com.seatbooking.mapper.BookingMapper;
import com.seatbooking.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Service
@CacheConfig(cacheNames = "booking")
public class BookingServiceImpl extends ServiceImpl<BookingMapper, Booking> implements BookingService {

    @Autowired
    private BookingMapper bookingMapper;

    @Override
    @CacheEvict(allEntries = true)
    public Booking createBooking(Long userId, Long seatId, Long spaceId, LocalDate date, String startTime, String endTime) {
        LocalTime start = LocalTime.parse(startTime);
        LocalTime end = LocalTime.parse(endTime);

        List<BookingDTO> existing = bookingMapper.selectBookingsByDateAndSeat(date, seatId);
        for (BookingDTO b : existing) {
            if ("RESERVED".equals(b.getStatus()) || "CHECKED_IN".equals(b.getStatus())) {
                LocalTime existStart = b.getStartTime();
                LocalTime existEnd = b.getEndTime();
                if (!(end.isBefore(existStart) || start.isAfter(existEnd))) {
                    throw new RuntimeException("该时间段已被预约");
                }
            }
        }

        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setSeatId(seatId);
        booking.setSpaceId(spaceId);
        booking.setBookingDate(date);
        booking.setStartTime(start);
        booking.setEndTime(end);
        booking.setStatus("RESERVED");
        booking.setDeleted(0);
        booking.setCreateTime(LocalDateTime.now());

        save(booking);
        return booking;
    }

    @Override
    @CacheEvict(allEntries = true)
    public boolean cancelBooking(Long bookingId, Long userId, String reason) {
        Booking booking = getById(bookingId);
        if (booking == null || !booking.getUserId().equals(userId)) {
            throw new RuntimeException("预约记录不存在或无权操作");
        }

        if (!"RESERVED".equals(booking.getStatus())) {
            throw new RuntimeException("只能取消已预约状态的记录");
        }

        booking.setStatus("CANCELLED");
        booking.setCancelTime(LocalDateTime.now());
        booking.setCancelReason(reason);
        booking.setUpdateTime(LocalDateTime.now());

        return updateById(booking);
    }

    @Override
    @CacheEvict(allEntries = true)
    public boolean checkIn(Long bookingId, Long userId) {
        Booking booking = getById(bookingId);
        if (booking == null || !booking.getUserId().equals(userId)) {
            throw new RuntimeException("预约记录不存在或无权操作");
        }

        if (!"RESERVED".equals(booking.getStatus())) {
            throw new RuntimeException("只能签到已预约状态的记录");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime bookingStart = LocalDateTime.of(booking.getBookingDate(), booking.getStartTime());
        LocalDateTime bookingEnd = LocalDateTime.of(booking.getBookingDate(), booking.getEndTime());

        if (now.isBefore(bookingStart.minusMinutes(30)) || now.isAfter(bookingEnd)) {
            throw new RuntimeException("不在签到时间范围内");
        }

        booking.setStatus("CHECKED_IN");
        booking.setCheckInTime(now);
        booking.setUpdateTime(now);

        return updateById(booking);
    }

    @Override
    @CacheEvict(allEntries = true)
    public boolean checkOut(Long bookingId, Long userId) {
        Booking booking = getById(bookingId);
        if (booking == null || !booking.getUserId().equals(userId)) {
            throw new RuntimeException("预约记录不存在或无权操作");
        }

        if (!"CHECKED_IN".equals(booking.getStatus())) {
            throw new RuntimeException("只能签退已签到状态的记录");
        }

        LocalDateTime now = LocalDateTime.now();
        booking.setStatus("CHECKED_OUT");
        booking.setCheckOutTime(now);
        booking.setUpdateTime(now);

        return updateById(booking);
    }

    @Override
    public IPage<BookingDTO> getBookingList(Long userId, String status, Integer pageNum, Integer pageSize) {
        Page<BookingDTO> page = new Page<>(pageNum, pageSize);
        return bookingMapper.selectBookingPage(page, userId, status);
    }

    @Override
    public BookingDTO getBookingById(Long id) {
        return bookingMapper.selectBookingById(id);
    }

    @Override
    public List<BookingDTO> getBookingsByDateAndSeat(LocalDate date, Long seatId) {
        return bookingMapper.selectBookingsByDateAndSeat(date, seatId);
    }

    @Override
    @Cacheable(key = "'space_' + #spaceId + '_' + #date")
    public List<BookingDTO> getBookingsBySpaceAndDate(Long spaceId, LocalDate date) {
        return bookingMapper.selectBookingsBySpaceAndDate(spaceId, date);
    }

    @Override
    public List<BookingDTO> getBookingsByUserAndDate(Long userId, LocalDate date) {
        return bookingMapper.selectBookingsByUserAndDate(userId, date);
    }

    @Override
    public IPage<BookingDTO> getAllBookings(String spaceName, String seatNumber, LocalDate bookingDate, String status, Integer pageNum, Integer pageSize) {
        Page<BookingDTO> page = new Page<>(pageNum, pageSize);
        return bookingMapper.selectAllBookings(page, spaceName, seatNumber, bookingDate, status);
    }

    @Override
    public List<Map<String, Object>> selectRecent7DaysCountByUserId(Long userId) {
        return bookingMapper.selectRecent7DaysCountByUserId(userId);
    }

    @Override
    public List<Map<String, Object>> getAllRecent7DaysCount() {
        return bookingMapper.selectAllRecent7DaysCount();
    }

    @Override
    public List<String> getSeatNumbersBySpaceName(String spaceName) {
        return bookingMapper.selectSeatNumbersBySpaceName(spaceName);
    }

    @Override
    public List<Map<String, Object>> getRecent7DaysBySpaceId(Long spaceId) {
        return bookingMapper.selectRecent7DaysBySpaceId(spaceId);
    }

    @Override
    @CacheEvict(allEntries = true)
    public void updateBookingTime(Long id, Long userId, String startTime, String endTime) {
        Booking booking = getById(id);
        if (booking == null || !booking.getUserId().equals(userId)) {
            throw new RuntimeException("预约记录不存在或无权操作");
        }

        if (!"RESERVED".equals(booking.getStatus())) {
            throw new RuntimeException("只能修改已预约状态的记录");
        }

        LocalTime start = LocalTime.parse(startTime);
        LocalTime end = LocalTime.parse(endTime);

        List<BookingDTO> existing = bookingMapper.selectBookingsByDateAndSeat(booking.getBookingDate(), booking.getSeatId());
        for (BookingDTO b : existing) {
            if (!b.getId().equals(id) && ("RESERVED".equals(b.getStatus()) || "CHECKED_IN".equals(b.getStatus()))) {
                LocalTime existStart = b.getStartTime();
                LocalTime existEnd = b.getEndTime();
                if (!(end.isBefore(existStart) || start.isAfter(existEnd))) {
                    throw new RuntimeException("该时间段已被预约");
                }
            }
        }

        booking.setStartTime(start);
        booking.setEndTime(end);
        booking.setUpdateTime(LocalDateTime.now());
        updateById(booking);
    }
}