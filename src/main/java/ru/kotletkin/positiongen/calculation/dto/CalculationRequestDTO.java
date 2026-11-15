package ru.kotletkin.positiongen.calculation.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import ru.kotletkin.positiongen.calculation.validation.ConsistentCalculation;

@Data
@ConsistentCalculation
public class CalculationRequestDTO {

    @Size(max = 200, message = "Label must be at most 200 characters")
    private String label = "";

    @Min(value = 2, message = "Dimension must be at least 2: coordinates use the first two axes")
    @Max(value = 16, message = "Dimension must be at most 16")
    private int dimension = 2;

    @DecimalMin(value = "0.000001", message = "Fractal dimension must be greater than zero")
    private double fractalDimension = 1.33;

    @Min(value = 1, message = "At least one place is required")
    @Max(value = 20000, message = "At most 20,000 places")
    private int pointCount = 250;

    @Min(value = 1, message = "At least one traveler is required")
    @Max(value = 5000, message = "At most 5,000 travelers")
    private int travelerCount = 8;

    @Min(value = 0, message = "Co-traveler count cannot be negative")
    @Max(value = 5000, message = "At most 5,000 co-travelers")
    private int coTravelerCount = 1;

    @DecimalMin(value = "0.001", message = "Observation window must be greater than zero")
    @DecimalMax(value = "3650", message = "Observation window must be at most 3650 days")
    private double days = 14;

    @DecimalMin(value = "0.001", message = "Mean report count must be greater than zero")
    @DecimalMax(value = "1000", message = "Mean report count must be at most 1000")
    private double meanReportCount = 40;
}
