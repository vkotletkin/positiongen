package ru.kotletkin.totleben.calculation.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import ru.kotletkin.totleben.calculation.validation.ConsistentCalculation;

@Data
@ConsistentCalculation
public class CalculationRequestDTO {

    @Size(max = 200, message = "Название не длиннее 200 символов")
    private String label = "";

    @Min(value = 2, message = "Размерность не меньше 2: координаты берутся по первым двум осям")
    @Max(value = 16, message = "Размерность не больше 16")
    private int dimension = 2;

    @DecimalMin(value = "0.000001", message = "Фрактальная размерность должна быть больше нуля")
    private double fractalDimension = 1.33;

    @Min(value = 1, message = "Нужна хотя бы одна точка")
    @Max(value = 20000, message = "Не больше 20 000 точек")
    private int pointCount = 250;

    @Min(value = 1, message = "Нужен хотя бы один путешественник")
    @Max(value = 5000, message = "Не больше 5 000 путешественников")
    private int travelerCount = 8;

    @Min(value = 0, message = "Число попутчиков не бывает отрицательным")
    @Max(value = 5000, message = "Не больше 5 000 попутчиков")
    private int coTravelerCount = 1;

    @DecimalMin(value = "0.001", message = "Окно наблюдения должно быть больше нуля")
    @DecimalMax(value = "3650", message = "Окно наблюдения не больше 3650 суток")
    private double days = 14;

    @DecimalMin(value = "0.001", message = "Среднее число отчётов должно быть больше нуля")
    @DecimalMax(value = "1000", message = "Среднее число отчётов не больше 1000")
    private double meanReportCount = 40;
}
