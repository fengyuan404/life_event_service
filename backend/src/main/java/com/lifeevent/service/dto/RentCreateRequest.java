package com.lifeevent.service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record RentCreateRequest(
        @NotNull Integer familyId,
        @NotNull Integer deceasedId,
        @NotNull Integer graveId,
        @NotNull Integer staffId,
        @NotNull LocalDate startDate,
        @NotNull LocalDate expireDate,
        @NotNull @DecimalMin("0.00") BigDecimal totalAmount
) {
}

