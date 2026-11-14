package ru.kotletkin.totleben.calculation.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CalculationStatus {
    PENDING("Ожидает"),
    RUNNING("Считается"),
    SUCCEEDED("Готово"),
    FAILED("Ошибка");

    private final String title;
}
