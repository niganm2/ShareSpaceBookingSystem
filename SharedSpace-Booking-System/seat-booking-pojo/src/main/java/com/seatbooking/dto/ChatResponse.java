package com.seatbooking.dto;

import lombok.Data;

@Data
public class ChatResponse {
    private String reply;
    private String intent;
    private Boolean success;
    private Object data;
}