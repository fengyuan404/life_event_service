package com.lifeevent.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("deceased")
public class Deceased {
    @TableId(type = IdType.AUTO)
    private Integer deceasedId;
    @NotNull
    private Integer familyId;
    @NotBlank
    private String deceasedName;
    @NotBlank
    private String gender;
    @NotBlank
    private String idCard;
    @NotNull
    private LocalDate birthDate;
    @NotNull
    private LocalDate deathDate;
    @NotBlank
    private String relationToFamily;
    private String epitaph;
    @NotBlank
    private String burialType;
    private LocalDateTime createdAt;
}

