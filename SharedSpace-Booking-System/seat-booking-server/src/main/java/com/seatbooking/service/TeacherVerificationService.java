package com.seatbooking.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.seatbooking.entity.TeacherVerification;

import java.util.List;

public interface TeacherVerificationService extends IService<TeacherVerification> {

    TeacherVerification submitVerification(Long userId, String realName, String phone, String verificationCode);

    TeacherVerification getVerificationByUserId(Long userId);

    List<TeacherVerification> getAllVerifications();
}