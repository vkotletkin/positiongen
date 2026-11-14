package ru.kotletkin.totleben.calculation.model;

public enum CalculationStatus {
    PENDING("Ожидает"),
    RUNNING("Считается"),
    SUCCEEDED("Готово"),
    FAILED("Ошибка");

    private final String title;

    CalculationStatus(String title) {
        this.title = title;
    }

    public String title() {
        return title;
    }
}
