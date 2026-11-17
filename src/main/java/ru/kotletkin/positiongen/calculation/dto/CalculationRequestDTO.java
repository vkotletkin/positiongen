package ru.kotletkin.positiongen.calculation.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import ru.kotletkin.positiongen.calculation.places.PlacePoint;
import ru.kotletkin.positiongen.calculation.validation.ConsistentCalculation;

import java.util.List;

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

    private boolean travelTogether = false;

    @Min(value = 0, message = "Together window cannot be negative")
    @Max(value = 720, message = "Together window must be at most 720 minutes")
    private int togetherMinutes = 15;

    @Min(value = 1, message = "Observation window must be at least 1 day")
    @Max(value = 3650, message = "Observation window must be at most 3650 days")
    private int days = 14;

    @Min(value = 1, message = "Mean report count must be at least 1")
    @Max(value = 1000, message = "Mean report count must be at most 1000")
    private int meanReportCount = 40;

    @DecimalMin(value = "0.0", message = "New-cluster rate cannot be negative")
    @DecimalMax(value = "10000", message = "New-cluster rate must be at most 10000")
    private double restartRate = 0.75;

    @DecimalMin(value = "-20.0", message = "Distance exponent must be at least -20")
    @DecimalMax(value = "20.0", message = "Distance exponent must be at most 20")
    private double distanceExponent = -2.0;

    @DecimalMin(value = "0.001", message = "Speed limit must be greater than zero")
    @DecimalMax(value = "5000", message = "Speed limit must be at most 5000 km/h")
    private double maxKilometersPerHour = 100.0;

    @DecimalMin(value = "0.001", message = "Kilometers per model unit must be greater than zero")
    @DecimalMax(value = "100000", message = "Kilometers per model unit must be at most 100000")
    private double kilometersPerUnit = 20.0;

    @DecimalMin(value = "0.0", message = "Mean exploration parameter cannot be negative")
    @DecimalMax(value = "10000", message = "Mean exploration parameter must be at most 10000")
    private double phiMean = 10.0;

    @DecimalMin(value = "1.0", message = "Mean trajectory length must be at least 1")
    @DecimalMax(value = "100000", message = "Mean trajectory length must be at most 100000")
    private double stepsMean = 100.0;

    @DecimalMin(value = "0.0000001", message = "Pareto lower bound must be greater than zero")
    @DecimalMax(value = "0.999", message = "Pareto lower bound must be less than 1")
    private double reportDelta = 0.001;

    @DecimalMin(value = "-20.0", message = "Pareto tail must be at least -20")
    @DecimalMax(value = "-1.0", inclusive = false, message = "Pareto tail must be less than -1")
    private double reportAlpha = -1.5;

    /**
     * Optional place set. When present, these places replace the generated ones.
     * The form uploads a CSV instead of binding this field.
     */
    private List<PlacePoint> places;
}
