package com.seatbooking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seatbooking.dto.ViolationDetailDTO;
import com.seatbooking.entity.Violation;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ViolationMapper extends BaseMapper<Violation> {

    List<ViolationDetailDTO> selectAllViolationDetails();
}
