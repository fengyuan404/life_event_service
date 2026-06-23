package com.lifeevent.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("staff")
public class Staff {
    @TableId(type = IdType.AUTO)
    private Integer staffId;
    private String username;
    private String passwordHash;
    private String staffName;
    private String gender;
    private String phone;
    private String role;
    private String status;
    private LocalDate hireDate;
    private LocalDateTime createdAt;
}

