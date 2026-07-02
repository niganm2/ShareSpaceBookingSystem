package com.seatbooking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.seatbooking.dto.BookingDTO;
import com.seatbooking.entity.Booking;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface BookingMapper extends BaseMapper<Booking> {

    List<BookingDTO> selectBookingsByUserIdAndStatus(
            @Param("userId") Long userId,
            @Param("status") String status);

    IPage<BookingDTO> selectBookingPage(
            Page<BookingDTO> page,
            @Param("userId") Long userId,
            @Param("status") String status);

    BookingDTO selectBookingById(@Param("id") Long id);

    List<BookingDTO> selectBookingsByDateAndSeat(
            @Param("date") LocalDate date,
            @Param("seatId") Long seatId);

    List<BookingDTO> selectBookingsBySpaceAndDate(
            @Param("spaceId") Long spaceId,
            @Param("date") LocalDate date);

    List<BookingDTO> selectBookingsByUserAndDate(
            @Param("userId") Long userId,
            @Param("date") LocalDate date);

    IPage<BookingDTO> selectAllBookings(
            Page<BookingDTO> page,
            @Param("spaceName") String spaceName,
            @Param("seatNumber") String seatNumber,
            @Param("bookingDate") LocalDate bookingDate,
            @Param("status") String status);

    List<Map<String, Object>> selectRecent7DaysCountByUserId(@Param("userId") Long userId);

    List<Map<String, Object>> selectAllRecent7DaysCount();

    List<String> selectSeatNumbersBySpaceName(@Param("spaceName") String spaceName);

    List<Map<String, Object>> selectRecent7DaysBySpaceId(@Param("spaceId") Long spaceId);
}
