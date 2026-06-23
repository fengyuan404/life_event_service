package com.lifeevent.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("family")
public class Family {
    @TableId(type = IdType.AUTO)
    private Integer familyId;
    @NotBlank
    private String familyName;
    @NotBlank
    private String gender;
    @NotBlank
    private String phone;
    @NotBlank
    private String idCard;
    @NotBlank
    private String address;
    private String remark;
    private LocalDateTime createdAt;
}

