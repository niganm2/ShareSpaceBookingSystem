package com.seatbooking.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.seatbooking.entity.Space;

import java.util.List;

public interface SpaceService extends IService<Space> {

    int countAvailableSeats(Long spaceId);

    List<Space> getAllSpaces();

    boolean saveSpace(Space space);

    boolean updateSpace(Space space);

    boolean removeSpace(Long id);

    boolean disableSpace(Space space);

    boolean enableSpace(Long id);

    void evictAllCache();
}