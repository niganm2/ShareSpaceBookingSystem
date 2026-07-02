package com.seatbooking.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.seatbooking.dto.BookingDTO;
import com.seatbooking.entity.Booking;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface BookingService extends IService<Booking> {

    Booking createBooking(Long userId, Long seatId, Long spaceId, LocalDate date, String startTime, String endTime);

    boolean cancelBooking(Long bookingId, Long userId, String reason);

    boolean checkIn(Long bookingId, Long userId);

    boolean checkOut(Long bookingId, Long userId);

    IPage<BookingDTO> getBookingList(Long userId, String status, Integer pageNum, Integer pageSize);

    BookingDTO getBookingById(Long id);

    List<BookingDTO> getBookingsByDateAndSeat(LocalDate date, Long seatId);

    List<BookingDTO> getBookingsBySpaceAndDate(Long spaceId, LocalDate date);

    List<BookingDTO> getBookingsByUserAndDate(Long userId, LocalDate date);

    IPage<BookingDTO> getAllBookings(String spaceName, String seatNumber, LocalDate bookingDate, String status, Integer pageNum, Integer pageSize);

    List<Map<String, Object>> selectRecent7DaysCountByUserId(Long userId);

    List<Map<String, Object>> getAllRecent7DaysCount();

    List<String> getSeatNumbersBySpaceName(String spaceName);

    List<Map<String, Object>> getRecent7DaysBySpaceId(Long spaceId);

    void updateBookingTime(Long id, Long userId, String startTime, String endTime);
}