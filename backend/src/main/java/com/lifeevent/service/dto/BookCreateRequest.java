package com.lifeevent.service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record BookCreateRequest(
        @NotNull Integer familyId,
        @NotNull Integer graveId,
        @NotNull Integer staffId,
        @NotNull LocalDateTime bookTime,
        @Min(1) int visitorCount,
        String specialNeed
) {
}

