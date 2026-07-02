package com.seatbooking.controller;

import com.seatbooking.common.Result;
import com.seatbooking.entity.Seat;
import com.seatbooking.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/seat")
public class SeatController {

    @Autowired
    private SeatService seatService;

    @GetMapping("/list/{spaceId}")
    public Result<List<Seat>> listBySpaceId(@PathVariable Long spaceId) {
        List<Seat> seats = seatService.getSeatsBySpaceId(spaceId);
        return Result.success(seats);
    }

    @GetMapping("/list/{spaceId}/with-status")
    public Result<List<Seat>> listBySpaceIdWithStatus(@PathVariable Long spaceId) {
        List<Seat> seats = seatService.getSeatsWithStatus(spaceId);
        return Result.success(seats);
    }

    @GetMapping("/available/{spaceId}")
    public Result<List<Seat>> getAvailableSeats(@PathVariable Long spaceId) {
        List<Seat> seats = seatService.getAvailableSeats(spaceId);
        return Result.success(seats);
    }

    @GetMapping("/{id}")
    public Result<Seat> getById(@PathVariable Long id) {
        Seat seat = seatService.getById(id);
        if (seat == null) {
            return Result.error("座位不存在");
        }
        return Result.success(seat);
    }
}