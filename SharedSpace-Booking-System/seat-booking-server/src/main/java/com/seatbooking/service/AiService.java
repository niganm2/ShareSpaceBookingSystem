package com.seatbooking.service;

import com.seatbooking.dto.ChatRequest;
import com.seatbooking.dto.ChatResponse;

public interface AiService {
    ChatResponse chat(Long userId, String role, ChatRequest request);
}