package com.lifeevent.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("payment")
public class Payment {
    @TableId(type = IdType.AUTO)
    private Integer paymentId;
    private Integer rentId;
    private Integer staffId;
    private String payType;
    private String payMethod;
    private BigDecimal payAmount;
    private LocalDateTime payTime;
    private String invoiceNo;
    private String remark;
}

