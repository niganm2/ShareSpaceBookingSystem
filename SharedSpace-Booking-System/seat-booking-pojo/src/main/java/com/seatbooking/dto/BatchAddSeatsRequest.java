package com.seatbooking.dto;

import lombok.Data;

@Data
public class BatchAddSeatsRequest {
    private Long spaceId;
    private String prefix;
    private Integer count;
    private String type;
}