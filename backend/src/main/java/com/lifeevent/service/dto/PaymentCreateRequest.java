package com.lifeevent.service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentCreateRequest(
        @NotNull Integer rentId,
        @NotNull Integer staffId,
        @NotBlank String payType,
        @NotBlank String payMethod,
        @NotNull @DecimalMin("0.01") BigDecimal payAmount,
        @NotNull LocalDateTime payTime,
        @NotBlank String invoiceNo,
        String remark
) {
}

