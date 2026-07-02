package com.seatbooking.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.seatbooking.common.Result;
import com.seatbooking.entity.Seat;
import com.seatbooking.entity.Space;
import com.seatbooking.mapper.SeatMapper;
import com.seatbooking.service.SpaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/space")
public class SpaceController {

    @Autowired
    private SpaceService spaceService;

    @Autowired
    private SeatMapper seatMapper;

    @GetMapping("/list")
    public Result<Map<String, Object>> list() {
        List<Space> spaces = spaceService.list(new QueryWrapper<Space>()
                .eq("deleted", 0)
                .orderByAsc("id"));

        Map<String, Object> result = new HashMap<>();
        result.put("spaces", spaces);
        result.put("spaceCount", spaces.size());

        int totalCapacity = spaces.stream()
                .mapToInt(Space::getCapacity)
                .sum();
        result.put("totalCapacity", totalCapacity);

        Long availableSeatCount = seatMapper.selectCount(
                new QueryWrapper<Seat>()
                        .eq("status", 1)
                        .eq("deleted", 0)
        );
        result.put("availableSeatCount", availableSeatCount);

        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<Space> getById(@PathVariable Long id) {
        Space space = spaceService.getById(id);
        if (space == null) {
            return Result.error("空间不存在");
        }
        return Result.success(space);
    }
}