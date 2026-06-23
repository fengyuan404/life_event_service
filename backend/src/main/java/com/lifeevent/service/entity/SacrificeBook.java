package com.lifeevent.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sacrifice_book")
public class SacrificeBook {
    @TableId(type = IdType.AUTO)
    private Integer bookId;
    private Integer familyId;
    private Integer graveId;
    private Integer staffId;
    private LocalDateTime bookTime;
    private Integer visitorCount;
    private String specialNeed;
    private String checkinStatus;
    private LocalDateTime checkinTime;
    private LocalDateTime createdAt;
}

