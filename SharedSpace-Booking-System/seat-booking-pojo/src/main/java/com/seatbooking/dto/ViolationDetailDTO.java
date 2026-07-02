package com.seatbooking.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ViolationDetailDTO {
    private Long id;
    private Long userId;
    private String userName;
    private String userRealName;
    private String userStudentId;
    private Long bookingId;
    private String seatNumber;
    private String spaceName;
    private String type;
    private String description;
    private Integer points;
    private LocalDateTime createTime;
}