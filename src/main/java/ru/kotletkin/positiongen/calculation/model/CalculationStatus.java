package ru.kotletkin.positiongen.calculation.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CalculationStatus {
    PENDING("PENDING"),
    RUNNING("CALCULATING"),
    SUCCEEDED("SUCCEEDED"),
    FAILED("FAILED");

    private final String title;
}
