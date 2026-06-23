package com.lifeevent.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("rent_record")
public class RentRecord {
    @TableId(type = IdType.AUTO)
    private Integer rentId;
    private Integer familyId;
    private Integer deceasedId;
    private Integer graveId;
    private Integer staffId;
    private LocalDate startDate;
    private LocalDate expireDate;
    private BigDecimal totalAmount;
    private String rentStatus;
    @TableField(exist = false)
    private Integer activeGraveId;
    private LocalDateTime createdAt;
}

