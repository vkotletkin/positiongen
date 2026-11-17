package ru.kotletkin.positiongen.calculation.dto;

import ru.kotletkin.positiongen.calculation.model.CalculationStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CalculationDTO(
        UUID id,
        String label,
        CalculationStatus status,
        String statusTitle,
        int dimension,
        double fractalDimension,
        int pointCount,
        boolean suppliedPlaces,
        int travelerCount,
        int coTravelerCount,
        boolean travelTogether,
        int togetherMinutes,
        int days,
        int meanReportCount,
        double restartRate,
        double distanceExponent,
        double maxKilometersPerHour,
        double kilometersPerUnit,
        double phiMean,
        double stepsMean,
        double reportDelta,
        double reportAlpha,
        Instant createdAt,
        Instant startedAt,
        Instant finishedAt,
        String errorMessage,
        Integer reportCount,
        Long xyByteSize,
        Long placesByteSize,
        List<Integer> coTravelerIds
) {
}
