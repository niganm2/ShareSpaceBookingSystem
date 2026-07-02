package com.seatbooking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seatbooking.entity.Seat;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SeatMapper extends BaseMapper<Seat> {
}
