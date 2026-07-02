package com.seatbooking.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.seatbooking.entity.Violation;

public interface ViolationService extends IService<Violation> {

    Violation createViolation(Long userId, Long bookingId, String type, String description, Integer points);
}