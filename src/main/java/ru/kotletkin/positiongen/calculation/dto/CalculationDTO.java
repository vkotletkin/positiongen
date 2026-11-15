package ru.kotletkin.positiongen.calculation.dto;

import ru.kotletkin.positiongen.calculation.model.CalculationStatus;

import java.time.Instant;
import java.util.UUID;

public record CalculationDTO(
        UUID id,
        String label,
        CalculationStatus status,
        String statusTitle,
        int dimension,
        double fractalDimension,
        int pointCount,
        int travelerCount,
        int coTravelerCount,
        double days,
        double meanReportCount,
        Instant createdAt,
        Instant startedAt,
        Instant finishedAt,
        String errorMessage,
        Integer reportCount,
        Long xyByteSize,
        Long placesByteSize
) {
}
