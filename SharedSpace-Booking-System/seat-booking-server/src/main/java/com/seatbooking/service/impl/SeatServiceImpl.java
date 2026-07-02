package com.seatbooking.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.seatbooking.entity.Seat;
import com.seatbooking.mapper.SeatMapper;
import com.seatbooking.service.SeatService;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@CacheConfig(cacheNames = "seat")
public class SeatServiceImpl extends ServiceImpl<SeatMapper, Seat> implements SeatService {

    @Override
    @Cacheable(key = "'list_' + #spaceId")
    public List<Seat> getSeatsBySpaceId(Long spaceId) {
        return list(new LambdaQueryWrapper<Seat>()
                .eq(Seat::getSpaceId, spaceId)
                .eq(Seat::getDeleted, 0)
                .orderByAsc(Seat::getSeatNumber));
    }

    @Override
    @Cacheable(key = "'status_' + #spaceId")
    public List<Seat> getSeatsWithStatus(Long spaceId) {
        return list(new LambdaQueryWrapper<Seat>()
                .eq(Seat::getSpaceId, spaceId)
                .eq(Seat::getDeleted, 0)
                .orderByAsc(Seat::getSeatNumber));
    }

    @Override
    @Cacheable(key = "'available_' + #spaceId")
    public List<Seat> getAvailableSeats(Long spaceId) {
        return list(new LambdaQueryWrapper<Seat>()
                .eq(Seat::getSpaceId, spaceId)
                .eq(Seat::getStatus, 1)
                .eq(Seat::getDeleted, 0)
                .orderByAsc(Seat::getSeatNumber));
    }

    @Override
    @CacheEvict(allEntries = true)
    public boolean saveSeat(Seat seat) {
        return save(seat);
    }

    @Override
    @CacheEvict(allEntries = true)
    public boolean updateSeat(Seat seat) {
        return updateById(seat);
    }

    @Override
    @CacheEvict(allEntries = true)
    public boolean removeSeat(Long id) {
        return removeById(id);
    }

    @Override
    @CacheEvict(allEntries = true)
    public void evictAllCache() {
    }
}