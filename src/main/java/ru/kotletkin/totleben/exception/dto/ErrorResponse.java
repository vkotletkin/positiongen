package ru.kotletkin.totleben.exception.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "Standard API error payload")
public record ErrorResponse(
        @Schema(description = "Timestamp when the error occurred", example = "2026-06-21T04:31:07.944+03:00")
        OffsetDateTime time,
        @Schema(description = "Short error title or field name", example = "startDate")
        String message,
        @Schema(description = "Human-readable details", example = "Required request parameter is missing")
        String description) {
}
