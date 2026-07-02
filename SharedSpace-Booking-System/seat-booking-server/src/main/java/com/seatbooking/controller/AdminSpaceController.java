package com.seatbooking.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.seatbooking.annotation.OperationLog;
import com.seatbooking.common.Result;
import com.seatbooking.dto.BatchAddSeatsRequest;
import com.seatbooking.entity.Seat;
import com.seatbooking.entity.Space;
import com.seatbooking.service.SeatService;
import com.seatbooking.service.SpaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/space")
public class AdminSpaceController {

    @Autowired
    private SpaceService spaceService;
    @Autowired
    private SeatService seatService;

    @GetMapping("/list")
    public Result<List<Space>> list() {
        return Result.success(spaceService.getAllSpaces());
    }

    @OperationLog("新增空间")
    @PostMapping("/add")
    public Result<?> add(@RequestBody Space space) {
        space.setDeleted(0);
        space.setStatus(1);
        space.setCreateTime(LocalDateTime.now());
        spaceService.saveSpace(space);
        return Result.success("添加成功");
    }

    @OperationLog("修改空间")
    @PutMapping("/update")
    public Result<?> update(@RequestBody Space space) {
        space.setUpdateTime(LocalDateTime.now());
        spaceService.updateSpace(space);
        return Result.success("更新成功");
    }

    @OperationLog("删除空间")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        spaceService.update(new LambdaUpdateWrapper<Space>().eq(Space::getId, id).set(Space::getDeleted, 1).set(Space::getUpdateTime, LocalDateTime.now()));
        seatService.update(new LambdaUpdateWrapper<Seat>().eq(Seat::getSpaceId, id).set(Seat::getDeleted, 1).set(Seat::getUpdateTime, LocalDateTime.now()));
        spaceService.evictAllCache();
        seatService.evictAllCache();
        return Result.success("删除成功");
    }

    @OperationLog("禁用空间")
    @PostMapping("/disable")
    public Result<?> disable(@RequestBody Space space) {
        Space updateSpace = new Space();
        updateSpace.setId(space.getId());
        updateSpace.setDisabledFrom(space.getDisabledFrom());
        updateSpace.setDisabledUntil(space.getDisabledUntil());
        updateSpace.setDisabledReason(space.getDisabledReason());
        updateSpace.setStatus(0);
        updateSpace.setUpdateTime(LocalDateTime.now());
        spaceService.disableSpace(updateSpace);
        return Result.success("禁用成功");
    }

    @OperationLog("启用空间")
    @PostMapping("/enable/{id}")
    public Result<?> enable(@PathVariable Long id) {
        spaceService.enableSpace(id);
        return Result.success("启用成功");
    }

    @GetMapping("/seats/{spaceId}")
    public Result<List<Seat>> getSeats(@PathVariable Long spaceId) {
        return Result.success(seatService.getSeatsBySpaceId(spaceId));
    }

    @OperationLog("新增座位")
    @PostMapping("/seat/add")
    public Result<?> addSeat(@RequestBody Seat seat) {
        seat.setDeleted(0);
        seat.setStatus(1);
        seat.setCreateTime(LocalDateTime.now());
        seatService.saveSeat(seat);
        return Result.success("添加座位成功");
    }

    @OperationLog("批量新增座位")
    @PostMapping("/seat/batch-add")
    public Result<?> batchAddSeats(@RequestBody BatchAddSeatsRequest request) {
        String prefix = request.getPrefix() != null ? request.getPrefix() : "";
        String type = request.getType() != null ? request.getType() : "普通座位";
        for (int i = 1; i <= request.getCount(); i++) {
            Seat seat = new Seat();
            seat.setSpaceId(request.getSpaceId());
            seat.setSeatNumber(prefix + String.format("%03d", i));
            seat.setType(type);
            seat.setStatus(1);
            seat.setDeleted(0);
            seat.setCreateTime(LocalDateTime.now());
            seatService.save(seat);
        }
        seatService.evictAllCache();
        return Result.success("批量添加" + request.getCount() + "个座位成功");
    }

    @OperationLog("禁用座位")
    @PostMapping("/seat/disable/{id}")
    public Result<?> disableSeat(@PathVariable Long id) {
        Seat seat = new Seat();
        seat.setId(id);
        seat.setStatus(0);
        seat.setUpdateTime(LocalDateTime.now());
        seatService.updateSeat(seat);
        return Result.success("禁用座位成功");
    }

    @OperationLog("启用座位")
    @PostMapping("/seat/enable/{id}")
    public Result<?> enableSeat(@PathVariable Long id) {
        Seat seat = new Seat();
        seat.setId(id);
        seat.setStatus(1);
        seat.setUpdateTime(LocalDateTime.now());
        seatService.updateSeat(seat);
        return Result.success("启用座位成功");
    }

    @OperationLog("修改座位")
    @PutMapping("/seat/update")
    public Result<?> updateSeat(@RequestBody Seat seat) {
        seat.setUpdateTime(LocalDateTime.now());
        seatService.updateSeat(seat);
        return Result.success("更新座位成功");
    }

    @OperationLog("删除座位")
    @DeleteMapping("/seat/{id}")
    public Result<?> deleteSeat(@PathVariable Long id) {
        seatService.update(new LambdaUpdateWrapper<Seat>().eq(Seat::getId, id).set(Seat::getDeleted, 1).set(Seat::getUpdateTime, LocalDateTime.now()));
        seatService.evictAllCache();
        return Result.success("删除座位成功");
    }
}