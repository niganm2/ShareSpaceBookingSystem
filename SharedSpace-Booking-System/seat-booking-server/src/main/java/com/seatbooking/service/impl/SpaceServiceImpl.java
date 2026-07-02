package com.seatbooking.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.seatbooking.entity.Seat;
import com.seatbooking.entity.Space;
import com.seatbooking.mapper.SpaceMapper;
import com.seatbooking.service.SpaceService;
import com.seatbooking.mapper.SeatMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@CacheConfig(cacheNames = "space")
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space> implements SpaceService {

    @Autowired
    private SeatMapper seatMapper;

    @Override
    public int countAvailableSeats(Long spaceId) {
        Long count = seatMapper.selectCount(new LambdaQueryWrapper<Seat>()
                .eq(Seat::getSpaceId, spaceId)
                .eq(Seat::getStatus, 1)
                .eq(Seat::getDeleted, 0));
        return count != null ? count.intValue() : 0;
    }

    @Override
    @Cacheable(key = "'all'")
    public List<Space> getAllSpaces() {
        return list(new LambdaQueryWrapper<Space>()
                .eq(Space::getDeleted, 0)
                .orderByDesc(Space::getCreateTime));
    }

    @Override
    @CacheEvict(key = "'all'")
    public boolean saveSpace(Space space) {
        return save(space);
    }

    @Override
    @CacheEvict(key = "'all'")
    public boolean updateSpace(Space space) {
        return updateById(space);
    }

    @Override
    @CacheEvict(key = "'all'")
    public boolean removeSpace(Long id) {
        return removeById(id);
    }

    @Override
    @CacheEvict(key = "'all'")
    public boolean disableSpace(Space space) {
        return updateById(space);
    }

    @Override
    @CacheEvict(key = "'all'")
    public boolean enableSpace(Long id) {
        Space updateSpace = new Space();
        updateSpace.setId(id);
        updateSpace.setStatus(1);
        return updateById(updateSpace);
    }

    @Override
    @CacheEvict(allEntries = true)
    public void evictAllCache() {
    }
}