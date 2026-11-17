package ru.kotletkin.positiongen.calculation.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import ru.kotletkin.positiongen.result.CalculationResult;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "calculation_request")
public class CalculationRequest {

    @Id
    private UUID id;

    @Column(length = 200)
    private String label;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private CalculationStatus status;

    @Column(nullable = false)
    private int dimension;

    @Column(nullable = false)
    private double fractalDimension;

    @Column(nullable = false)
    private int pointCount;

    private Boolean suppliedPlaces;

    public boolean isSuppliedPlaces() {
        return Boolean.TRUE.equals(suppliedPlaces);
    }

    @Column(nullable = false)
    private int travelerCount;

    @Column(nullable = false)
    private int coTravelerCount;

    private Boolean travelTogether;

    private Integer togetherMinutes;

    public boolean isTravelTogether() {
        return Boolean.TRUE.equals(travelTogether);
    }

    public int resolvedTogetherMinutes() {
        return togetherMinutes == null ? 15 : togetherMinutes;
    }

    @Column(nullable = false)
    private int days;

    @Column(nullable = false)
    private int meanReportCount;

    @Column(nullable = false)
    @ColumnDefault("0.75")
    private double restartRate;

    @Column(nullable = false)
    @ColumnDefault("-2.0")
    private double distanceExponent;

    @Column(nullable = false)
    @ColumnDefault("100.0")
    private double maxKilometersPerHour;

    @Column(nullable = false)
    @ColumnDefault("20.0")
    private double kilometersPerUnit;

    @Column(nullable = false)
    @ColumnDefault("10.0")
    private double phiMean;

    @Column(nullable = false)
    @ColumnDefault("100.0")
    private double stepsMean;

    @Column(nullable = false)
    @ColumnDefault("0.001")
    private double reportDelta;

    @Column(nullable = false)
    @ColumnDefault("-1.5")
    private double reportAlpha;

    @Column(length = 2000)
    private String errorMessage;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant startedAt;

    private Instant finishedAt;

    @OneToOne(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    private CalculationResult result;
}
