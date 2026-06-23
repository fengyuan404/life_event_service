package com.lifeevent.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("grave")
public class Grave {
    @TableId(type = IdType.AUTO)
    private Integer graveId;
    @NotNull
    private Integer areaId;
    @NotBlank
    private String graveCode;
    @NotBlank
    private String locationDesc;
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal graveSize;
    @NotBlank
    private String status;
    @NotNull
    @DecimalMin("0.00")
    private BigDecimal rentPrice;
    @Min(1)
    private Integer maxYears;
    private String remark;
}

