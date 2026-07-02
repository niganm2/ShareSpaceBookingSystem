package com.seatbooking.task;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.seatbooking.entity.User;
import com.seatbooking.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ScoreResetTask {

    @Autowired
    private UserService userService;

    @Scheduled(cron = "0 0 0 1 * ?")
    @CacheEvict(value = "user", allEntries = true)
    public void resetMonthlyScore() {
        LocalDateTime now = LocalDateTime.now();
        userService.update(new LambdaUpdateWrapper<User>()
                .eq(User::getDeleted, 0)
                .set(User::getScore, 100)
                .set(User::getUpdateTime, now));
    }
}
