package com.seatbooking.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.seatbooking.common.Result;
import com.seatbooking.entity.Seat;
import com.seatbooking.entity.Space;
import com.seatbooking.mapper.SeatMapper;
import com.seatbooking.mapper.SpaceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private SeatMapper seatMapper;
    @Autowired
    private SpaceMapper spaceMapper;

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Map<String, Object> map = new HashMap<>();
        long spaceCount = spaceMapper.selectCount(new QueryWrapper<Space>().eq("deleted", 0));
        map.put("spaceCount", spaceCount);
        List<Space> spaces = spaceMapper.selectList(new QueryWrapper<Space>().eq("deleted", 0).select("capacity"));
        int totalCapacity = spaces.stream().mapToInt(Space::getCapacity).sum();
        map.put("totalCapacity", totalCapacity);
        map.put("availableSeatCount", totalCapacity);
        map.put("todayBookingCount", 0);
        map.put("violationCount", 0);
        return Result.success(map);
    }
}