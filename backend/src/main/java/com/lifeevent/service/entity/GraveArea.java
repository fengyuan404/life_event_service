package com.lifeevent.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("grave_area")
public class GraveArea {
    @TableId(type = IdType.AUTO)
    private Integer areaId;
    @NotBlank
    private String areaCode;
    @NotBlank
    private String areaName;
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal areaSize;
    @NotNull
    @DecimalMin("0.00")
    private BigDecimal basePrice;
    private String environmentDesc;
    @NotBlank
    private String status;
}

