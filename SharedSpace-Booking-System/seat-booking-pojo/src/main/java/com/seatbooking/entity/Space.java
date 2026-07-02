package com.seatbooking.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("space")
public class Space {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String location;
    private Integer capacity;
    private String description;
    private Integer status;
    private LocalDateTime disabledFrom;
    private LocalDateTime disabledUntil;
    private String disabledReason;
    private Integer deleted;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}