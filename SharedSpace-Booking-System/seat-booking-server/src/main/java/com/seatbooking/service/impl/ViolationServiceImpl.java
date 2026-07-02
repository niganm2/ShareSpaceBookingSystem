package com.seatbooking.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.seatbooking.entity.Violation;
import com.seatbooking.mapper.ViolationMapper;
import com.seatbooking.service.ViolationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ViolationServiceImpl extends ServiceImpl<ViolationMapper, Violation> implements ViolationService {

    @Override
    public Violation createViolation(Long userId, Long bookingId, String type, String description, Integer points) {
        Violation violation = new Violation();
        violation.setUserId(userId);
        violation.setBookingId(bookingId);
        violation.setType(type);
        violation.setDescription(description);
        violation.setPoints(points);
        violation.setDeleted(0);
        violation.setCreateTime(LocalDateTime.now());

        save(violation);
        return violation;
    }
}