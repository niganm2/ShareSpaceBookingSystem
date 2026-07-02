package com.seatbooking.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.seatbooking.entity.TeacherVerification;
import com.seatbooking.mapper.TeacherVerificationMapper;
import com.seatbooking.service.TeacherVerificationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TeacherVerificationServiceImpl extends ServiceImpl<TeacherVerificationMapper, TeacherVerification> implements TeacherVerificationService {

    @Override
    public TeacherVerification submitVerification(Long userId, String realName, String phone, String verificationCode) {
        TeacherVerification verification = new TeacherVerification();
        verification.setUserId(userId);
        verification.setRealName(realName);
        verification.setPhone(phone);
        verification.setVerificationCode(verificationCode);
        verification.setReviewStatus("PENDING");
        verification.setCreateTime(LocalDateTime.now());

        save(verification);
        return verification;
    }

    @Override
    public TeacherVerification getVerificationByUserId(Long userId) {
        return getOne(new LambdaQueryWrapper<TeacherVerification>()
                .eq(TeacherVerification::getUserId, userId)
                .orderByDesc(TeacherVerification::getCreateTime)
                .last("LIMIT 1"));
    }

    @Override
    public List<TeacherVerification> getAllVerifications() {
        return list(new LambdaQueryWrapper<TeacherVerification>()
                .orderByDesc(TeacherVerification::getCreateTime));
    }
}