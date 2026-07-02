package com.seatbooking.dto;

import com.seatbooking.entity.Booking;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class BookingDTO {
    private Long id;
    private Long userId;
    private Long seatId;
    private Long spaceId;
    private String seatNumber;
    private String spaceName;
    private String userName;
    private String userRealName;
    private String userStudentId;
    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String status;
    private String cancelReason;

    public static BookingDTO fromBooking(Booking booking) {
        BookingDTO dto = new BookingDTO();
        dto.setId(booking.getId());
        dto.setUserId(booking.getUserId());
        dto.setSeatId(booking.getSeatId());
        dto.setSpaceId(booking.getSpaceId());
        dto.setBookingDate(booking.getBookingDate());
        dto.setStartTime(booking.getStartTime());
        dto.setEndTime(booking.getEndTime());
        dto.setStatus(booking.getStatus());
        dto.setCancelReason(booking.getCancelReason());
        return dto;
    }
}