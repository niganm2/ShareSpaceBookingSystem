package com.seatbooking.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.seatbooking.entity.Seat;

import java.util.List;

public interface SeatService extends IService<Seat> {

    List<Seat> getSeatsBySpaceId(Long spaceId);

    List<Seat> getSeatsWithStatus(Long spaceId);

    List<Seat> getAvailableSeats(Long spaceId);

    boolean saveSeat(Seat seat);

    boolean updateSeat(Seat seat);

    boolean removeSeat(Long id);

    void evictAllCache();
}